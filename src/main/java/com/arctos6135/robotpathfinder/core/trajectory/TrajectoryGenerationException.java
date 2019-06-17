package com.arctos6135.robotpathfinder.core.trajectory;

/**
 * Signals that an error with trajectory generation has occurred.
 * <p>
 * This exception is thrown by the constructors of trajectories when they're
 * given impossible constraints.
 * </p>
 * 
 * @author Tyler Tian
 * @since 3.0.0
 */
public class TrajectoryGenerationException extends RuntimeException {

	/**
	 * Creates a new {@link TrajectoryGenerationException} with a message.
	 */
	private static final long serialVersionUID = 4458957792647962346L;

	public TrajectoryGenerationException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link TrajectoryGenerationException} with the default message.
	 */
	public TrajectoryGenerationException() {
		super();
	}
}
