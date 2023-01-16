package poly;

import battlecode.common.*;

import java.util.Map;

public class Lib {

    RobotController rc;

    int roundNum;
    int lastRoundNum;

    int[] islandsTaken = new int[10];

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

        if(width/2 > rc.getLocation().x) { //left section
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
            if(height/2 > rc.getLocation().y) { //bottom section quadrant 4
                return 4;
            }
        }
        return 1;
    }

    public int getQuadrant(MapLocation m){
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();

        if(width/2 > m.x) { //left section
            if(height/2 <= m.y) { //top section, quadrant 2
                return 2;
            }
            if(height/2 >= m.y) { //bottom section quadrant 3
                return 3;
            }
        }
        if(width/2 <= m.x) { //right section
            if(height/2 <= m.y) { //top section, quadrant 1
                return 1;
            }
            if(height/2 > m.y) { //bottom section quadrant 4
                return 4;
            }
        }
        return 1;
    }

    public MapLocation getOrigin(int q){
        if(q == 1){
            return new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        }
        if(q == 2){
            return new MapLocation(rc.getMapWidth()/2-1, rc.getMapHeight()/2);
        }
        if(q == 3){
            return new MapLocation(rc.getMapWidth()/2-1, rc.getMapHeight()/2-1);
        }
        if(q == 4){
            return new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2-1);
        }
        return new MapLocation(0,0);
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
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR) >= 40;
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

    public boolean contains(int[] ints, int i){
        for(int j : ints){
            if(j == i){
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


    Direction[] startDirList(int index, int offset){
        Direction[] dirs = new Direction[8];
        index = (index + offset) % 8;
        for(Direction dir : directions){
            dirs[index] = dir;
            index++;
            if(index == 8){
                index = 0;
            }
        }
        return dirs;
    }

    Direction[] reverse(Direction[] dirs){ //meant for the hq
        if(getQuadrant() == 1 || getQuadrant() == 4){
            Direction[] newDirs = new Direction[dirs.length];
            int j = dirs.length-1;
            for(int i = 0; i < dirs.length; i++){
                newDirs[i] = dirs[j];
                j--;
            }
            return newDirs;
        }
        return dirs;
    }

    int dirToIndex(Direction dir){
        switch(dir){
            case NORTH: return 0;
            case NORTHEAST: return 1;
            case EAST: return 2;
            case SOUTHEAST: return 3;
            case SOUTH: return 4;
            case SOUTHWEST: return 5;
            case WEST: return 6;
            case NORTHWEST: return 7;
        }
        return 0;
    }

    MapLocation[] islandLocs;

    MapLocation[] getIslandLocs() throws GameActionException {
        if(islandLocs == null){
            int[] idx = rc.senseNearbyIslands();
            int i = 0;
            if(idx.length > 0){
                islandLocs = getFreeIslands(idx);
            }
        }
        return islandLocs;
    }

    MapLocation[] getFreeIslands(int[] indexes) throws GameActionException {
        MapLocation[] locs = new MapLocation[0];
        for(int i = 0; i < indexes.length; i++){
            locs = rc.senseNearbyIslandLocations(indexes[0]);
            if(rc.canSenseLocation(locs[0])){
                if(rc.senseTeamOccupyingIsland(indexes[i]) != rc.getTeam()){
                    System.out.println(indexes[i]);
                    i = indexes.length+1;
                }
            }
            if(i == indexes.length-1){
                locs = new MapLocation[0];
            }
        }
        return locs;
    }

    int getIsland() throws GameActionException {
        int index = 0;
        int[] indexes = rc.senseNearbyIslands();
        for (int j : indexes) {
            MapLocation[] locs = rc.senseNearbyIslandLocations(indexes[0]);
            if (rc.canSenseLocation(locs[0])) {
                if (rc.senseTeamOccupyingIsland(j) != rc.getTeam()) {
                    return j;
                }
                else {
                    if(!contains(islandsTaken, j)){
                        addIsland(j);
                    }
                }
            } else if (!contains(islandsTaken, j)) {
                return j;
            }
        }
        return index;
    }

    void addIsland(int i){
        for(int j = 0; j < islandsTaken.length; j++){
            if(islandsTaken[j] == 0){
                islandsTaken[j] = i;
                break;
            }
        }
    }

    Direction educatedGuess(MapLocation hq){
        return rc.getLocation().directionTo(new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2));
    }

    MapLocation getEnemyBase() throws GameActionException {
        if(!new MapLocation(rc.readSharedArray(0), rc.readSharedArray(1)).equals(new MapLocation(0,0))){
            return new MapLocation(rc.readSharedArray(0), rc.readSharedArray(1));
        }
        return noLoc;
    }

    MapLocation getEnemyBase(int index) throws GameActionException {
        if(!new MapLocation(rc.readSharedArray((index * 2)), rc.readSharedArray(1+(index*2))).equals(new MapLocation(0,0))){
            return new MapLocation(rc.readSharedArray((index * 2)), rc.readSharedArray(1+(index*2)));
        }
        return noLoc;
    }

    boolean getEnemyBase(MapLocation m) throws GameActionException {
        if(m.equals(getEnemyBase(0))){
            return true;
        }
        else if(m.equals(getEnemyBase(1))){
            return true;
        }
        else if(m.equals(getEnemyBase(2))){
            return true;
        }
        else if(m.equals(getEnemyBase(3))){
            return true;
        }
        return false;

    }


    MapLocation getNearestEnemy() throws GameActionException {
        MapLocation currentLowest = getEnemyBase(0);
        for(int i = 0; i < 4; i++){
            if(!getEnemyBase(i).equals(noLoc)){
                MapLocation c = getEnemyBase(i);
                if(currentLowest.distanceSquaredTo(rc.getLocation()) > c.distanceSquaredTo(rc.getLocation())){
                    currentLowest = getEnemyBase(i);
                }
            }
        }
        return currentLowest;
    }

    void writeEnemyHQ(MapLocation enemy) throws GameActionException {
        if(rc.canWriteSharedArray(0,0)) {
            if (getEnemyBase(0) == noLoc) {
                rc.writeSharedArray(0,enemy.x);
                rc.writeSharedArray(1,enemy.y);
            }
            else if (getEnemyBase(1) == noLoc) {
                rc.writeSharedArray(2,enemy.x);
                rc.writeSharedArray(3,enemy.y);
            }
            else if (getEnemyBase(2) == noLoc) {
                rc.writeSharedArray(4,enemy.x);
                rc.writeSharedArray(5,enemy.y);
            }
            else if (getEnemyBase(3) == noLoc) {
                rc.writeSharedArray(6,enemy.x);
                rc.writeSharedArray(7,enemy.y);
            }
        }
    }

    void clearEnemyHQ(MapLocation hq) throws GameActionException {
        if(rc.canWriteSharedArray(0,0)) {
            if(getEnemyBase(0).equals(hq)){
                rc.writeSharedArray(0,0);
                rc.writeSharedArray(1,0);
            }
            else if(getEnemyBase(1).equals(hq)){
                rc.writeSharedArray(2,0);
                rc.writeSharedArray(3,0);
            }
            else if(getEnemyBase(2).equals(hq)){
                rc.writeSharedArray(4,0);
                rc.writeSharedArray(5,0);
            }
            else if(getEnemyBase(3).equals(hq)){
                rc.writeSharedArray(6,0);
                rc.writeSharedArray(7,0);
            }
        }
    }


    boolean onIsland(int index) throws GameActionException {
        if(contains(rc.senseNearbyIslands(), index)) {
            MapLocation[] locs = rc.senseNearbyIslandLocations(index);
            for (MapLocation loc : locs) {
                if (rc.getLocation().equals(loc)) {
                    return true;
                }
            }
        }
        return false;
    }

    MapLocation getBase() throws GameActionException {
        if(!new MapLocation(rc.readSharedArray(32), rc.readSharedArray(33)).equals(new MapLocation(0,0))){
            return new MapLocation(rc.readSharedArray(32), rc.readSharedArray(33));
        }
        return noLoc;
    }

    MapLocation getBase(int index) throws GameActionException {
        if(!new MapLocation(rc.readSharedArray((index * 2)+32), rc.readSharedArray(33+(index*2))).equals(new MapLocation(0,0))){
            return new MapLocation(rc.readSharedArray((index * 2)+32), rc.readSharedArray(33+(index*2)));
        }
        return noLoc;
    }


    void writeHQ(MapLocation hq) throws GameActionException {
        if(rc.canWriteSharedArray(0,0)) {
            if (getBase(0) == noLoc) {
                rc.writeSharedArray(32,hq.x);
                rc.writeSharedArray(33,hq.y);
            }
            else if (getBase(1) == noLoc) {
                rc.writeSharedArray(34,hq.x);
                rc.writeSharedArray(35,hq.y);
            }
            else if (getBase(2) == noLoc) {
                rc.writeSharedArray(36,hq.x);
                rc.writeSharedArray(37,hq.y);
            }
            else if (getBase(3) == noLoc) {
                rc.writeSharedArray(38,hq.x);
                rc.writeSharedArray(39,hq.y);
            }
        }
    }

    MapLocation getNearestHQ() throws GameActionException {
        MapLocation currentLowest = getBase(0);
        for(int i = 0; i < 4; i++){
            if(!getBase(i).equals(noLoc)){
                MapLocation c = getBase(i);
                if(currentLowest.distanceSquaredTo(rc.getLocation()) > c.distanceSquaredTo(rc.getLocation())){
                    currentLowest = getBase(i);
                }
            }
        }
        return currentLowest;
    }

    int numHq() throws GameActionException {
        return (getBase(0) != noLoc ? 1 : 0) + (getBase(1) != noLoc ? 1 : 0) + (getBase(2) != noLoc ? 1 : 0) + (getBase(3) != noLoc ? 1 : 0);
    }

    MapLocation[] getHqs() throws GameActionException {
        MapLocation[] locs = new MapLocation[numHq()];
        for(int i = 0; i < locs.length; i++){
            locs[i] = getBase(i);
        }
        return locs;
    }

}
