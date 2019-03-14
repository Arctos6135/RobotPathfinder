package com.arctos6135.robotpathfinder.core.path;

import com.arctos6135.robotpathfinder.core.Waypoint;
import com.arctos6135.robotpathfinder.core.lifecycle.GlobalLifeCycleManager;
import com.arctos6135.robotpathfinder.core.lifecycle.JNIObject;
import com.arctos6135.robotpathfinder.math.Vec2D;
import com.arctos6135.robotpathfinder.util.Pair;

/**
 * A class that represents a path for the robot to follow. Paths are different
 * from trajectories; they only contain information about the locations the
 * robot will pass through. To follow a path, use a trajectory. This class is
 * the superclass of all path classes.
 * 
 * @author Tyler Tian
 * @since 3.0.0
 */
public class Path extends JNIObject {

    static {
        GlobalLifeCycleManager.initialize();
    }

    private native void _construct(Waypoint[] waypoints, double alpha, int type);

    protected PathType type;
    protected Waypoint[] waypoints;
    protected double alpha;

    /**
     * Creates a new {@link Path} with the specified waypoints, alpha, and type.
     * <p>
     * Alpha is the turn smoothness constant. A lower value will result in a
     * relatively shorter path with sharper turns <em>at the waypoints</em>, and a
     * higher value will result in a relatively longer path with smoother turns
     * <em>at the waypoints</em>. However, since the turns are only smoothed near
     * the waypoints, increasing this value too much can result in unwanted sharp
     * turns between waypoints.
     * </p>
     * 
     * @param waypoints The waypoints this path must pass through
     * @param alpha     The turn smoothness constant
     * @param type      The type of the path
     */
    public Path(Waypoint[] waypoints, double alpha, PathType type) {
        this.type = type;
        this.waypoints = waypoints;
        this.alpha = alpha;
        if (waypoints.length < 2) {
            throw new IllegalArgumentException("Not enough waypoints");
        }

        _construct(waypoints, alpha, type.getJNIID());
        GlobalLifeCycleManager.register(this);
    }

    public Path(Waypoint[] waypoints, double alpha, PathType type, long ptr) {
        this.waypoints = waypoints;
        this.alpha = alpha;
        this.type = type;
        _nativePtr = ptr;
        GlobalLifeCycleManager.register(this);
    }

    protected native void _destroy();

    private native void _setBaseRadius(double radius);

    private native void _setBackwards(boolean backwards);

    protected double radius;
    protected boolean backwards = false;

    public void setBaseRadius(double radius) {
        this.radius = radius;
        _setBaseRadius(radius);
    }

    public void setDrivingBackwards(boolean backwards) {
        this.backwards = backwards;
        _setBackwards(backwards);
    }

    public double getBaseRadius() {
        return radius;
    }

    public boolean getDrivingBackwards() {
        return backwards;
    }

    public Waypoint[] getWaypoints() {
        return waypoints;
    }

    public double getAlpha() {
        return alpha;
    }

    public native Vec2D at(double time);

    public native Vec2D derivAt(double time);

    public native Vec2D secondDerivAt(double time);

    public native Pair<Vec2D, Vec2D> wheelsAt(double time);

    private native double _computeLen(int points);

    private native double _s2T(double s);

    private native double _t2S(double t);

    protected double length = Double.NaN;

    public double computeLen(int points) {
        length = _computeLen(points);
        return length;
    }

    public double getLength() {
        if (length == Double.NaN) {
            throw new IllegalStateException("Length has not been computed");
        }
        return length;
    }

    public double s2T(double s) {
        if (length == Double.NaN) {
            throw new IllegalStateException("Length has not been computed");
        }
        return _s2T(s);
    }

    public double t2S(double t) {
        if (length == Double.NaN) {
            throw new IllegalStateException("Length has not been computed");
        }
        return _t2S(t);
    }

    private native long _mirrorLeftRight();

    private native long _mirrorFrontBack();

    private native long _retrace();

    public native void _updateWaypoints();

    public Path mirrorLeftRight() {
        Path p = new Path(waypoints, alpha, type, _mirrorLeftRight());
        p.backwards = backwards;
        p.radius = radius;
        p.waypoints = new Waypoint[waypoints.length];
        p._updateWaypoints();
        return p;
    }

    public Path mirrorFrontBack() {
        Path p = new Path(waypoints, alpha, type, _mirrorFrontBack());
        p.backwards = !backwards;
        p.radius = radius;
        p.waypoints = new Waypoint[waypoints.length];
        p._updateWaypoints();
        return p;
    }

    public Path retrace() {
        Path p = new Path(waypoints, alpha, type, _retrace());
        p.backwards = !backwards;
        p.radius = radius;
        p.waypoints = new Waypoint[waypoints.length];
        p._updateWaypoints();
        return p;
    }
}
