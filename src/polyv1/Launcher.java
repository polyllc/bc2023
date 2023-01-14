package polyv1;

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
        if(rc.getRoundNum() > 300){
            if(rc.getRoundNum() % 2 == 0){
                job = Jobs.DEFENDINGBASE;
            }
        }
    }

    enum Jobs {
        FINDINGENEMIES,
        SURROUNDINGBASE,
        KILLINGENEMIES, //pretty sure this will be after all of the bases are surrounded so they can do whatever
        DESTROYINGANCHOR,
        DEFENDINGBASE //all hands on deck!
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
        }

        attack();
        if(job == Jobs.FINDINGENEMIES){
            if(targetLoc == Lib.noLoc) {
                //find enemy bases, we'll surround them
                //whatever is first, anchor or base
                //if anchor, we destroy
                for(RobotInfo robot : lib.getRobots()){
                    if(robot.getTeam() != rc.getTeam()){
                        if(robot.getType() == RobotType.HEADQUARTERS){
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

        //statusReport(); we'll do status reports when something notable comes up

    }

    void move() throws GameActionException {
        if(lib.detectCorner(dirGoing)){
            //dirGoing = rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2));
            dirGoing = dirGoing.opposite();
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
        System.out.println("targetLoc: " + targetLoc +
                "\njob: " + job +
                "\ndirGoing: " + dirGoing +
                "\nposition: " + rc.getLocation() +
                "\n");
    }

    void attack() throws GameActionException {
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
                targetLoc = Lib.noLoc;
                dirGoing = Lib.directions[(int) Math.floor(Math.random() * 8)];
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
