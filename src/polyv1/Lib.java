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
}
