package robot.pathfinder.follower;

import robot.pathfinder.core.trajectory.TankDriveMoment;
import robot.pathfinder.core.trajectory.TankDriveTrajectory;
import robot.pathfinder.math.MathUtils;

/**
 * A follower class for tank drive robots and trajectories.
 * <p>
 * Followers are classes that can be given parameters to follow a specific trajectory.
 * Tank drive follower does so by using a feedback loop, consisting of 5 gains: velocity feedforward,
 * acceleration feedforward, optional proportional gain, optional derivative gain, and optional 
 * directional-proportional gain.
 * </p>
 * @author Tyler Tian
 *
 */
public class TankFollower extends Follower {

	TankDriveTrajectory traj;
	TimestampSource timer;
	DistanceSource lDistSrc, rDistSrc;
	DirectionSource directionSrc;
	Motor lMotor, rMotor;
	
	//Directional proportional gain
	double kDP = 0;
	
	//Keep track of the initial timestamp and distance measurements so we don't have to reset
	//Keep track of the error and timestamp of the last iteration to calculate the derivative
	double initTime, lastTime, lLastErr, rLastErr, lInitDist, rInitDist, initDirection;
	
	boolean running = false;
	
	//Store these as member variables so they can be accessed from outside the class for testing purposes
	double leftErr, rightErr, dirErr, leftOutput, rightOutput, leftDeriv, rightDeriv, leftVelo, rightVelo, leftAccel, rightAccel;
	
	/**
	 * Constructs a new tank drive follower. Note that since this constructor does not require distance 
	 * sources or direction sources, the trajectory following is based entirely on the feedforward terms.
	 * @param traj The trajectory to follow
	 * @param lMotor The left side motor
	 * @param rMotor The right side motor
	 * @param timer A {@link robot.pathfinder.follower.Follower.TimestampSource TimestampSource} to grab timestamps from
	 * @param kV The velocity feedforward 
	 * @param kA The acceleration feedforward
	 */
	public TankFollower(TankDriveTrajectory traj, Motor lMotor, Motor rMotor, TimestampSource timer,
			double kV, double kA) {
		setGains(kV, kA, 0, 0, 0);
		this.traj = traj;
		this.lMotor = lMotor;
		this.rMotor = rMotor;
		this.lDistSrc = null;
		this.rDistSrc = null;
		this.timer = timer;
		this.directionSrc = null;
	}
	/**
	 * Constructs a new tank drive follower. Note that since this constructor does not require a direction
	 * source, the directional-proportional term will not be used.
	 * @param traj The trajectory to follow
	 * @param lMotor The left side motor
	 * @param rMotor The right side motor
	 * @param lDistSrc A {@link robot.pathfinder.follower.Follower.DistanceSource DistanceSource} for the left motor
	 * @param rDistSrc A {@link robot.pathfinder.follower.Follower.DistanceSource DistanceSource} for the right motor
	 * @param timer A {@link robot.pathfinder.follower.Follower.TimestampSource TimestampSource} to grab timestamps from
	 * @param kV The velocity feedforward 
	 * @param kA The acceleration feedforward
	 * @param kP The proportional gain
	 * @param kD The derivative gain
	 */
	public TankFollower(TankDriveTrajectory traj, Motor lMotor, Motor rMotor, 
			DistanceSource lDistSrc, DistanceSource rDistSrc, TimestampSource timer,
			double kV, double kA, double kP, double kD) {
		setGains(kV, kA, kP, kD, 0);
		this.traj = traj;
		this.lMotor = lMotor;
		this.rMotor = rMotor;
		this.lDistSrc = lDistSrc;
		this.rDistSrc = rDistSrc;
		this.timer = timer;
		this.directionSrc = null;
	}
	/**
	 * Constructs a new tank drive follower. Note that since this constructor does not require distance
	 * sources, the proportional and derivative terms will not be used.
	 * @param traj The trajectory to follow
	 * @param lMotor The left side motor
	 * @param rMotor The right side motor
	 * @param timer A {@link robot.pathfinder.follower.Follower.TimestampSource TimestampSource} to grab timestamps from
	 * @param dirSrc A {@link robot.pathfinder.follower.Follower.DirectionSource DirectionSource} to get angle data from
	 * @param kV The velocity feedforward 
	 * @param kA The acceleration feedforward
	 * @param kDP The directional-proportional gain; for more information, see {@link #setDP(double)}
	 */
	public TankFollower(TankDriveTrajectory traj, Motor lMotor, Motor rMotor, TimestampSource timer, 
			DirectionSource dirSrc, double kV, double kA, double kDP) {
		setGains(kV, kA, 0, 0, kDP);
		this.traj = traj;
		this.lMotor = lMotor;
		this.rMotor = rMotor;
		this.lDistSrc = null;
		this.rDistSrc = null;
		this.timer = timer;
		this.directionSrc = dirSrc;
	}
	/**
	 * Constructs a new tank drive follower.
	 * @param traj The trajectory to follow
	 * @param lMotor The left side motor
	 * @param rMotor The right side motor
	 * @param lDistSrc A {@link robot.pathfinder.follower.Follower.DistanceSource DistanceSource} for the left motor
	 * @param rDistSrc A {@link robot.pathfinder.follower.Follower.DistanceSource DistanceSource} for the right motor
	 * @param timer A {@link robot.pathfinder.follower.Follower.TimestampSource TimestampSource} to grab timestamps from
	 * @param dirSrc A {@link robot.pathfinder.follower.Follower.DirectionSource DirectionSource} to get angle data from
	 * @param kV The velocity feedforward 
	 * @param kA The acceleration feedforward
	 * @param kP The proportional gain
	 * @param kD The derivative gain
	 * @param kDP The directional-proportional gain; for more information, see {@link #setDP(double)}
	 */
	public TankFollower(TankDriveTrajectory traj, Motor lMotor, Motor rMotor, DistanceSource lDistSrc,
			DistanceSource rDistSrc, TimestampSource timer, DirectionSource dirSrc, 
			double kV, double kA, double kP, double kD, double kDP) {
		setGains(kV, kA, kP, kD, kDP);
		this.traj = traj;
		this.lMotor = lMotor;
		this.rMotor = rMotor;
		this.lDistSrc = lDistSrc;
		this.rDistSrc = rDistSrc;
		this.timer = timer;
		this.directionSrc = dirSrc;
	}
	
	/**
	 * Sets the directional-proportional gain of the feedback loop. The directional-proportional gain allows
	 * the robot to better follow the trajectory by trying to follow not just the position, velocity and 
	 * acceleration, but the direction as well. The actual angle the robot is facing at a given time is 
	 * subtracted from the angle it is supposed to be facing, and then multiplied by the 
	 * directional-proportional gain and added/subtracted to the outputs of the left and right wheels.
	 * @param kDP The new directional-proportional gain
	 */
	public void setDP(double kDP) {
		this.kDP = kDP;
	}
	/**
	 * Gets the directional-proportional gain of the feedback loop. The directional-proportional gain allows
	 * the robot to better follow the trajectory by trying to follow not just the position, velocity and 
	 * acceleration, but the direction as well. The actual angle the robot is facing at a given time is 
	 * subtracted from the angle it is supposed to be facing, and then multiplied by the 
	 * directional-proportional gain and added/subtracted to the outputs of the left and right wheels.
	 * @return The directional-proportional gain
	 */
	public double getDP() {
		return kDP;
	}
	/**
	 * Sets the gains of the feedback loop.
	 * @param kV The velocity feedforward
	 * @param kA The acceleration feedforward
	 * @param kP The proportional gain
	 * @param kD The derivative gain
	 * @param kDP The directional-proportional gain
	 */
	public void setGains(double kV, double kA, double kP, double kD, double kDP) {
		setGains(kV, kA, kP, kD);
		setDP(kDP);
	}
	/**
	 * Sets the trajectory to follow.
	 * @param traj The new trajectory to follow
	 * @throws RuntimeException If the follower is running
	 */
	public void setTrajectory(TankDriveTrajectory traj) {
		if(running) {
			throw new RuntimeException("Trajectory cannot be changed when follower is running");
		}
		this.traj = traj;
	}
	/**
	 * Sets the timestamp source.
	 * @param timer The new timestamp source
	 * @throws RuntimeException If the follower is running
	 */
	public void setTimestampSource(TimestampSource timer) {
		if(running) {
			throw new RuntimeException("Timestamp Source cannot be changed when follower is running");
		}
		this.timer = timer;
	}
	/**
	 * Sets the motors.
	 * @param lMotor The left motor
	 * @param rMotor The right motor
	 * @throws RuntimeException If the follower is running
	 */
	public void setMotors(Motor lMotor, Motor rMotor) {
		if(running) {
			throw new RuntimeException("Motors cannot be changed when follower is running");
		}
		this.lMotor = lMotor;
		this.rMotor = rMotor;
	}
	/**
	 * Sets the distance sources.
	 * @param lDistSrc The left distance source
	 * @param rDistSrc The right distance source
	 * @throws RuntimeException If the follower is running
	 */
	public void setDistanceSources(DistanceSource lDistSrc, DistanceSource rDistSrc) {
		if(running) {
			throw new RuntimeException("Distance Sources cannot be changed when follower is running");
		}
		this.lDistSrc = lDistSrc;
		this.rDistSrc = rDistSrc;
	}
	
	/**
	 * {@inheritDoc} The follower is considered to be "running" if {@link #initialize()}
	 * has been called, the trajectory did not end, and {@link #stop()} has not been called.
	 */
	public boolean isRunning() {
		return running;
	}
	/**
	 * {@inheritDoc}<br>
	 * <br>
	 * If the follower is currently running, this method will do nothing.
	 */
	public void initialize() {
		if(running) {
			return;
		}
		if(lDistSrc != null && rDistSrc != null) {
			lInitDist = lDistSrc.getDistance();
			rInitDist = rDistSrc.getDistance();
		}
		if(directionSrc != null) {
			initDirection = directionSrc.getDirection();
		}
		initTime = lastTime = timer.getTimestamp();
		
		running = true;
	}
	/**
	 * {@inheritDoc}<br>
	 * <br>
	 * If the follower is not initialized (not running), this method will first call {@link #initialize()}
	 * and then perform one cycle of the control loop.
	 */
	public void run() {
		if(!running) {
			initialize();
		}
		
		//Calculate current t and time difference from last iteration
		double timestamp = timer.getTimestamp();
		double dt = timestamp - lastTime;
		double t = timestamp - initTime;
		if(t > traj.totalTime()) {
			stop();
			return;
		}
		
		TankDriveMoment m = traj.get(t);
		
		leftErr = rightErr = leftDeriv = rightDeriv = dirErr = 0;
		//Calculate errors and derivatives only if the distance sources are not null
		if(lDistSrc != null && rDistSrc != null) {
			//Calculate left and right errors
			leftErr = m.getLeftPosition() - (lDistSrc.getDistance() - lInitDist);
			rightErr = m.getRightPosition() - (rDistSrc.getDistance() -rInitDist);
			//Get the derivative of the errors
			//Subtract away the desired velocity to get the true error
			leftDeriv = (leftErr - lLastErr) / dt 
	    			- m.getLeftVelocity();
	    	rightDeriv = (rightErr - rLastErr) / dt
	    			- m.getRightVelocity();
		}
		//Calculate directional error only if the direction source is not null
		if(directionSrc != null) {
			//This angle diff will be positive if the robot needs to turn left
			dirErr = MathUtils.angleDiff(directionSrc.getDirection() - initDirection, m.getFacingRelative());
		}
		leftVelo = m.getLeftVelocity();
		rightVelo = m.getRightVelocity();
		leftAccel = m.getLeftAcceleration();
		rightAccel = m.getRightAcceleration();
    	//Calculate outputs
    	leftOutput = kA * m.getLeftAcceleration() + kV * m.getLeftVelocity()
				+ kP * leftErr + kD * leftDeriv - dirErr * kDP;
		rightOutput = kA * m.getRightAcceleration() + kV * m.getRightVelocity()
				+ kP * rightErr + kD * rightDeriv + dirErr * kDP;
		//Constrain
    	leftOutput = Math.max(-1, Math.min(1, leftOutput));
    	rightOutput = Math.max(-1, Math.min(1, rightOutput));
    	
    	lMotor.set(leftOutput);
    	rMotor.set(rightOutput);
    	
    	lastTime = t;
    	lLastErr = leftErr;
    	rLastErr = rightErr;
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		lMotor.set(0);
		rMotor.set(0);
		
		running = false;
	}
	
	/**
	 * Retrieves the last positional error of the left wheel. This value is multiplied by <b>kP</b>, and added to the left output.
	 * @return The last left positional error
	 */
	public double lastLeftError() {
		return leftErr;
	}
	/**
	 * Retrieves the last positional error of the right wheel. This value is multiplied by <b>kP</b>, and added to the right output.
	 * @return The last right positional error
	 */
	public double lastRightError() {
		return rightErr;
	}
	/**
	 * Retrieves the last directional error of the robot. This value is multiplied by <b>kDP</b>, and subtracted from the left output and added to the right output.
	 * @return The last directional error
	 */
	public double lastDirectionalError() {
		return dirErr;
	}
	/**
	 * Retrieves the last output written to the left motor.
	 * @return The last left output
	 */
	public double lastLeftOutput() {
		return leftOutput;
	}
	/**
	 * Retrieves the last output written to the right motor.
	 * @return The last right output
	 */
	public double lastRightOutput() {
		return rightOutput;
	}
	/**
	 * Retrieves the last derivative error of the left wheel. This value is multiplied by <b>kD</b>, and added to the left output.
	 * @return The last left derivative error
	 */
	public double lastLeftDerivative() {
		return leftDeriv;
	}
	/**
	 * Retrieves the last derivative error of the right wheel. This value is multiplied by <b>kD</b>, and added to the right output.
	 * @return The last right derivative error
	 */
	public double lastRightDerivative() {
		return rightDeriv;
	}
	/**
	 * Retrieves the last desired (not actual!) velocity of the left wheel. This value is multiplied by <b>kV</b>, and added to the left output.
	 * @return The last left velocity
	 */
	public double lastLeftVelocity() {
		return leftVelo;
	}
	/**
	 * Retrieves the last desired (not actual!) velocity of the right wheel. This value is multiplied by <b>kV</b>, and added to the right output.
	 * @return The last right velocity
	 */
	public double lastRightVelocity() {
		return rightVelo;
	}
	/**
	 * Retrieves the last desired (not actual!) acceleration of the left wheel. This value is multiplied by <b>kV</b>, and added to the left output.
	 * @return The last left acceleration
	 */
	public double lastLeftAcceleration() {
		return leftAccel;
	}
	/**
	 * Retrieves the last desired (not actual!) acceleration of the right wheel. This value is multiplied by <b>kV</b>, and added to the right output.
	 * @return The last right acceleration
	 */
	public double lastRightAcceleration() {
		return rightAccel;
	}

}