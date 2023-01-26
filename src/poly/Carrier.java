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
    MapLocation mainResource = null;
    boolean stopMoving = false;
    ResourceType primaryResource;
    int turnsLookingForResource; //after like 80 rounds, just go to the one that you see
    int numFills = 0;

    MapLocation islandLoc = Lib.noLoc;
    int islandIndex;

    enum Jobs {
        GETTINGSRESOURCES,
        SCOUTINGENEMIES,
        PLACINGARCHON,
        REPORTINGMANATOBASE
    }
    public Carrier(RobotController robot){
        rc = robot;
        nav = new Navigation(rc);
        startedRound = rc.getRoundNum();
        int lastRound = startedRound--;
        lib = new Lib(rc);
        job = Jobs.GETTINGSRESOURCES;
        if(rc.getRoundNum() > 1){
            if(rc.getRoundNum() % 2 == 0){
                primaryResource = ResourceType.ADAMANTIUM;
            }
            else {
                primaryResource = ResourceType.MANA; //todo, prioritize
            }
        }
        else {
            turnsLookingForResource = 81;
        }
        if(rc.getRoundNum() < 20){
            primaryResource = ResourceType.MANA;
            turnsLookingForResource = 0;
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
        if(startedRound+2 < rc.getRoundNum()){
            if(myHQ == null){
                myHQ = lib.getNearestHQ();
            }
        }

        //todo, bro fucking fix the discovery pathfinding, for both launcher and carrier

        if(job == Jobs.REPORTINGMANATOBASE){
            targetLoc = myHQ;
            if(rc.canWriteSharedArray(0,0)){
                lib.setMana(mainResource);
                targetLoc = mainResource;
                job = Jobs.GETTINGSRESOURCES;
            }
        }


        if(job == Jobs.GETTINGSRESOURCES){
            turnsLookingForResource++;
            if(targetLoc == Lib.noLoc || targetLoc == null) {
                if(turnsLookingForResource > 9 && rc.getRoundNum() < 100){
                    if(!lib.getMana().equals(new MapLocation(0,0))){
                        targetLoc = lib.getMana();
                        mainResource = lib.getMana();
                        dirGoing = Direction.CENTER;
                        primaryResource = null;
                    }
                }

                if(turnsLookingForResource > 1){
                    if(primaryResource != null) {
                        if (primaryResource.equals(ResourceType.MANA)) {
                            if (!lib.getMana().equals(new MapLocation(0, 0))) {
                                targetLoc = lib.getMana();
                                mainResource = lib.getMana();
                                dirGoing = Direction.CENTER;
                            }
                        }
                        else if (primaryResource.equals(ResourceType.ADAMANTIUM)) {
                            if (!lib.getAda().equals(new MapLocation(0, 0))) {
                                targetLoc = lib.getAda();
                                mainResource = lib.getAda();
                                dirGoing = Direction.CENTER;
                            }
                        }
                    }
                }

                for(WellInfo loc : rc.senseNearbyWells()){
                     if(mainResource == null){
                         if(turnsLookingForResource > 50 || loc.getResourceType().equals(primaryResource)) {

                             if(loc.getResourceType() == ResourceType.MANA){
                                 if(rc.getRoundNum() < 20){
                                     if(lib.getMana() == Lib.noLoc){
                                         job = Jobs.REPORTINGMANATOBASE;
                                     }
                                 }
                             }

                             mainResource = loc.getMapLocation();
                             targetLoc = loc.getMapLocation();
                             dirGoing = Direction.CENTER;
                             if(primaryResource != null) {
                                 if (primaryResource.equals(ResourceType.MANA)) {
                                     lib.setMana(loc.getMapLocation());
                                 }
                                 if (primaryResource.equals(ResourceType.ADAMANTIUM)) {
                                     lib.setAda(loc.getMapLocation());
                                 }
                             }
                         }
                     }
                     else{
                         targetLoc = mainResource;
                     }
                     if(!lib.contains(resourceLocs, loc)){
                         //resourceLocs[]
                     }
                }
            }
            if(targetLoc != Lib.noLoc && targetLoc != null){

                //implement surrounded code here

                if(senseSurrounded(targetLoc)){
                    if(targetLoc.equals(lib.getAda())){
                        if(!lib.getAda(1).equals(Lib.noLoc)){
                            targetLoc = lib.getAda(1);
                        }
                    }
                    else if(targetLoc.equals(lib.getMana())){
                        if(!lib.getMana(1).equals(Lib.noLoc)){
                            targetLoc = lib.getMana(1);
                        }
                    }
                    else {
                        targetLoc = Lib.noLoc;
                    }
                }


                if(rc.canSenseLocation(targetLoc)){
                    int nullify = 0;
                    if(rc.senseWell(targetLoc) == null){
                        RobotInfo info = rc.senseRobotAtLocation(targetLoc);
                        if(info == null){ //meaning most likely a bot or hq
                            nullify = 1;
                        }
                        else {
                            if(info.getTeam() != rc.getTeam()){
                                nullify = 1;
                            }
                            else if(info.getType() != RobotType.HEADQUARTERS){
                                nullify = 1;
                            }
                            else if(!rc.sensePassability(targetLoc)){ //if it's not a bot, then its a wall
                                nullify = 1;
                            }
                        }
                    }
                    if(nullify == 1){
                        if(lib.getAda(0).equals(targetLoc) || lib.getAda(1).equals(targetLoc)){ //i know this will only work when near an hq or island but maybe down the line, it'll just be a variable so when it goes back to hq it'll clear
                            lib.clearAda(targetLoc);
                        }
                        if(lib.getMana(0).equals(targetLoc) || lib.getMana(1).equals(targetLoc)){
                            lib.clearMana(targetLoc);
                        }
                        targetLoc = Lib.noLoc;
                    }
                }

                if(targetLoc.equals(myHQ) && !lib.isFullResources()){
                    targetLoc = Lib.noLoc;
                    dirGoing = rc.getLocation().directionTo(rc.getLocation());
                }
                if(lib.isFullResources() || (rc.getRoundNum() < 100 && lib.getWeight() >= 20) ){
                    stopMoving = false;
                    targetLoc = lib.getNearestHQ();
                    //System.out.println("nearest hq: " + );
                    //targetLoc = myHQ;
                    for(Direction dir : Lib.directions){
                        if(rc.getLocation().add(dir).equals(targetLoc)){
                            if(primaryResource != null) {
                                if (primaryResource.equals(ResourceType.MANA)) {
                                    if (lib.getMana().equals(new MapLocation(0, 0))) {
                                        lib.setMana(mainResource);
                                    }
                                }
                                if (primaryResource.equals(ResourceType.ADAMANTIUM)) {
                                    if (lib.getAda().equals(new MapLocation(0, 0))) {
                                        lib.setAda(mainResource);
                                    }
                                }
                            }
                            transferToHQ();
                            targetLoc = mainResource;
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

            if(targetLoc != null) {
                if (rc.canSenseLocation(targetLoc)) {
                    if (rc.senseIsland(targetLoc) != -1) {
                        if (rc.senseTeamOccupyingIsland(rc.senseIsland(targetLoc)) == rc.getTeam()) {
                            islandLoc = Lib.noLoc;
                            targetLoc = Lib.noLoc; //this may loop back to the island that is still occupied
                            job = Jobs.GETTINGSRESOURCES;
                            dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
                            System.out.println("occupied already");
                        }
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
                nav.goTo(targetLoc, false);
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
                            "\nj: " + mainResource +
                            "\np: " + primaryResource);
    }


    void attack() throws GameActionException {
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

    boolean senseSurrounded(MapLocation loc) throws GameActionException {
        if(!loc.equals(Lib.noLoc)){
            int i = 0;
            for(Direction dir : Lib.directions){
                if(rc.canSenseLocation(loc.add(dir))) {
                    if(!rc.getLocation().equals(loc.add(dir))) {
                        if (!rc.sensePassability(loc.add(dir)) || rc.senseRobotAtLocation(loc.add(dir)) != null) {
                            i++;
                        }
                    }
                }
            }
            return i == 8;
        }
        return false;
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
