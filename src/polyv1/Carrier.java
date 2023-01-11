package polyv1;

import battlecode.common.*;

import java.util.Arrays;

public class Carrier {
    RobotController rc;
    Navigation nav;
    Lib lib;
    int startedRound;
    int lastRound;
    RobotInfo myHQ;

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
    }

    Direction dirGoing = Direction.CENTER;
    MapLocation targetLoc;

    public void takeTurn() throws GameActionException {
        if(rc.isMovementReady()){
            move();
        }
        if(startedRound == rc.getRoundNum() || startedRound+1 == rc.getRoundNum()){
            System.out.println(Arrays.toString(lib.getRobots()));
            for(RobotInfo robot : lib.getRobots()){
                if(robot.getType() == RobotType.HEADQUARTERS && rc.getTeam() == robot.getTeam()){
                    myHQ = robot;
                    dirGoing = robot.getLocation().directionTo(rc.getLocation()); //opposite of hq dir
                }
            }
        }
    }

    void move() throws GameActionException {
        if(dirGoing != Direction.CENTER){
            nav.tryMove(dirGoing);
        }
    }


}
