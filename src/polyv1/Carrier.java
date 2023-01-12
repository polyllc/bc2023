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
    MapLocation[] resourceLocs;

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
                }
            }
            if(targetLoc != Lib.noLoc){
                if(lib.isFullResources()){
                    targetLoc = myHQ;
                    for(Direction dir : Lib.directions){
                        if(rc.getLocation().add(dir).equals(targetLoc)){
                            transferToHQ();
                            System.out.println("sending");
                        }
                    }
                }
                for(Direction dir : Lib.directions){
                    if(rc.getLocation().add(dir).equals(targetLoc)){
                        if(rc.canCollectResource(rc.getLocation().add(dir),-1)){
                            rc.collectResource(rc.getLocation().add(dir), -1);
                        }
                    }
                }
            }
        }


    }

    void move() throws GameActionException {
        if(targetLoc != Lib.noLoc){
            nav.tryMove(rc.getLocation().directionTo(targetLoc));
        }
        if(dirGoing != Direction.CENTER){
            nav.tryMove(dirGoing);
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


}
