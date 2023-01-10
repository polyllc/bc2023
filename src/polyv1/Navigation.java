package polyv1;

import battlecode.common.*;

import java.util.*;

public class Navigation {
    RobotController rc;
    public Navigation(RobotController robot){
        rc = robot;
    }
    Lib l = new Lib();
    //this is the navigation class that we create an object of for each robot to keep our nav code nice and neat

    MapLocation newCoord(MapLocation coords) throws GameActionException {
        List<MapLocation> bfs = getBfs(coords);
        return bfs.get(0);
    }

    List<MapLocation> getBfs(MapLocation coords) throws GameActionException {
        Map<MapLocation, MapLocation> bfsLookup = new HashMap<MapLocation, MapLocation>();
        Queue<MapLocation> toLookFor = new LinkedList<MapLocation>();
        List<MapLocation> correctPath = new ArrayList<>();
        List<MapLocation> searched = new ArrayList<>();
        MapLocation currentPos;
        toLookFor.add(rc.getLocation());
        while(!bfsLookup.containsKey(coords) && toLookFor.size() > 0) {
            currentPos = toLookFor.remove();
            if(!searched.contains(currentPos)) {
                for(Direction dir : l.directions) {
                    if(!bfsLookup.containsKey(currentPos.add(dir))) {
                        if(rc.canSenseLocation(currentPos.add((dir)))) {
                            bfsLookup.put(currentPos.add(dir), currentPos);
                            toLookFor.add(currentPos.add(dir));
                        }
                        else{
                            bfsLookup.put(currentPos.add(dir), currentPos);
                            toLookFor.add(currentPos.add(dir));
                        }
                    }
                }
                searched.add(currentPos);
            }
        }
        if(bfsLookup.containsKey(coords)) {
            currentPos = coords;
            while(currentPos != rc.getLocation()) {
                correctPath.add(currentPos);
                currentPos = bfsLookup.get(currentPos);
            }
            correctPath.add(rc.getLocation());
        }
        Collections.reverse(correctPath);
        return correctPath;

    }
}
