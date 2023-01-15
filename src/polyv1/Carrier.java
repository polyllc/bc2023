package polyv1;

import battlecode.common.*;

import java.util.Arrays;

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
        if(rc.getRoundNum() > 350 && rc.getRoundNum() < 500){ //todo, optimize this
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

        antiLock();

        if(job == Jobs.GETTINGSRESOURCES){
            turnsLookingForResource++;
            if(targetLoc == Lib.noLoc) {
                for(WellInfo loc : rc.senseNearbyWells()){
                        if(mainResource == null){
                            if(turnsLookingForResource > 80 || loc.getResourceType().equals(primaryResource)) {
                                mainResource = loc;
                                targetLoc = loc.getMapLocation();
                                dirGoing = Direction.CENTER;
                            }
                        }
                        if(!lib.contains(resourceLocs, loc)){
                            //resourceLocs[]
                        }
                }
            }
            if(targetLoc != Lib.noLoc){
                if(targetLoc.equals(myHQ) && !lib.isFullResources()){
                    targetLoc = Lib.noLoc;
                    dirGoing = rc.getLocation().directionTo(rc.getLocation());
                }
                if(lib.isFullResources()){
                    stopMoving = false;
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


        //todo, bring those free island coords to the hq so they can immediately build an anchor and new carriers can just take over an island
        if(islandLoc == Lib.noLoc){ //somehow I need to make sure that once an island is taken, don't go there anymore (just defend with launchers)
            if(lib.getIslandLocs() != null) {
                if (lib.getIslandLocs().length > 0) {
                    islandIndex = lib.getIsland();
                    if(islandIndex != 0) {
                        islandLoc = rc.senseNearbyIslandLocations(islandIndex)[0]; //this will always give a free island!
                        //targetLoc = shouldIAnchor();
                    }
                    System.out.println(islandIndex);
                }
            }
        }
        else{

            if(rc.canSenseLocation(targetLoc)){
                if(rc.senseIsland(targetLoc) != -1) {
                    if (rc.senseTeamOccupyingIsland(rc.senseIsland(targetLoc)) == rc.getTeam()) {
                        islandLoc = Lib.noLoc;
                        targetLoc = Lib.noLoc; //this may loop back to the island that is still occupied
                        dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
                        System.out.println("occupied already");
                    }
                }
            }

            if(rc.canTakeAnchor(myHQ, Anchor.STANDARD)){
                rc.takeAnchor(myHQ, Anchor.STANDARD);
                targetLoc = islandLoc;
                job = Jobs.PLACINGARCHON;
                System.out.println("took anchor");
            }
            if(rc.getAnchor() != null) {
                if (rc.getLocation().equals(targetLoc) || rc.getLocation().equals(islandLoc)) {
                    if (rc.canPlaceAnchor()) {
                        System.out.println("placed anchor");
                        rc.placeAnchor();
                        job = Jobs.GETTINGSRESOURCES;
                        targetLoc = Lib.noLoc;
                        dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
                    }
                }
            }
        }

        attack(); //attack if there are enemies nearby, and you seem to be losing uh oh
        statusReport(); //we'll do status reports when something notable comes up

    }

    void move() throws GameActionException {
        if(lib.detectCorner(dirGoing)){
            //dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2));
            dirGoing = dirGoing.opposite();
        }
        if(!stopMoving) {
            if (!targetLoc.equals(Lib.noLoc)) {
                nav.goTo(targetLoc);
            }
            if (dirGoing != Direction.CENTER) {
                nav.goTo(dirGoing);
            }
        }
    }

    void transferToHQ() throws GameActionException {
        if(rc.canTransferResource(myHQ, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))){
            rc.transferResource(myHQ, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR));
        }
        if(rc.canTransferResource(myHQ, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))){
            rc.transferResource(myHQ, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
        }
        if(rc.canTransferResource(myHQ, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))){
            rc.transferResource(myHQ, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
        }
    }


    void statusReport(){
        rc.setIndicatorString("targetLoc: " + targetLoc +
                            "\nislandLoc: " + islandLoc +
                            "\ndir: " + dirGoing +
                            "\njob: " + job);
    }


    void attack() throws GameActionException {
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(rc.getLocation().distanceSquaredTo(myHQ) < 20){
                    if(lib.getWeight() > 0) {
                        if (rc.canAttack(robot.getLocation())) {
                            rc.attack(robot.getLocation());
                            System.out.println("attacking");
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
