

import javax.swing.JFrame;

import com.arctos6135.robotpathfinder.core.JNITrajectoryParams;
import com.arctos6135.robotpathfinder.core.JNIWaypoint;
import com.arctos6135.robotpathfinder.core.RobotSpecs;
import com.arctos6135.robotpathfinder.core.TrajectoryParams;
import com.arctos6135.robotpathfinder.core.Waypoint;
import com.arctos6135.robotpathfinder.core.WaypointEx;
import com.arctos6135.robotpathfinder.core.path.PathType;
import com.arctos6135.robotpathfinder.core.trajectory.BasicTrajectory;
import com.arctos6135.robotpathfinder.core.trajectory.JNIBasicTrajectory;
import com.arctos6135.robotpathfinder.core.trajectory.TankDriveTrajectory;
import com.arctos6135.robotpathfinder.core.trajectory.TrajectoryGenerator;
import com.arctos6135.robotpathfinder.tools.Grapher;

public class DebugTests {
    
    public static void test18() {
        RobotSpecs robotSpecs = new RobotSpecs(5.0, 3.5, 2.0);
        TrajectoryParams params = new TrajectoryParams();
        params.waypoints = new Waypoint[] {
                new WaypointEx(0.0, 0.0, Math.PI / 4, 2.0),
                new WaypointEx(25.0, 25.0, Math.PI / 2, 0),
                new WaypointEx(40.0, 25.0, -Math.PI / 2, 2.0),
        };
        params.alpha = 40.0;
        params.segmentCount = 1000;
        params.isTank = true;
        params.pathType = PathType.QUINTIC_HERMITE;
        BasicTrajectory trajectory = new BasicTrajectory(robotSpecs, params);

        JFrame f1 = Grapher.graphPath(trajectory.getPath(), 0.01);
        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f1.setVisible(true);
        JFrame f2 = Grapher.graphTrajectory(trajectory, 0.01);
        f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f2.setVisible(true);
    }
    
    public static void test19() {
        RobotSpecs robotSpecs = new RobotSpecs(5.0, 3.5, 3.0);

        TankDriveTrajectory traj = TrajectoryGenerator.generateRotationTank(robotSpecs, -Math.PI);

        JFrame f1 = Grapher.graphTrajectory(traj, 0.001, true);
        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f1.setVisible(true);
    }

	public static void main(String[] args) throws Exception {
		RobotSpecs robotSpecs = new RobotSpecs(5.0, 3.5, 2.0);
        TrajectoryParams params = new TrajectoryParams();
        JNITrajectoryParams jniParams = new JNITrajectoryParams();
        params.waypoints = new Waypoint[] {
            new Waypoint(0.0, 0.0, Math.PI / 4),
            new Waypoint(25.0, 25.0, Math.PI / 2),
            new Waypoint(40.0, 25.0, -Math.PI / 2),
        };
        jniParams.waypoints = new JNIWaypoint[] {
            new JNIWaypoint(0.0, 0.0, Math.PI / 4),
            new JNIWaypoint(25.0, 25.0, Math.PI / 2),
            new JNIWaypoint(40.0, 25.0, -Math.PI / 2),
        };
        params.alpha = jniParams.alpha = 40.0;
        params.segmentCount = jniParams.segmentCount = 500;
        params.isTank = jniParams.isTank = true;
        params.pathType = jniParams.pathType = PathType.QUINTIC_HERMITE;
        long nanos1 = System.nanoTime();
        JNIBasicTrajectory trajectory = new JNIBasicTrajectory(robotSpecs, jniParams);
        long nanos2 = System.nanoTime();
        BasicTrajectory trajectory2 = new BasicTrajectory(robotSpecs, params);
        long nanos3 = System.nanoTime();
        trajectory.close();
        System.out.println("I AM ALIVE!");

        System.out.println("Native\t" + (nanos2 - nanos1) / 1000 + "us");
        System.out.println("Java\t" + (nanos3 - nanos2) / 1000 + "us");
	}
}
