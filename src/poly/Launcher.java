package poly;

import battlecode.common.*;

import java.util.Arrays;

public class Launcher {
    RobotController rc;
    Navigation nav;
    Lib lib;
    int startedRound;
    int lastRound;
    MapLocation myHQ;
    Jobs job;
    boolean stopMoving = false;
    MapLocation enemyHQ = Lib.noLoc;
    int turnInDir = 0;
    int turnsJob = 0;
    int enemyID;
    int dontSurroundBaseFor = 0;
    MapLocation[] enemyBaseAlreadyThere = new MapLocation[4];
    int enemyBaseI = 0;


    public Launcher(RobotController robot){
        rc = robot;
        nav = new Navigation(rc);
        startedRound = rc.getRoundNum();
        int lastRound = startedRound--;
        lib = new Lib(rc);
        job = Jobs.FINDINGENEMIES;
        for(RobotInfo r : lib.getRobots()){
            if(r.getTeam() != rc.getTeam()){
                job = Jobs.DEFENDINGBASE;
            }
        }
        if(rc.getRoundNum() > 300){
            if(rc.getRoundNum() % 2 == 0){
                job = Jobs.DEFENDINGBASE;
            }
        }
        if(rc.getRoundNum() < 6) {
            stopMoving = true;
        }

        enemyBaseAlreadyThere[0] = Lib.noLoc;
        enemyBaseAlreadyThere[1] = Lib.noLoc;
        enemyBaseAlreadyThere[2] = Lib.noLoc;
        enemyBaseAlreadyThere[3] = Lib.noLoc;
    }
    enum Jobs {
        FINDINGENEMIES,
        SURROUNDINGBASE,
        KILLINGENEMIES, //pretty sure this will be after all of the bases are surrounded so they can do whatever
        DESTROYINGANCHOR,
        DEFENDINGBASE, //all hands on deck!
        REPORTINGBASE,
        CHASINGENEMY
    }

    Direction dirGoing = Direction.CENTER;
    MapLocation targetLoc = Lib.noLoc;

    //shit to try
    //dont move until like round 50
    //move somehow as a group much better
    //target the highest health
    //target the lowest health
    //attack together, make sure that the maximum amount of shots go out
    //hang around mid and kill all of the launchers that come by
    //separate them into groups with different tasks, there is still a lot of space in the array

    //todo, defend base more
    //todo, group up all of the launchers and attack the other launchers, don't really care about attacking the base immediately (if attacking the base the first time failed)
    //todo, once at an enemy base, don't stick around! find other enemies around the map

    public void takeTurn() throws GameActionException {

        if(stopMoving && rc.getRoundNum() == 6){
            stopMoving = false;
        }

       //System.out.println("1: " + Clock.getBytecodeNum());

        if(dirGoing != Direction.CENTER){ //todo, this actually doesn't work wtf
            turnInDir++;
            if(turnInDir > 65){
                dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
                turnInDir = 0;
            }
        }
        if(rc.isMovementReady()){
          // System.out.println("2: " + Clock.getBytecodeNum());
            move();
        }
       //System.out.println("3: " + Clock.getBytecodeNum());
        if(startedRound == rc.getRoundNum() || startedRound+1 == rc.getRoundNum() || myHQ == null){
            for(RobotInfo robot : lib.getRobots()){
                if(robot.getType() == RobotType.HEADQUARTERS && rc.getTeam() == robot.getTeam()){
                    myHQ = robot.getLocation();
                    dirGoing = lib.educatedGuess(myHQ); //opposite of hq
                }
            }
           //System.out.println("4: " + Clock.getBytecodeNum());
            //todo, send an amplifier with them so they can remove the enemy hq from the list that are needed to be surrounded
            if(lib.getEnemyBase() != Lib.noLoc){
                targetLoc = lib.getEnemyBase();
                job = Jobs.SURROUNDINGBASE;
            }
           //.out.println("5: " + Clock.getBytecodeNum());
        }

        if(rc.isActionReady()) {
            attack();
        }
       //System.out.println("6: " + Clock.getBytecodeNum());
        if(job == Jobs.FINDINGENEMIES){
            if(targetLoc == Lib.noLoc) {
                //find enemy bases, we'll surround them
                //whatever is first, anchor or base
                //if anchor, we destroy
                if(lib.getEnemyBase() != Lib.noLoc && rc.getRoundNum() > 50 && !lib.contains(enemyBaseAlreadyThere, lib.getEnemyBase())){ //todo, shit dont work
                    System.out.println("from lib.getenemybase() " + !lib.contains(enemyBaseAlreadyThere, lib.getEnemyBase()) + " " + lib.getEnemyBase() + " " + Arrays.toString(enemyBaseAlreadyThere));
                    enemyHQ = lib.getEnemyBase();
                    targetLoc = enemyHQ;
                    job = Jobs.SURROUNDINGBASE;
                }
               //System.out.println("7: " + Clock.getBytecodeNum());
                for(RobotInfo robot : lib.getRobots()){
                    if(robot.getTeam() != rc.getTeam()){
                        if(robot.getType() == RobotType.HEADQUARTERS){
                            enemyHQ = robot.getLocation();
                            if(rc.getRoundNum() > 35 && !lib.contains(enemyBaseAlreadyThere, lib.getEnemyBase())){
                                targetLoc = myHQ;
                                job = Jobs.REPORTINGBASE;
                                break;
                            }
                            //targetLoc = robot.getLocation();
                            job = Jobs.SURROUNDINGBASE;

                        } else {
                            job = Jobs.CHASINGENEMY;
                            enemyID = robot.getID();
                            targetLoc = robot.getLocation();
                        }
                    }
                }
              // System.out.println("8: " + Clock.getBytecodeNum());
            }
            if(targetLoc != Lib.noLoc){
                //essentially, once near an enemy base, surround it!
                //also hit enemy amplifiers
                if(rc.canSenseLocation(targetLoc)) {
                    if (rc.senseRobotAtLocation(targetLoc) == null) {
                        targetLoc = Lib.noLoc;
                    }
                }
            }
        }

        if(job == Jobs.CHASINGENEMY){
            if(enemyID == 0){
                job = Jobs.FINDINGENEMIES;
            }
            else {
                for(RobotInfo r : lib.getRobots()){
                    if(r.getID() == enemyID){
                        targetLoc = r.getLocation();
                    }
                }
                if(!rc.canSenseRobot(enemyID)){ //todo, it should probably go somewhere after this, right now they're just dicking around being completely useless
                    enemyID = 0;
                    job = Jobs.FINDINGENEMIES;
                    targetLoc = Lib.noLoc;
                }
                else if(!lib.contains(lib.getRobots(), rc.senseRobot(enemyID))){
                    enemyID = 0;
                    job = Jobs.FINDINGENEMIES;
                    targetLoc = Lib.noLoc;
                }
            }
        }


        if(job == Jobs.SURROUNDINGBASE){
          // System.out.println("9: " + Clock.getBytecodeNum());
            if(targetLoc != Lib.noLoc){

                for(RobotInfo robot : lib.getRobots()){
                    if(rc.getTeam() != robot.getTeam()){
                        if(robot.getType() == RobotType.HEADQUARTERS){
                            enemyHQ = robot.getLocation();
                            targetLoc = robot.getLocation();
                        }
                        else {
                            job = Jobs.CHASINGENEMY;
                            enemyID = robot.getID();
                            targetLoc = robot.getLocation();
                        }
                    }
                }

                if(rc.canSenseLocation(targetLoc)){ //todo, sometimes this shit dont work (ex pit polyv5 vs poly)
                    if(rc.canSenseRobotAtLocation(targetLoc)){
                        if(rc.senseRobotAtLocation(targetLoc) == null){
                            lib.clearEnemyHQ(targetLoc);
                            targetLoc = Lib.noLoc;
                            enemyHQ = Lib.noLoc;
                            job = Jobs.FINDINGENEMIES;
                        }
                        else if(rc.senseRobotAtLocation(targetLoc).getType() != RobotType.HEADQUARTERS){ //so no exception occurs
                            lib.clearEnemyHQ(targetLoc);
                            job = Jobs.FINDINGENEMIES;
                            targetLoc = Lib.noLoc;
                            enemyHQ = Lib.noLoc;
                        }
                    }
                    else { //there is no bot there
                        lib.clearEnemyHQ(targetLoc);
                        job = Jobs.FINDINGENEMIES;
                        targetLoc = Lib.noLoc;
                        enemyHQ = Lib.noLoc;
                    }

                }
                surroundEnemyBase();
            }
            else {
                job = Jobs.FINDINGENEMIES;
            }
        }
       //System.out.println("10: " + Clock.getBytecodeNum());
        if(job == Jobs.DEFENDINGBASE){
            //circle around the base, don't stay near it
            circleBase();
        }

        if(job == Jobs.REPORTINGBASE){
            if(lib.getEnemyBase(enemyHQ)){
                targetLoc = enemyHQ;
                job = Jobs.SURROUNDINGBASE;
            }
            if(rc.getLocation().distanceSquaredTo(myHQ) < 9){
                lib.clearEnemyHQ(lib.getEnemyBase());
                lib.writeEnemyHQ(enemyHQ);
                job = Jobs.SURROUNDINGBASE;
                targetLoc = enemyHQ;
            }
        }
       //System.out.println("11: " + Clock.getBytecodeNum());

       //System.out.println("12: " + Clock.getBytecodeNum());

        statusReport();
       //System.out.println("13: " + Clock.getBytecodeNum());

    }

    void move() throws GameActionException {
        if(lib.detectCorner(dirGoing)){
            //dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2));
            dirGoing = dirGoing.rotateRight();
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



    void statusReport(){
        rc.setIndicatorString(Arrays.toString(enemyBaseAlreadyThere));
        rc.setIndicatorString("t: " + targetLoc +
               "\nj: " + job +
               "\nd: " + dirGoing +
              "\nmyhq: " + myHQ +
               "\n");
    }

    void attack() throws GameActionException {

        if(job == Jobs.CHASINGENEMY) {  //todo, still not attacking the lowest health enemy, all of the first 3 launchers should be attacking 1 enemy at once
            if (rc.canSenseRobot(enemyID)){
                RobotInfo enemy = rc.senseRobot(enemyID);
                if(rc.canAttack(enemy.getLocation())){
                    int health = enemy.getHealth();
                    if(health <= 30){
                        enemyID = 0;
                        job = Jobs.FINDINGENEMIES;
                    }
                    rc.attack(enemy.getLocation());
                }
            }
        }
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(robot.getType() == RobotType.LAUNCHER){ //priority
                    if(rc.canAttack(robot.getLocation())){
                        rc.attack(robot.getLocation());
                    }
                }
            }
        }
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(robot.getType() == RobotType.AMPLIFIER){ //priority
                    if(rc.canAttack(robot.getLocation())){
                        rc.attack(robot.getLocation());
                    }
                }
            }
        }
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(robot.getType() != RobotType.HEADQUARTERS){
                    if(rc.canAttack(robot.getLocation())){
                        rc.attack(robot.getLocation());
                    }
                }
            }
        }
    }

    void surroundEnemyBase() throws GameActionException {
        if(rc.getLocation().distanceSquaredTo(targetLoc) < 6){
            if(enemyHQ.equals(Lib.noLoc)){
                enemyHQ = targetLoc;
            }
            if(!lib.contains(enemyBaseAlreadyThere, targetLoc)){ //todo, shit dont work
                System.out.println("added " + targetLoc + " " + Arrays.toString(enemyBaseAlreadyThere));
                enemyBaseAlreadyThere[enemyBaseI] = targetLoc;
                enemyBaseI++;
            }
            targetLoc = Lib.noLoc;
            dirGoing = enemyHQ.directionTo(rc.getLocation());
            job = Jobs.FINDINGENEMIES;
            //System.out.println("deleted enemy base");

        }
    }

    boolean senseSurrounded() throws GameActionException {
        if(!enemyHQ.equals(Lib.noLoc)){
            int i = 0;
            for(Direction dir : Lib.directions){
                if(rc.canSenseLocation(enemyHQ.add(dir))) {
                    if(!rc.getLocation().equals(enemyHQ.add(dir))) {
                        if (!rc.sensePassability(enemyHQ.add(dir)) || rc.senseRobotAtLocation(enemyHQ.add(dir)) != null) {
                            i++;
                        }
                    }
                }
            }
            return i == 8;
        }
        return false;
    }

    void circleBase(){
        if(rc.getLocation().distanceSquaredTo(myHQ) < 8){
            dirGoing = myHQ.directionTo(rc.getLocation());
        }
        if(rc.getLocation().distanceSquaredTo(myHQ) > 22){
            dirGoing = dirGoing.rotateRight();
        }
        turnsJob++;
        if(turnsJob > 50){
            job = Jobs.FINDINGENEMIES;
            turnsJob = 0;
        }
    }
}
