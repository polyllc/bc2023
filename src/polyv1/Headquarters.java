package polyv1;

import battlecode.common.*;

import java.awt.*;

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
        //first 100 rounds
        //  carrier every other round
        //  launcher every other round
        //then after
        //  every 50th round, amplifier
        //  every time the carrier asks, make an anchor
        //  and just follow the every other round thing of the carrier and launcher
        spawnCarrierAndLauncher();
        if(rc.getRoundNum() >= 100){
            //we'll start doing those other things, but now focus on the launchers and carriers
        }




        if(rc.getRoundNum() > 800){
            rc.resign();
        }

    }

    void firstRoundSetup(){
        quadrant = lib.getQuadrant();
    }

    void spawnCarrierAndLauncher() throws GameActionException {
        if(rc.getRoundNum() % 2 == 0){
            spawn(RobotType.CARRIER);
        }
        if(rc.getRoundNum() % 2 == 1){
            spawn(RobotType.LAUNCHER);
        }
    }

    void spawn(RobotType robot) throws GameActionException {
        for(Direction dir : Lib.directions){
            if(rc.canBuildRobot(robot, rc.getLocation().add(dir))){
                rc.buildRobot(robot, rc.getLocation().add(dir));
            }
        }
    }

    void spawn(RobotType robot, Direction dir) throws GameActionException {
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir));
        }
    }
}
