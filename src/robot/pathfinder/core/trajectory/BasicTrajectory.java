package robot.pathfinder.core.trajectory;

import robot.pathfinder.core.RobotSpecs;
import robot.pathfinder.core.TrajectoryParams;
import robot.pathfinder.core.Waypoint;
import robot.pathfinder.core.path.Path;
import robot.pathfinder.math.MathUtils;
import robot.pathfinder.math.Vec2D;

/**
 * A class that represents a basic trajectory (motion profile).
 * <p>
 * Trajectories generated by this class assume a hypothetical robot that has limited speed and acceleration,
 * but unlimited jerk, and can move in any direction without slowing down. In reality, because most drivetrains
 * have to slow down when turning, the basic trajectory cannot be used. However, the generation of more advanced
 * trajectories rely on a base provided by this class.
 * </p>
 * @see TankDriveTrajectory
 * @author Tyler Tian
 *
 */
public class BasicTrajectory {
	
	/*
	 * The algorithm used to generate these trajectories are based almost entirely on the algorithm from Team
	 * 254 The Cheesy Poofs. Video here: https://youtu.be/8319J1BEHwM
	 */
	
	//The path followed by this trajectory
	Path path;
	
	/*
	 * "Moments" represent a moment in time.
	 * Each moment has a position, velocity, acceleration and time. The trajectory is made of a collection of
	 * these generated Moments. Using them, at any given time we can (roughly, but closely enough) determine
	 * the position, velocity and acceleration the robot is supposed to be at.
	 */
	BasicMoment[] moments;
	
	/*
	 * Because directly lerping between angles can cause glitches when one angle is positive and the other is
	 * negative, MathUtils.lerpAngle is used. Keep an array of the headings as normalized vectors to pass to
	 * lerpAngle later speeds up the computation when retrieving moments.
	 */
	Vec2D[] headingVectors;
	
	//Keep a copy of the robot's specs and the generation parameters
	RobotSpecs robotSpecs;
	TrajectoryParams params;
	
	//These are used by TankDriveTrajectory to generate different profiles for the left and right wheels
	//They're made package-private so only TankDriveTrajectory can access them
	boolean isTank;
	//Stores the time on the path for each moment
	double[] pathT = null;
	//Stores the radius of the path for each moment
	double[] pathRadius = null;
	
	/**
	 * Creates a basic trajectory with the specified robot properties and generation parameters.
	 * <p>
	 * Note: Tank drive trajectories require extra processing in this step. If the generated trajectory is to be
	 * turned into a {@link TankDriveTrajectory}, {@link TrajectoryParams#isTank} must be set to true.
	 * </p>
	 * @param specs A {@link RobotSpecs} object with the robot's properties, such as max speed and acceleration.
	 * @param params A {@link TrajectoryParams} object with the parameters for this trajectory.
	 */
	public BasicTrajectory(RobotSpecs specs, TrajectoryParams params) {
		this.params = params;
		this.robotSpecs = specs;
		
		//Extract the fields to make the code less cluttered later
		boolean isTank = params.isTank;
		Waypoint[] waypoints = params.waypoints;
		int segmentCount = params.segmentCount;
		double alpha = params.alpha;
		double maxVelocity = specs.getMaxVelocity();
		double maxAcceleration = specs.getMaxAcceleration();
		double baseWidth = specs.getBaseWidth();
		
		//null and NaN are the default values for these fields
		//Check to see if they're set
		if(waypoints == null) {
			throw new IllegalArgumentException("Waypoints are not set, or is null");
		}
		if(Double.isNaN(alpha)) {
			throw new IllegalArgumentException("Alpha is not set, or is NaN");
		}
		if(isTank && Double.isNaN(baseWidth)) {
			throw new IllegalArgumentException("A tank trajectory must have a base with");
		}
		
		this.isTank = isTank;
		//Generate the path
		path = Path.constructPath(params.pathType, waypoints, alpha);
		if(isTank) {
			path.setBaseRadius(baseWidth / 2);
		}
		
		/*
		 * Because most parametric polynomials don't have constant speed (i.e. the magnitude of the derivative
		 * is non-constant), we use some special processing to make samples the same physical distance apart.
		 * Instead of getting positions from the path and iterating the time, we calculate the whole length
		 * of the path, and make each sample a constant length away. The time value can then be found by
		 * calling the s2T method in Path, and any special processing can be done with that.
		 */
		//Instead of iterating over t, we iterate over s, which represents the fraction of the total distance
		double s_delta = 1.0 / (segmentCount - 1);
		double totalDist = path.computePathLength(segmentCount);
		double distPerIteration = totalDist / (segmentCount - 1);
		
		//This array stores the direction of the robot at each moment
		//Directions are generated in a separate process as the velocities and accelerations
		double[] headings = new double[segmentCount];
		headingVectors = new Vec2D[segmentCount];
		
		//This array stores the theoretical max velocity at each point in this trajectory
		//This is needed for tank drive, since the robot has to slow down when turning
		//For regular basic trajectories every element of this array is set to the max velocity
		double[] maxVelocities = new double[segmentCount];
		
		if(isTank) {
			//Tank drive trajectories require extra processing as described above
			pathRadius = new double[segmentCount];
			pathT = new double[segmentCount];
			for(int i = 0; i < segmentCount; i ++) {
				//Call s2T to translate between length and time
				double t = path.s2T(s_delta * i);
				pathT[i] = t;
				
				//Use the curvature formula in multivariable calculus to figure out the curvature at this point
				//of the path
				Vec2D deriv = path.derivAt(t);
				double xDeriv = deriv.getX();
				double yDeriv = deriv.getY();
				Vec2D secondDeriv = path.secondDerivAt(t);
				double xSecondDeriv = secondDeriv.getX();
				double ySecondDeriv = secondDeriv.getY();
				double curvature = MathUtils.curvature(xDeriv, xSecondDeriv, yDeriv, ySecondDeriv);
				//The heading is generated as a by-product
				headings[i] = Math.atan2(yDeriv, xDeriv);
				headingVectors[i] = new Vec2D(xDeriv, yDeriv);
				headingVectors[i].normalize();
				
				//Since curvature is 1 / radius, we take its reciprocal to get the radius of the path at this point
				//And since the robot's speed is always positive no matter which direction we turn in,
				//the absolute value is taken
				double r = Math.abs(1 / curvature);
				/*
				 * The maximum speed for the entire robot is computed with a formula. Derivation here:
				 * Start with the equations:
				 * 1. (r - l) / b = w, where l and r are the wheel velocities, b is the base width and w (omega)
				 * is the angular velocity.
				 * 2. (l + r) / 2 = V, where l and r are the wheel velocities, and V is the overall velocity
				 * 3. w = V / R, where w is the angular velocity, V is the overall velocity, and R is 
				 * the radius of the path.
				 * 
				 * 1. Rearrange equation 1: wb = r - l, l = r - wb
				 * 2. Since we want the robot to go as fast as possible, the faster wheel has velocity Vmax
				 * 3. Assuming the right side is faster, r = Vmax, and by 1, l = Vmax - wb
				 * 4. Equation 2 becomes: (2Vmax - wb) / 2 = V
				 * 5. Substitute in equation 3, (2Vmax - (V / R)b) / 2 = V
				 * 6. Now solve for V: 2Vmax - (V / R)b = 2V, 2V + (V / R)b = 2Vmax, V(2 + b / R) = 2Vmax,
				 * V = 2Vmax / (2 + b / R), V = Vmax / (1 + b / (2R))
				 */
				double vMax = maxVelocity / (1 + baseWidth / (2 * r));
				
				//Store the signed curvature in the array to be used by TankDriveTrajectory later
				pathRadius[i] = 1.0 / curvature;
				maxVelocities[i] = vMax;
			}
		}
		else {
			//If the trajectory is just a basic trajectory, there's no need to slow down, so every point's
			//max velocity is the specified max velocity.
			for(int i = 0; i < segmentCount; i ++) {
				maxVelocities[i] = maxVelocity;
				
				//Even if the trajectory is not for tank drive robots, the heading still needs to be calculated
				double t = path.s2T(s_delta * i);
				Vec2D deriv = path.derivAt(t);
				double xDeriv = deriv.getX();
				double yDeriv = deriv.getY();
				headings[i] = Math.atan2(yDeriv, xDeriv);
				headingVectors[i] = new Vec2D(xDeriv, yDeriv);
				headingVectors[i].normalize();
			}
		}
		
		//Create the BasicMoment array and initialize first element to 0 position, velocity, acceleration and time
		moments = new BasicMoment[segmentCount];
		moments[0] = new BasicMoment(0, 0, 0, headings[0], 0);

		//Forwards pass as described in the algorithm in the video
		for(int i = 1; i < moments.length; i ++) {
			double accumulatedDist = i * distPerIteration;
			
			double theoreticalMax = maxVelocities[i];
			
			//Check if we could accelerate
			if(moments[i - 1].getVelocity() < theoreticalMax) {
				double distDiff = distPerIteration;
				
				//First, check what velocity we would reach if we were to accelerate at maximum acceleration
				//Use the kinematic equations to figure it out
				double maxVel = Math.sqrt(Math.pow(moments[i - 1].getVelocity(), 2) + 2 * maxAcceleration * distDiff);
				
				double vel;
				//Check if this velocity exceeds the max
				if(maxVel > theoreticalMax) {
					//If it's too fast, use the kinematic equations to figure out exactly how much we can accelerate
					double accel = (Math.pow(theoreticalMax, 2) - Math.pow(moments[i - 1].getVelocity(), 2)) / (2 * distDiff);
					vel = theoreticalMax;
					moments[i - 1].setAcceleration(accel);
				}
				else {
					//If it's within limits, then accelerate at max acceleration
					vel = maxVel;
					moments[i - 1].setAcceleration(maxAcceleration);
				}
				
				moments[i] = new BasicMoment(accumulatedDist, vel, 0, headings[i]);
			}
			else {
				//If not, then do not accelerate, and set the velocity to the maximum
				moments[i] = new BasicMoment(accumulatedDist, theoreticalMax, 0, headings[i]);
				moments[i - 1].setAcceleration(0);
			}
		}
		
		//Prepare for backwards pass
		moments[moments.length - 1].setVelocity(0);
		moments[moments.length - 1].setAcceleration(0);
		//Backwards pass as described in the algorithm in the video
		for(int i = moments.length - 2; i >= 0; i --) {
			//Only do processing if the velocity of this moment is greater than the next
			//i.e. we need to decelerate
			if(moments[i].getVelocity() > moments[i + 1].getVelocity()) {
			
				double distDiff = moments[i + 1].getPosition() - moments[i].getPosition();
				//Calculate max velocity like in the forwards pass
				double maxVel = Math.sqrt(Math.pow(moments[i + 1].getVelocity(), 2) + 2 * maxAcceleration * distDiff);
				
				double vel;
				//Compare with the velocity set by the forwards pass
				if(maxVel > moments[i].getVelocity()) {
					double accel = (Math.pow(moments[i].getVelocity(), 2) - Math.pow(moments[i + 1].getVelocity(), 2)) / (2 * distDiff);
					moments[i].setAcceleration(-accel);
					vel = moments[i].getVelocity();
				}
				else {
					vel = maxVel;
					moments[i].setAcceleration(-maxAcceleration);
				}
				
				moments[i].setVelocity(vel);
			}
		}
		
		for(int i = 1; i < moments.length; i ++) {
			//Now that we have the desired position, velocity and acceleration for each moment, we can solve
			//for the time using the kinematic formulas
			double dt = MathUtils.findPositiveQuadraticRoot(moments[i - 1].getAcceleration() / 2, moments[i - 1].getVelocity(), 
					-(moments[i].getPosition() - moments[i - 1].getPosition()), params.roundingLimit);
			moments[i].setTime(moments[i - 1].getTime() + dt);
		}
	}
	/*
	 * This constructor is used internally by the mirrorLeftRight, mirrorFrontBack and retrace methods.
	 * It requires pre-generated moments so it's not visible to the world.
	 */
	protected BasicTrajectory(BasicMoment[] moments, Path path, RobotSpecs specs, TrajectoryParams params, double[] pathT, double[] pathRadius) {
		this.moments = moments;
		this.path = path;
		this.isTank = params.isTank;
		this.pathT = pathT;
		this.pathRadius = pathRadius;
		this.robotSpecs = specs;
		this.params = params;
		
		headingVectors = new Vec2D[moments.length];
		for(int i = 0; i < moments.length; i ++) {
			headingVectors[i] = new Vec2D(Math.cos(moments[i].getHeading()), Math.sin(moments[i].getHeading()));
		}
	}
	
	/**
	 * Retrieves the interal {@link Path} followed by this trajectory.
	 * @return The {@link Path} followed by this trajectory
	 */
	public Path getPath() {
		return path;
	}
	
	/**
	 * Retrieves the array of moment objects generated by this trajectory. 
	 * @return The array of moment objects generated by this trajectory
	 */
	public BasicMoment[] getMoments() {
		return moments;
	}
	
	/**
	 * Retrieves the total time it takes to complete this trajectory.
	 * @return The total amount of time it takes to drive this trajectory
	 */
	public double totalTime() {
		return moments[moments.length - 1].getTime();
	}
	
	/**
	 * Retrieves the moment object associated with the specified time. 
	 * @param t
	 * @return
	 */
	public BasicMoment get(double t) {
		//Do binary search to find the closest approximation
		int start = 0;
		int end = moments.length - 1;
		int mid;
		
		//If t is greater than the entire length in time of the left side, return the last BasicMoment
		if(t >= moments[moments.length - 1].getTime())
			return moments[moments.length - 1];
		
		while(true) {
			mid = (start + end) / 2;
			
			double midTime = moments[mid].getTime();
			
			if(midTime == t)
				return moments[mid].clone();
			//If we reached the end, return the end
			if(mid == moments.length - 1)
				return moments[mid].clone();
			
			double nextTime = moments[mid + 1].getTime();
			
			if(midTime <= t && nextTime >= t) {
				
				double f = (t - midTime) / (nextTime - midTime);
				return new BasicMoment(MathUtils.lerp(moments[mid].getPosition(), moments[mid + 1].getPosition(), f),
						MathUtils.lerp(moments[mid].getVelocity(), moments[mid + 1].getVelocity(), f),
						MathUtils.lerp(moments[mid].getAcceleration(), moments[mid + 1].getAcceleration(), f),
						MathUtils.lerpAngle(headingVectors[mid], headingVectors[mid + 1], f), t);
			}
			//Continue the binary search if not found
			if(midTime < t) {
				start = mid;
				continue;
			}
			else if(midTime > t) {
				end = mid;
				continue;
			}
		}
	}
	
	public boolean isTank() {
		return isTank;
	}
	
	public RobotSpecs getRobotSpecs() {
		return robotSpecs;
	}
	public TrajectoryParams getGenerationParams() {
		return params;
	}
	
	public BasicTrajectory mirrorLeftRight() {
		Path newPath = path.mirrorLeftRight();
		double refAngle = params.waypoints[0].getHeading();
		
		BasicMoment[] newMoments = new BasicMoment[moments.length];
		for(int i = 0; i < newMoments.length; i ++) {
			newMoments[i] = moments[i].clone();
			newMoments[i].setHeading(MathUtils.mirrorAngle(moments[i].getHeading(), refAngle));
		}
		
		TrajectoryParams newParams = params.clone();
		newParams.waypoints = newPath.getWaypoints();
		
		double[] newPathRadius = null;
		if(pathRadius != null) {
			newPathRadius = new double[pathRadius.length];
			
			//Since every left turn becomes a right turn, the radius will also be negative at every point
			for(int i = 0; i < newPathRadius.length; i ++) {
				newPathRadius[i] = -pathRadius[i];
			}
		}
		
		return new BasicTrajectory(newMoments, newPath, robotSpecs, newParams, pathT, newPathRadius);
	}
	
	public BasicTrajectory mirrorFrontBack() {
		Path newPath = path.mirrorFrontBack();
		double refAngle = params.waypoints[0].getHeading() + Math.PI / 2;
		
		BasicMoment[] newMoments = new BasicMoment[moments.length];
		for(int i = 0; i < newMoments.length; i ++) {
			newMoments[i] = new BasicMoment(-moments[i].getPosition(), -moments[i].getVelocity(), 
					-moments[i].getAcceleration(), MathUtils.mirrorAngle(moments[i].getHeading(), refAngle),
					moments[i].getTime());
		}
		
		TrajectoryParams newParams = params.clone();
		newParams.waypoints = newPath.getWaypoints();
		
		return new BasicTrajectory(newMoments, newPath, robotSpecs, newParams, pathT, pathRadius.clone());
	}
	
	public BasicTrajectory retrace() {
		Path newPath = path.retrace();
		
		BasicMoment[] newMoments = new BasicMoment[moments.length];
		BasicMoment lastMoment = moments[moments.length - 1];
		for(int i = 0; i < newMoments.length; i ++) {
			BasicMoment currentMoment = moments[moments.length - 1 - i];
			
			newMoments[i] = new BasicMoment(-(lastMoment.getPosition() - currentMoment.getPosition()), 
					-currentMoment.getVelocity(), currentMoment.getAcceleration(), 
					(currentMoment.getHeading() + Math.PI) / (2 * Math.PI), 
					lastMoment.getTime() - currentMoment.getTime());
		}
		
		TrajectoryParams newParams = params.clone();
		newParams.waypoints = newPath.getWaypoints();
		
		double[] newPathRadius = null;
		double[] newPathT = null;
		if(pathRadius != null) {
			newPathRadius = new double[pathRadius.length];
			newPathT = new double[pathT.length];
			
			//Since now we start from the end, the order of these are reversed
			for(int i = 0; i < newPathRadius.length; i ++) {
				newPathRadius[i] = pathRadius[pathRadius.length - 1 - i];
				newPathT[i] = pathT[pathT.length - 1 - i];
			}
		}
		
		return new BasicTrajectory(newMoments, newPath, robotSpecs, newParams, newPathT, newPathRadius);
	}
}


