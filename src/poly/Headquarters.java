package poly;

import battlecode.common.*;

public class Headquarters {
    RobotController rc;
    Lib lib;
    int roundsWithoutAnchor = 0;
    int carrierModifier = 3;
    int numCarriers = 0;

    public Headquarters(RobotController robot) throws GameActionException {
        rc = robot;
        lib = new Lib(rc);
        lib.writeHQ(rc.getLocation());
        System.out.println(lib.getBase());
    }

    int quadrant;

    public void takeTurn() throws GameActionException {
        if(rc.getRoundNum() == 1){
            firstRoundSetup();
        }
        if(rc.getRoundNum() == 2){
            //you can figure out the enemy bases due to the position of your bases
            MapLocation[] hqs = lib.getHqs();
            MapLocation[] enemyHq = new MapLocation[hqs.length];
            int i = 0;
            for(MapLocation m : hqs){
                int q = lib.getQuadrant(m);
                MapLocation origin = lib.getOrigin(q);
                int xOffset = m.x - origin.x;
                int yOffset = m.y - origin.y;
                int oppositeQ = 0;
                if(q == 1){
                    oppositeQ = 3;
                }
                if(q == 2){
                    oppositeQ = 4;
                }
                if(q == 3){
                    oppositeQ = 1;
                }
                if(q == 4){
                    oppositeQ = 2;
                }
                MapLocation otherOrigin = lib.getOrigin(oppositeQ);
                int realX = 0;
                int realY = 0;
                switch (oppositeQ){
                    case 1: realX = otherOrigin.x + Math.abs(xOffset); realY = otherOrigin.y + Math.abs(yOffset); break;
                    case 2: realX = otherOrigin.x - Math.abs(xOffset); realY = otherOrigin.y + Math.abs(yOffset); break;
                    case 3: realX = otherOrigin.x - Math.abs(xOffset); realY = otherOrigin.y - Math.abs(yOffset); break;
                    case 4: realX = otherOrigin.x + Math.abs(xOffset); realY = otherOrigin.y - Math.abs(yOffset); break;
                }
                enemyHq[i] = new MapLocation(realX, realY);
              //  lib.writeEnemyHQ(enemyHq[i]);
            }
        }

        //what to make?
        //first 100 rounds
        //  carrier every other round
        //  launcher every other round
        //then after
        //  every 50th round, amplifier
        //  every time the carrier asks, make an anchor
        //  and just follow the every other round thing of the carrier and launcher

        if(roundsWithoutAnchor < 100 || rc.getRoundNum() < 500) {
            spawnCarrierAndLauncher();
        }

        if(rc.getRoundNum() >= 200){
            //we'll start doing those other things, but now focus on the launchers and carriers
            if(rc.getRoundNum() % 1 == 0) {
                spawnAnchors();
            }
        }

        if(rc.getRoundNum() > 250){
            carrierModifier = 2;
        }
        else {
            carrierModifier = 3;
        }


        for(RobotInfo r : lib.getRobots()){
            if(r.getTeam() != rc.getTeam()){
                carrierModifier = 7;
            }
        }

    //todo, !IMPORTANT the first 4 carriers should spawn on the 4 corners

    }

    void firstRoundSetup(){
        quadrant = lib.getQuadrant();
    }

    void spawnCarrierAndLauncher() throws GameActionException {
        if(rc.getRoundNum() % carrierModifier == 0){
            if(numCarriers < 4 && rc.getRoundNum() < 50){ //make these corners based on where the base is, where the first corner is nearest to the center
                if(spawn(RobotType.CARRIER, Lib.directions[numCarriers*2+1])){
                    numCarriers++;
                }
            }
            spawn(RobotType.CARRIER,0);
        }
        else {
            System.out.println("want to spawn launcher");
            spawn(RobotType.LAUNCHER,0);
        }
    }

    boolean spawn(RobotType robot, int offset) throws GameActionException {
        for(Direction dir : lib.reverse(lib.startDirList(lib.dirToIndex(rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2))), offset))){ //todo, make sure this points at hq's, and for resources for carriers, near resources if near one
            if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir))){
                rc.buildRobot(robot, rc.getLocation().add(dir).add(dir));
                return true;
            }
            if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight()))){
                rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight()));
                return true;
            }
            if(rc.canBuildRobot(robot, rc.getLocation().add(dir))){
                rc.buildRobot(robot, rc.getLocation().add(dir));
                return true;
            }
        }
        return false;
    }

    boolean spawn(RobotType robot, Direction dir) throws GameActionException {
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir));
            return true;
        }
        return false;
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
