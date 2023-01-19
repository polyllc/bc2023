package poly;

import battlecode.common.*;

public class Carrier {
    RobotController rc;
    Navigation nav;
    Lib lib;
    int startedRound;
    int lastRound;
    MapLocation myHQ;
    Jobs job;
    WellInfo[] resourceLocs = new WellInfo[64]; //probably overkill
    WellInfo mainResource = null;
    boolean stopMoving = false;
    ResourceType primaryResource;
    int turnsLookingForResource; //after like 80 rounds, just go to the one that you see

    MapLocation islandLoc = Lib.noLoc;
    int islandIndex;

    enum Jobs {
        GETTINGSRESOURCES,
        SCOUTINGENEMIES,
        PLACINGARCHON
    }
    public Carrier(RobotController robot){
        rc = robot;
        nav = new Navigation(rc);
        startedRound = rc.getRoundNum();
        int lastRound = startedRound--;
        lib = new Lib(rc);
        job = Jobs.GETTINGSRESOURCES;
        if(rc.getRoundNum() < 20){
            primaryResource = ResourceType.MANA;
            turnsLookingForResource = 61;
        }
        if(rc.getRoundNum() > 120){ //todo, optimize this
            if(rc.getRoundNum() % 2 == 0){
                primaryResource = ResourceType.ADAMANTIUM;
            }
            else {
                primaryResource = ResourceType.MANA;
            }
        }
        else {
            turnsLookingForResource = 81;
        }
    }

    Direction dirGoing = Direction.CENTER;
    MapLocation targetLoc = Lib.noLoc;

    public void takeTurn() throws GameActionException {
        if(rc.isMovementReady()){
            move();
        }
        if(startedRound == rc.getRoundNum() || startedRound+1 == rc.getRoundNum()){
            for(RobotInfo robot : lib.getRobots()){
                if(robot.getType() == RobotType.HEADQUARTERS && rc.getTeam() == robot.getTeam()){
                    myHQ = robot.getLocation();
                    dirGoing = robot.getLocation().directionTo(rc.getLocation()); //opposite of hq dir
                }
            }
        }



        //todo, if someone finds a nearer adamantium/mana resource, just go there
        //todo, bro fucking fix the discovery pathfinding, for both launcher and carrier


        if(job == Jobs.GETTINGSRESOURCES){
            turnsLookingForResource++;
            if(targetLoc == Lib.noLoc) {
                if(turnsLookingForResource > 9 && rc.getRoundNum() < 100){
                    if(!lib.getMana().equals(new MapLocation(0,0))){
                        targetLoc = lib.getMana();
                        dirGoing = Direction.CENTER;
                        primaryResource = null;
                    }
                }
                for(WellInfo loc : rc.senseNearbyWells()){
                     if(mainResource == null){
                         if(turnsLookingForResource > 50 || loc.getResourceType().equals(primaryResource)) {
                             mainResource = loc;
                             targetLoc = loc.getMapLocation();
                             dirGoing = Direction.CENTER;
                             primaryResource = null;
                             if(primaryResource.equals(ResourceType.MANA)){
                                 if(lib.getMana().equals(new MapLocation(0,0))){
                                     lib.setMana(loc.getMapLocation());
                                 }
                             }
                         }
                     }
                     else{
                         targetLoc = mainResource.getMapLocation();
                     }
                     if(!lib.contains(resourceLocs, loc)){
                         //resourceLocs[]
                     }
                }
            }
            if(targetLoc != Lib.noLoc && targetLoc != null){
                if(targetLoc.equals(myHQ) && !lib.isFullResources()){
                    targetLoc = Lib.noLoc;
                    dirGoing = rc.getLocation().directionTo(rc.getLocation());
                }
                if(lib.isFullResources() || rc.getRoundNum() < 100 && lib.getWeight() >= 20){
                    stopMoving = false;
                    targetLoc = lib.getNearestHQ();
                    targetLoc = myHQ;
                    for(Direction dir : Lib.directions){
                        if(rc.getLocation().add(dir).equals(targetLoc)){
                            transferToHQ();
                            targetLoc = mainResource.getMapLocation();
                            dirGoing = Direction.CENTER;
                        }
                    }
                }
                for(Direction dir : Lib.directions){
                    if(rc.getLocation().add(dir).equals(targetLoc)){
                        stopMoving = true;
                        if(rc.canCollectResource(rc.getLocation().add(dir),-1)){
                            rc.collectResource(rc.getLocation().add(dir), -1);
                        }
                    }
                }
            }
        }


        if(islandLoc == Lib.noLoc){ //somehow I need to make sure that once an island is taken, don't go there anymore (just defend with launchers)
            if(lib.getIslandLocs() != null) {
                if (lib.getIslandLocs().length > 0) {
                    islandIndex = lib.getIsland();
                    if(islandIndex != 0) {
                        MapLocation temp = rc.senseNearbyIslandLocations(islandIndex)[0];
                        if(temp != null) {
                            islandLoc = temp; //this will always give a free island!
                            //targetLoc = shouldIAnchor();
                        }
                    }
                }
            }
        }
        else{

            if(rc.canSenseLocation(targetLoc)){
                if(rc.senseIsland(targetLoc) != -1) {
                    if (rc.senseTeamOccupyingIsland(rc.senseIsland(targetLoc)) == rc.getTeam()) {
                        islandLoc = Lib.noLoc;
                        targetLoc = Lib.noLoc; //this may loop back to the island that is still occupied
                        job = Jobs.GETTINGSRESOURCES;
                        dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
                        System.out.println("occupied already");
                    }
                }
            }
            if(islandLoc != Lib.noLoc) {
                if (rc.canTakeAnchor(myHQ, Anchor.STANDARD)) {
                    rc.takeAnchor(myHQ, Anchor.STANDARD);
                    targetLoc = islandLoc;
                    job = Jobs.PLACINGARCHON;
                    System.out.println("took anchor");
                }
                if (rc.getAnchor() != null) {
                    if (rc.getLocation().equals(targetLoc) || rc.getLocation().equals(islandLoc) || lib.onIsland(islandIndex)) {
                        if (rc.canPlaceAnchor()) {
                            System.out.println("placed anchor");
                            rc.placeAnchor();
                            job = Jobs.GETTINGSRESOURCES;
                            targetLoc = Lib.noLoc;
                            dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
                            islandIndex = 0;
                        }
                    }
                }
            }
        }

        if(rc.getAnchor() != null){ //if you have an anchor, just explore
            job = Jobs.PLACINGARCHON;
        }

        attack(); //attack if there are enemies nearby, and you seem to be losing uh oh
        statusReport(); //we'll do status reports when something notable comes up
        antiLock();

    }

    void move() throws GameActionException {
        if(lib.detectCorner(dirGoing)){
            //dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2))
            dirGoing = dirGoing.opposite();
        }
        if(!stopMoving) {
            if (targetLoc != null && !targetLoc.equals(Lib.noLoc)) {
                nav.goTo(targetLoc);
            }
            else if (dirGoing != Direction.CENTER) {
                nav.goTo(dirGoing);
            }
        }
    }

    void transferToHQ() throws GameActionException {
        if(rc.canTransferResource(targetLoc, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))){
            rc.transferResource(targetLoc, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR));
        }
        if(rc.canTransferResource(targetLoc, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))){
            rc.transferResource(targetLoc, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
        }
        if(rc.canTransferResource(targetLoc, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))){
            rc.transferResource(targetLoc, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
        }
    }


    void statusReport(){
        rc.setIndicatorString("t: " + targetLoc +
                            "\ni: " + stopMoving +
                            "\nd: " + dirGoing +
                            "\nj: " + job +
                            "\np: " + primaryResource);
    }


    void attack() throws GameActionException { //todo, !important this shit dont work sometimes
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(rc.getLocation().distanceSquaredTo(myHQ) < 30){
                    if(lib.getWeight() > 0) {
                        if(rc.getLocation().distanceSquaredTo(robot.getLocation()) <= 9) {
                            if (rc.canAttack(robot.getLocation())) {
                                rc.attack(robot.getLocation());
                            }
                        }
                        else {
                           // stopMoving = false;
                           // targetLoc = myHQ;
                        }
                    }
                }
            }
        }
    }

    MapLocation shouldIAnchor(){
        if(rc.getRoundNum() < 300){
            return targetLoc;
        }
        if(rc.getRoundNum() < 500){
            if(rc.getRoundNum() % 4 == 1){
                return myHQ;
            }
        }
        if(rc.getRoundNum() < 700){
            if(rc.getRoundNum() % 2 == 1){
                return myHQ;
            }
        }
        if(rc.getRoundNum() < 1000){
            return myHQ;
        }
        return targetLoc;
    }

    void antiLock(){
        if(targetLoc == Lib.noLoc && dirGoing == Direction.CENTER){
            dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
        }
    }

}
