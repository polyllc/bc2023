package poly;

import battlecode.common.*;

import java.awt.*;
import java.util.Arrays;

public class Headquarters {
    RobotController rc;
    Lib lib;
    int roundsWithoutAnchor = 0;
    int carrierModifier = 3;
    int numCarriers = 0;
    int hqNum = 0;
    int numLaunchers = 0;
    MapLocation[] enemies = new MapLocation[4];

    public Headquarters(RobotController robot) throws GameActionException {
        rc = robot;
        lib = new Lib(rc);
        lib.writeHQ(rc.getLocation());
       // System.out.println(rc.getLocation());
        lib.updateHQNum();
        hqNum = lib.getHQNum();
        enemies[0] = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
    }

    int quadrant;

    public void takeTurn() throws GameActionException {


        if(rc.getRoundNum() == 1){
            firstRoundSetup();
        }
        if(rc.getRoundNum() == 2){
            lib.writeHQ(rc.getLocation());
            lib.writeHQ(rc.getLocation());
            lib.writeHQ(rc.getLocation());
        }
        if(rc.getRoundNum() == 3){
            lib.writeHQ(rc.getLocation());
        }
        if(rc.getRoundNum() == 4){
            lib.writeHQ(rc.getLocation());
        }
        if(rc.getRoundNum() == 1){
            //you can figure out the enemy bases due to the position of your bases
            MapLocation[] hqs = lib.getHqs();
            MapLocation[] enemyHq = new MapLocation[lib.getHQNum()];
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
                enemies[i] = new MapLocation(realX, realY);
                lib.writeEnemyHQ(enemyHq[i]);
            }
        }

        if(rc.getRoundNum() < 20){
            for(WellInfo w : rc.senseNearbyWells()) {
                if (rc.getRoundNum() % carrierModifier == 0) {
                    if (w.getResourceType() == ResourceType.MANA) {
                        lib.setMana(w.getMapLocation());
                        if (numCarriers < 4) { //make these corners based on where the base is, where the first corner is nearest to the center
                            if (spawn(RobotType.CARRIER, rc.getLocation().directionTo(w.getMapLocation()))) {
                                System.out.println("spawn based on near mana");
                                numCarriers++;
                            }
                        }
                    }
                    if(w.getResourceType() == ResourceType.ADAMANTIUM){
                        lib.setAda(w.getMapLocation());
                        if (numCarriers >= 4) { //make these corners based on where the base is, where the first corner is nearest to the center
                            if (spawn(RobotType.CARRIER, rc.getLocation().directionTo(w.getMapLocation()))) {
                                System.out.println("spawn based on near ada");
                                numCarriers++;
                            }
                        }
                    }
                }
            }



        }

        if(rc.getRoundNum() == 50){
              //  rc.resign();
        }

        if(roundsWithoutAnchor < 200 && rc.getNumAnchors(Anchor.STANDARD) < 1 || rc.getNumAnchors(Anchor.STANDARD) == 1 || rc.getRoundNum() < 500) {
            spawnCarrierAndLauncher();
        }


        rc.setIndicatorString("e1: " + lib.getBase() + " e2: " + lib.getBase(1) +" e3: " + lib.getBase(2) +" e4: " + lib.getBase(3));

        if(rc.getRoundNum() >= 200){

            //we'll start doing those other things, but now focus on the launchers and carriers
            if(rc.getRoundNum() % 1 == 0 && rc.getNumAnchors(Anchor.STANDARD) < 1) {
                spawnAnchors();
            }
        }

        if(rc.getRoundNum() > 250 || rc.getRoundNum() < 15){
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
    }



    void firstRoundSetup(){
        quadrant = lib.getQuadrant();
    }

    void spawnCarrierAndLauncher() throws GameActionException {
        if(rc.getRoundNum() % carrierModifier == 0){
            if(numCarriers < 6 && rc.getRoundNum() < 50){ //make these corners based on where the base is, where the first corner is nearest to the center
                if(spawn(RobotType.CARRIER, Lib.directions[(numCarriers*2+1) % 8])){ //skip the corner closest to the hq
                    System.out.println("spawn based on first 6 in dir: " + Lib.directions[(numCarriers*2+1) % 8]);
                    numCarriers++;
                }
            }
            else if(spawn(RobotType.CARRIER,rc.getRoundNum() >= 50 ? rc.getRoundNum() % 8 : 0)){
                System.out.println("spawn in each dir after round 50");
            }
        }
        else {
            if(rc.getRoundNum() < 35) {

                if(numLaunchers < 1){

                            if(spawn(RobotType.LAUNCHER, rc.getLocation().directionTo(new MapLocation(rc.getMapHeight()/2, rc.getMapWidth()/2)))){ //rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)))
                                System.out.println("spawning " + rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)));
                            }
                            if(spawn(RobotType.LAUNCHER, rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)))){
                                System.out.println("spawning " + rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)));
                            }
                            if(spawn(RobotType.LAUNCHER, rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)))){
                                System.out.println("spawning " + rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)));
                            }


                    numLaunchers++;
                }

                else {
                    //System.out.println("trying to build: " + rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)) + " " + lib.getEnemyBase() + " " + lib.getEnemyBase(hqNum-1));
                    if (spawn(RobotType.LAUNCHER, rc.getLocation().directionTo(lib.getEnemyBase(hqNum - 1)))) {
                        //System.out.println("spawning " + rc.getLocation().directionTo(lib.getEnemyBase(hqNum-1)));
                    }
                }
            }
            else if(spawn(RobotType.LAUNCHER,rc.getRoundNum() % 8)){
            }
        }
    }

    boolean spawn(RobotType robot, int offset) throws GameActionException {
        for(Direction dir : lib.reverse(lib.startDirList(lib.dirToIndex(rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2))), offset))){
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
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir));
            return true;
        }

        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight()));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateLeft()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateLeft()));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight().rotateRight()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateRight().rotateRight()));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateLeft().rotateLeft()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir).add(dir.rotateLeft().rotateLeft()));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir.rotateRight()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir.rotateRight()));
            return true;
        }
        if(rc.canBuildRobot(robot, rc.getLocation().add(dir).add(dir.rotateLeft()))){
            rc.buildRobot(robot, rc.getLocation().add(dir).add(dir.rotateLeft()));
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
