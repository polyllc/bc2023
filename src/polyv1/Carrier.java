package polyv1;

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

        if(job == Jobs.GETTINGSRESOURCES){
            if(targetLoc == Lib.noLoc) {
                for(WellInfo loc : rc.senseNearbyWells()){
                        targetLoc = loc.getMapLocation();
                        dirGoing = Direction.CENTER;
                        if(mainResource == null){
                            mainResource = loc;
                        }
                        if(!lib.contains(resourceLocs, loc)){
                            //resourceLocs[]
                        }
                }
            }
            if(targetLoc != Lib.noLoc){
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

        //statusReport(); we'll do status reports when something notable comes up

    }

    void move() throws GameActionException {
        detectCorner();
        if(!stopMoving) {
            if (!targetLoc.equals(Lib.noLoc)) {
                nav.goTo(targetLoc);
                System.out.println("target");
            }
            if (dirGoing != Direction.CENTER) {
                nav.goTo(dirGoing);
                System.out.println("direction");
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
        System.out.println("targetLoc: " + targetLoc +
                            "\njob: " + job +
                            "\ndirGoing: " + dirGoing +
                            "\nmainResource: " + mainResource +
                            "\nposition: " + rc.getLocation() +
                            "\n");
    }

    void detectCorner(){
        if(rc.getLocation().equals(new MapLocation(rc.getMapWidth() - 1, rc.getMapHeight() - 1)) ||
            rc.getLocation().equals(new MapLocation(0, rc.getMapHeight() - 1)) ||
            rc.getLocation().equals(new MapLocation(rc.getMapWidth() - 1, 0)) ||
            rc.getLocation().equals(new MapLocation(0,0))){
            dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2));

        }
    }

}
