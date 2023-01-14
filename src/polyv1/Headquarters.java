package polyv1;

import battlecode.common.*;

import java.awt.*;

public class Headquarters {
    RobotController rc;
    Lib lib;
    int roundsWithoutAnchor = 0;
    int carrierModifier = 2;

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
        if(rc.getRoundNum() >= 200){
            //we'll start doing those other things, but now focus on the launchers and carriers
            if(rc.getRoundNum() % 1 == 0) {
                spawnAnchors();
            }
        }
        if(rc.getRoundNum() < 200 || roundsWithoutAnchor < 40) {
            spawnCarrierAndLauncher();
        }

        if(rc.getRoundNum() > 500){
            carrierModifier = 4;
        }


    }

    void firstRoundSetup(){
        quadrant = lib.getQuadrant();
    }

    void spawnCarrierAndLauncher() throws GameActionException {
        if(rc.getRoundNum() % carrierModifier == 0){
            spawn(RobotType.CARRIER);
        }
        else {
            spawn(RobotType.LAUNCHER);
        }
    }

    void spawn(RobotType robot) throws GameActionException {
        for(Direction dir : lib.startDirList(lib.dirToIndex(rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2))))){
            if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir))){
                rc.buildRobot(robot, rc.getLocation().add(dir).add(dir));
            }
        }
    }

    void spawn(RobotType robot, Direction dir) throws GameActionException {
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir));
        }
    }

    void spawnAnchors() throws GameActionException {
        if(rc.canBuildAnchor(Anchor.STANDARD)){
            rc.buildAnchor(Anchor.STANDARD);
            roundsWithoutAnchor = 0;
        }
        else {
            roundsWithoutAnchor++;
        }
    }
}
