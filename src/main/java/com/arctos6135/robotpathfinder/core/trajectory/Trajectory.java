package com.arctos6135.robotpathfinder.core.trajectory;

import com.arctos6135.robotpathfinder.core.TrajectoryParams;
import com.arctos6135.robotpathfinder.core.RobotSpecs;
import com.arctos6135.robotpathfinder.core.lifecycle.JNIObject;
import com.arctos6135.robotpathfinder.core.path.Path;

/**
 * A class that represents a trajectory (motion profile).
 * <p>
 * This is the abstract class that is the superclass of all trajectory classes.
 * A trajectory not only defines the path the robot will go through, it also
 * provides information about the velocity, acceleration and direction at every
 * point in time. Using this information, a robot can implement a feedback loop
 * to follow this trajectory.
 * </p>
 * <h2>Technical Details</h2>
 * <p>
 * Trajectories are generated using numerical integration. This means that it is
 * impossible to have a completely accurate trajectory. However, with enough
 * segments, the error is easily negligible. Trajectories are generated with an
 * algorithm based on the one shown by Team 254 (The Cheesy Poofs) in their
 * video on motion profiling.
 * </p>
 * <h2>Memory Management</h2>
 * <p>
 * Each Trajectory has a Java part (the object itself) and a part that resides
 * in native code (stored as a pointer casted into a {@code long}). Because
 * these objects contain handles to native resources that cannot be
 * automatically released by the JVM, the {@link #free()} or {@link #close()}
 * method must be called to free the native resource when the object is no
 * longer needed.
 * </p>
 * <p>Note: Almost all RobotPathfinder JNI classes have some kind of reference
 * counting. However, this reference count is only increased when an object is
 * created or copied by a method, and not when the reference is copied through
 * assignment.
 * For example:</p>
 * 
 * <pre>
 * Path p0 = someTrajectory.getPath();
 * Path p1 = someTrajectory.getPath();
 * p0.free();
 * p1.at(0); // This is valid, because the native resource was never freed due to
 *           // reference counting
 * </pre>
 * 
 * But:
 * 
 * <pre>
 * Path p0 = someTrajectory.getPath();
 * Path p1 = p0;
 * p0.free();
 * p1.at(0); // This will throw an IllegalStateException, since the native resource has
 *           // already been freed
 * </pre>
 * 
 * @author Tyler Tian
 * @since 3.0.0
 */
public abstract class Trajectory extends JNIObject {

    RobotSpecs specs;
    TrajectoryParams params;

    /**
     * {@inheritDoc}
     */
    @Override
    public void free() {
        super.free();
        clearMomentsCache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();
        clearMomentsCache();
    }

    // Native
    abstract protected int _getMomentCount();

    // Native
    abstract protected void _getMoments();

    /**
     * Retrieves all the {@link Moment}s generated by this trajectory.
     * <p>
     * Because the moments have to be retrieved from a native object, when this
     * method is first called, the moments are retrieved and cached so that future
     * calls to this method will be faster. To free the cache and reclaim the
     * memory, use {@link #clearMomentsCache()}.
     * </p>
     * 
     * @return An array of {@link Moment}s generated by this trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    abstract public Moment[] getMoments();

    /**
     * Clears the cached moments from {@link #getMoments()}.
     */
    abstract public void clearMomentsCache();

    // Native
    abstract protected Moment _get(double t);

    /**
     * Retrieves the {@link Moment} associated with the specified time.
     * <p>
     * Because it's impossible to have an infinite number of {@link Moment}s,
     * there's a good chance that the specified time does not have a corresponding
     * moment readily available. In this case, the result is generated from linearly
     * interpolating between the two {@link Moment}s that have a times closest to
     * the specified time.
     * </p>
     * 
     * @param t The time
     * @return The {@link Moment} associated with the given time
     * @throws IllegalArgumentException If the specified time is infinite or NaN
     * @throws IllegalStateException    If the native resource has already been
     *                                  freed (see class JavaDoc)
     */
    public Moment get(double t) {
        if (Double.isNaN(t) || !Double.isFinite(t)) {
            throw new IllegalArgumentException("Time must be finite and not NaN");
        }
        return _get(t);
    }

    // Native
    abstract protected long _getPath();

    /**
     * Retrieves the internal {@link Path} followed by this trajectory.
     * 
     * @return The {@link Path} followed by this trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    public Path getPath() {
        Path path = new Path(params.waypoints, params.alpha, params.pathType, _getPath());
        path.setBaseRadius(specs.getBaseWidth() / 2);
        path._updateWaypoints();
        return path;
    }

    /**
     * Retrieves the {@link RobotSpecs} object used to generate this trajectory.
     * <p>
     * Note that this method may return {@code null} if the trajectory was not
     * generated by normal means (e.g. with the {@link TrajectoryGenerator}).
     * </p>
     * 
     * @return The {@link RobotSpecs} object used to generate this trajectory
     */
    public RobotSpecs getRobotSpecs() {
        return specs;
    }

    /**
     * Retrieves the {@link TrajectoryParams} object used to generate this
     * trajectory.
     * <p>
     * Note that this method may return {@code null} if the trajectory was not
     * generated by normal means (e.g. with the {@link TrajectoryGenerator}).
     * </p>
     * 
     * @return The {@link TrajectoryParams} object used to generate this trajectory
     */
    public TrajectoryParams getGenerationParams() {
        return params;
    }

    /**
     * Retrieves the total amount of time it takes to drive out this trajectory.
     * 
     * @return The total time needed to drive this trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    public abstract double totalTime();

    /**
     * Creates a new {@link Trajectory} in which every left turn becomes a right
     * turn.
     * <p>
     * Using this method is faster than creating a new trajectory.
     * </p>
     * <p>
     * Note that the trajectory generated by this method will carry the same
     * {@link RobotSpecs} and {@link TrajectoryParams} as original trajectory (e.g.
     * The change in waypoints is not reflected in the new trajectory's
     * {@link TrajectoryParams}).
     * </p>
     * 
     * @return The new trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    abstract public Trajectory mirrorLeftRight();

    /**
     * Creates a new {@link Trajectory} in which the direction of driving is
     * reversed (i.e. driving forwards is now driving backwards).
     * <p>
     * It is impossible to create a trajectory in which the robot drives backwards
     * without using this method or {@link #retrace()}.
     * </p>
     * <p>
     * Note that the trajectory generated by this method will carry the same
     * {@link RobotSpecs} and {@link TrajectoryParams} as original trajectory (e.g.
     * The change in waypoints is not reflected in the new trajectory's
     * {@link TrajectoryParams}).
     * </p>
     * 
     * @return The new trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    abstract public Trajectory mirrorFrontBack();

    /**
     * Creates a new {@link Trajectory}, which, if driven out from the end of the
     * original trajectory, will retrace the steps of the original trajectory
     * exactly and return to where the original trajectory started.
     * <p>
     * It is impossible to create a trajectory in which the robot drives backwards
     * without using this method or {@link #mirrorFrontBack()}.
     * </p>
     * <p>
     * Note that the trajectory generated by this method will carry the same
     * {@link RobotSpecs} and {@link TrajectoryParams} as original trajectory (e.g.
     * The change in waypoints is not reflected in the new trajectory's
     * {@link TrajectoryParams}).
     * </p>
     * 
     * @return The new trajectory
     * @throws IllegalStateException If the native resource has already been freed
     *                               (see class JavaDoc)
     */
    abstract public Trajectory retrace();
}
