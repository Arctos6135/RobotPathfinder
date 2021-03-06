package com.arctos6135.robotpathfinder.core.trajectory;

import com.arctos6135.robotpathfinder.math.MathUtils;

/**
 * Moments are objects that hold information about a robot at a moment in time.
 * All Moments in RobotPathfinder inherit from this abstract class.
 * <p>
 * Moment objects contain information about the position, velocity, acceleration
 * and direction of a robot at a certain time. They're returned by trajectories
 * and motion profiles when querying a specific time.
 * </p>
 * <h2>Difference Between Heading and Facing</h2>
 * <p>
 * <em>Heading</em> refers to the direction <em>the robot is moving in</em>,
 * while <em>facing</em> refers to the direction <em>the front of the robot is
 * facing</em>. Because RobotPathfinder allows the robot to move backwards,
 * these are not necessarily the same. For example, a robot moving backwards
 * would have a heading going backwards, but the facing direction would still be
 * the front.
 * <h3>Relative And Absolute Directions</h3>
 * <p>
 * <em>Absolute</em> directions are directions relative to the positive x-axis,
 * while <em>relative</em> directions are relative to the starting position of
 * the robot. For example, a robot starting in direction &pi;/2 and is currently
 * facing the direction 0 would have an absolute facing direction of 0, but a
 * relative facing direction of -&pi;.
 * </p>
 * 
 * @author Tyler Tian
 * @see BasicMoment
 * @see TankDriveMoment
 * @since 3.0.0
 */
public abstract class Moment implements Cloneable {

	protected double heading;
	protected double initialFacing;
	protected boolean backwards = false;

	/**
	 * Retrieves the direction the robot is moving in. For more information, see the
	 * class Javadoc.
	 * 
	 * @return The heading of the robot
	 */
	public double getHeading() {
		return heading;
	}

	/**
	 * Sets the direction the robot is moving in. For more information, see the
	 * class Javadoc.
	 * 
	 * @param heading The new heading of the robot
	 */
	public void setHeading(double heading) {
		this.heading = heading;
	}

	/**
	 * Retrieves the <em>initial</em> direction the robot is <em>facing</em>. This
	 * value is used to calculate the result of {@link #getFacingRelative()}. For
	 * more information, see the class Javadoc.
	 * 
	 * @return The initial direction the robot is facing
	 */
	public double getInitialFacing() {
		return initialFacing;
	}

	/**
	 * Sets the <em>initial</em> direction the robot is <em>facing</em>. This value
	 * is used to calculate the result of {@link #getFacingRelative()}. For more
	 * information, see the class Javadoc.
	 * 
	 * @param initialFacing The initial direction the robot is facing
	 */
	public void setInitialFacing(double initialFacing) {
		this.initialFacing = initialFacing;
	}

	/**
	 * Retrieves whether the robot is driving backwards in this moment. Used to
	 * determine the value of {@link #getFacingRelative()} and
	 * {@link #getFacingAbsolute()}.
	 * 
	 * @return Whether the robot is driving backwards in this moment
	 */
	public boolean getBackwards() {
		return backwards;
	}

	/**
	 * Sets whether the robot is driving backwards in this moment. Used to determine
	 * the value of {@link #getFacingRelative()} and {@link #getFacingAbsolute()}.
	 * 
	 * @param backwards Whether the robot is driving backwards in this moment
	 */
	public void setBackwards(boolean backwards) {
		this.backwards = backwards;
	}

	/**
	 * Retrieves the direction the robot is facing, relative to the starting
	 * position of the robot. For more information, see the class Javadoc.
	 * 
	 * @return The relative facing direction of the robot
	 */
	public double getFacingRelative() {
		return MathUtils.restrictAngle(getFacingAbsolute() - initialFacing);
	}

	/**
	 * Retrieves the direction the robot is facing, relative to the positive x-axis.
	 * For more information, see the class Javadoc.
	 * 
	 * @return The absolute facing direction of the robot
	 */
	public double getFacingAbsolute() {
		return backwards ? -heading : heading;
	}
}
