package polyv1;

import battlecode.common.*;

import java.util.*;

public class Navigation {
    RobotController rc;
    public Navigation(RobotController robot){
        rc = robot;
    }
    //this is the navigation class that we create an object of for each robot to keep our nav code nice and neat

    public void tryMove(Direction dir) throws GameActionException {
        if(rc.canMove(dir)){
            rc.move(dir);
        }
    }


    MapLocation newCoord(MapLocation coords) throws GameActionException {
        List<MapLocation> bfs = getBfs(coords);
        return bfs.get(0);
    }

    List<MapLocation> getBfs(MapLocation coords) throws GameActionException {
        Map<MapLocation, MapLocation> bfsLookup = new HashMap<>();
        Queue<MapLocation> toLookFor = new LinkedList<>();
        List<MapLocation> correctPath = new ArrayList<>();
        List<MapLocation> searched = new ArrayList<>();
        MapLocation currentPos;
        toLookFor.add(rc.getLocation());
        while(!bfsLookup.containsKey(coords) && toLookFor.size() > 0) {
            currentPos = toLookFor.remove();
            if(!searched.contains(currentPos)) {
                for(Direction dir : Lib.directions) {
                    if(!bfsLookup.containsKey(currentPos.add(dir))) {
                        if(rc.canSenseLocation(currentPos.add((dir)))) {
                            bfsLookup.put(currentPos.add(dir), currentPos);
                            toLookFor.add(currentPos.add(dir));
                        }
                        else{ //should we really do this? maybe what we could do is fill in the map and then add it here, but again, I think that just making a nav system
                            //that pretty much just moves to its target while just finding out on the fly how to avoid obstacles would be best (walls are not fun to do)
                            bfsLookup.put(currentPos.add(dir), currentPos);
                            toLookFor.add(currentPos.add(dir));
                        }
                    }
                }
                searched.add(currentPos);
            }
        }
        //todo, most likely the coords aren't contained here because it's out of reach, so just to the one that's closest to the coords
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
