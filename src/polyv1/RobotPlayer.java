package polyv1;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int ECInt = 0;
    static int POInt = 0;
    static int SLInt = 0;
    static int MUInt = 0;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        Headquarters hq = null;
        Carrier ca = null;
        Launcher la = null;
        Destabilizer de = null;
        Booster bo = null;
        Amplifier am = null;

        hq = new Headquarters(rc);
        ca = new Carrier(rc);
        la = new Launcher(rc);
        de = new Destabilizer(rc);
        bo = new Booster(rc);
        am = new Amplifier(rc);


        turnCount = 0;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch(rc.getType()) {
                    case HEADQUARTERS: hq.takeTurn(); break;
                    case CARRIER: ca.takeTurn(); break;
                    case LAUNCHER: la.takeTurn(); break;
                    case DESTABILIZER: de.takeTurn(); break;
                    case BOOSTER: bo.takeTurn(); break;
                    case AMPLIFIER: am.takeTurn(); break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }


}
