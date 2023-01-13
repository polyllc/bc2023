package polyv1;

import battlecode.common.*;

public class Headquarters {
    RobotController rc;
    Lib lib;

    public Headquarters(RobotController robot){
        rc = robot;
        lib = new Lib(rc);
    }

    int quadrant;

    public void takeTurn() throws GameActionException {
        if(rc.getRoundNum() == 1){
            firstRoundSetup();
        }

        //what to make?

        for(Direction dir : Lib.directions){
            if(rc.canBuildRobot(RobotType.CARRIER, rc.getLocation().add(dir))){
                rc.buildRobot(RobotType.CARRIER, rc.getLocation().add(dir));
            }
        }

        if(rc.getRoundNum() > 800){
            rc.resign();
        }

    }

    void firstRoundSetup(){
        quadrant = lib.getQuadrant();
    }
}
