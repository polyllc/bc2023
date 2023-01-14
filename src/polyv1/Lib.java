package polyv1;

import battlecode.common.*;

public class Lib {

    RobotController rc;

    int roundNum;
    int lastRoundNum;

    static MapLocation noLoc = new MapLocation(256,256);

    public Lib(RobotController robot){
        rc = robot;
        roundNum = rc.getRoundNum();
        lastRoundNum = roundNum--;
    }
    //pretty much any useful function or variables go here
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public int getQuadrant(){
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();

        if(width/2 >= rc.getLocation().x) { //left section
            if(height/2 <= rc.getLocation().y) { //top section, quadrant 2
                return 2;
            }
            if(height/2 >= rc.getLocation().y) { //bottom section quadrant 3
                return 3;
            }
        }
        if(width/2 <= rc.getLocation().x) { //right section
            if(height/2 <= rc.getLocation().y) { //top section, quadrant 1
                return 1;
            }
            if(height/2 >= rc.getLocation().y) { //bottom section quadrant 4
                return 4;
            }
        }
        return 1;
    }

    RobotInfo[] currentRoundRobots =  new RobotInfo[0];

    public RobotInfo[] getRobots(){
        roundNum = rc.getRoundNum();
        if(currentRoundRobots.length == 0 || lastRoundNum < roundNum){
            currentRoundRobots = rc.senseNearbyRobots();
            lastRoundNum = roundNum;
        }
        return currentRoundRobots;
    }


    public int getWeight(){
        int totalWeight = 0;
        totalWeight += rc.getResourceAmount(ResourceType.ADAMANTIUM);
        totalWeight += rc.getResourceAmount(ResourceType.ELIXIR);
        totalWeight += rc.getResourceAmount(ResourceType.MANA);
        return totalWeight;
    }

    public boolean isFullResources(){
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) >= 40){
            return true;
        }
        return false;
    }

    public boolean contains(WellInfo[] wells, WellInfo well){
        for(WellInfo w : wells){
            if(well.equals(w)){
                return true;
            }
        }
        return false;
    }

    public boolean contains(RobotInfo[] robots, RobotInfo robot){
        for(RobotInfo r : robots){
            if(robot.equals(r)){
                return true;
            }
        }
        return false;
    }

    boolean detectCorner(Direction dirGoing) throws GameActionException {
        if(rc.getLocation().equals(new MapLocation(rc.getMapWidth() - 1, rc.getMapHeight() - 1)) ||
                rc.getLocation().equals(new MapLocation(0, rc.getMapHeight() - 1)) ||
                rc.getLocation().equals(new MapLocation(rc.getMapWidth() - 1, 0)) ||
                rc.getLocation().equals(new MapLocation(0,0))){
            return true;
        }

        if(dirGoing != Direction.CENTER) {
            int[] walls = new int[8];
            int i = 0;
            for (Direction dir : directions) {
                if (rc.canSenseLocation(rc.getLocation().add(dir))) {
                    if (!rc.sensePassability(rc.getLocation().add(dir))) {
                        walls[i] = 1;
                    }
                }
                i++;
            }

            if (walls[0] == 1 && walls[1] == 1 && walls[2] == 1 && dirGoing == Direction.NORTHEAST) { //corner northeast
                return true;
            }
            if (walls[2] == 1 && walls[3] == 1 && walls[4] == 1 && dirGoing == Direction.SOUTHEAST) { //corner southeast
                return true;
            }
            if (walls[4] == 1 && walls[5] == 1 && walls[6] == 1 && dirGoing == Direction.SOUTHWEST) { //corner southwest
                return true;
            }
            if (walls[6] == 1 && walls[7] == 1 && walls[0] == 1 && dirGoing == Direction.NORTHWEST) { //corner northwest
                return true;
            }
        }

        return false;
    }
}
