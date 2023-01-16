package poly;

import battlecode.common.*;

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
    }
    //todo, maybe once after like round something, group up all of the launchers and attack at once???
    enum Jobs {
        FINDINGENEMIES,
        SURROUNDINGBASE,
        KILLINGENEMIES, //pretty sure this will be after all of the bases are surrounded so they can do whatever
        DESTROYINGANCHOR,
        DEFENDINGBASE, //all hands on deck!
        REPORTINGBASE,
        REPORTSURROUNDED
    }

    Direction dirGoing = Direction.CENTER;
    MapLocation targetLoc = Lib.noLoc;

    public void takeTurn() throws GameActionException {
        if(dirGoing != Direction.CENTER){
            turnInDir++;
            if(turnInDir > 65){
                dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
            }
        }
        if(rc.isMovementReady()){
            move();
        }
        if(startedRound == rc.getRoundNum() || startedRound+1 == rc.getRoundNum()){
            for(RobotInfo robot : lib.getRobots()){
                if(robot.getType() == RobotType.HEADQUARTERS && rc.getTeam() == robot.getTeam()){
                    myHQ = robot.getLocation();
                    dirGoing = lib.educatedGuess(myHQ); //opposite of hq dir
                }
            }
            //todo, send an amplifier with them so they can remove the enemy hq from the list that are needed to be surrounded
            if(lib.getEnemyBase() != Lib.noLoc){
                targetLoc = lib.getEnemyBase();
                job = Jobs.SURROUNDINGBASE;
            }
        }

        attack();
        if(job == Jobs.FINDINGENEMIES){
            if(targetLoc == Lib.noLoc) {
                //find enemy bases, we'll surround them
                //whatever is first, anchor or base
                //if anchor, we destroy
                if(lib.getEnemyBase() != Lib.noLoc){
                    enemyHQ = lib.getEnemyBase();
                    targetLoc = enemyHQ;
                    job = Jobs.SURROUNDINGBASE;
                }
                for(RobotInfo robot : lib.getRobots()){
                    if(robot.getTeam() != rc.getTeam()){
                        if(robot.getType() == RobotType.HEADQUARTERS){
                            enemyHQ = robot.getLocation();
                            if(rc.getRoundNum() > 223){ //todo, make sure only like a couple do this
                                targetLoc = myHQ;
                                job = Jobs.REPORTINGBASE;
                                break;
                            }
                            targetLoc = robot.getLocation();
                            job = Jobs.SURROUNDINGBASE;

                        }
                    }
                }
            }
            if(targetLoc != Lib.noLoc){
                //essentially, once near an enemy base, surround it!
                //also hit enemy amplifiers
            }
        }

        if(job == Jobs.SURROUNDINGBASE){
            if(targetLoc != Lib.noLoc){
                if(rc.canSenseLocation(targetLoc)){
                    if(rc.canSenseRobotAtLocation(targetLoc)){
                        if(rc.senseRobotAtLocation(targetLoc) == null){
                            lib.clearEnemyHQ(targetLoc);
                            job = Jobs.FINDINGENEMIES;
                        }
                        else if(rc.senseRobotAtLocation(targetLoc).getType() != RobotType.HEADQUARTERS){ //so no exception occurs
                            lib.clearEnemyHQ(targetLoc);
                            job = Jobs.FINDINGENEMIES;
                        }
                    }
                }
                surroundEnemyBase();
            }
            else {
                job = Jobs.FINDINGENEMIES;
            }
        }

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
                lib.writeEnemyHQ(enemyHQ);
                job = Jobs.SURROUNDINGBASE;
                targetLoc = enemyHQ;
            }
        }

        if(job == Jobs.REPORTSURROUNDED){ //to be honest, i'm not sure if this works
            if(rc.getLocation().distanceSquaredTo(myHQ) < 9){
                lib.clearEnemyHQ(enemyHQ);
                job = Jobs.SURROUNDINGBASE;
                targetLoc = Lib.noLoc;
                dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
                enemyHQ = Lib.noLoc;
            }
        }

        statusReport();

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
        rc.setIndicatorString("targetLoc: " + targetLoc +
                "\njob: " + job +
                "\ndirGoing: " + dirGoing +
                "\nposition: " + rc.getLocation() +
                "\n");
    }

    void attack() throws GameActionException {
        //todo, attack taken islands
        for(RobotInfo robot : lib.getRobots()){
            if(robot.getTeam() != rc.getTeam()){
                if(robot.getType() == RobotType.AMPLIFIER){ //priority
                    if(rc.canAttack(robot.getLocation())){
                        rc.attack(robot.getLocation());
                    }
                }
                else if(robot.getType() != RobotType.HEADQUARTERS){
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
                System.out.println(targetLoc);
            }
            if(senseSurrounded()){
                if(rc.getRoundNum() > 300 && rc.getRoundNum() % 2 == 0) {
                    targetLoc = Lib.noLoc;
                    dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
                    enemyHQ = Lib.noLoc;
                }
                else {
                    job = Jobs.REPORTSURROUNDED;
                    targetLoc = myHQ;
                }
            }
            if(rc.getLocation().equals(targetLoc)){ //meaning right next to
                stopMoving = true;
            }
            else { //get to a place around it
                for(Direction dir : Lib.directions){
                    if(rc.canSenseLocation(enemyHQ.add(dir))) {
                        if (rc.sensePassability(enemyHQ.add(dir)) && rc.senseRobotAtLocation(enemyHQ.add(dir)) == null) {
                            targetLoc = enemyHQ.add(dir); //um how well will that work? we'll see!
                        }
                    }
                }
            }
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
