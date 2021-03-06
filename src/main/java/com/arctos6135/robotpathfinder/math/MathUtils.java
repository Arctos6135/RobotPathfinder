package com.arctos6135.robotpathfinder.math;

/**
 * A class containing a collection of static math utility methods.
 * 
 * @author Tyler Tian
 * @since 3.0.0
 */
public final class MathUtils {

	private MathUtils() {
	}

	/**
	 * Finds the roots of a quadratic equation in standard form.
	 * <p>
	 * If no real roots exist, the resulting array will contain {@code [NaN, NaN]};
	 * if the two roots are the same, the two elements of the array will be the same
	 * number.
	 * </p>
	 * 
	 * @param a The squared term coefficient
	 * @param b The linear term coefficient
	 * @param c The constant
	 * @return The roots of the equation
	 */
	public static double[] findQuadraticRoots(double a, double b, double c) {
		return findQuadraticRoots(a, b, c, 0);
	}

	/**
	 * Finds the roots of a quadratic equation in standard form.
	 * <p>
	 * If no real roots exist, the resulting array will contain {@code [NaN, NaN]};
	 * if the two roots are the same, the two elements of the array will be the same
	 * number.
	 * </p>
	 * <p>
	 * If the absolute value of {@code b^2-4ac} is less than or equal to the
	 * rounding limit, it will be rounded down to 0; this can help prevent the
	 * situation in which perfectly fine equations become unsolvable due to rounding
	 * issues.
	 * </p>
	 * 
	 * @param a       The squared term coefficient
	 * @param b       The linear term coefficient
	 * @param c       The constant
	 * @param minUnit The rounding limit for {@code b^2-4ac}
	 * @return The roots of the equation
	 */
	public static double[] findQuadraticRoots(double a, double b, double c, double minUnit) {
		// Special case
		if (a == 0) {
			return new double[] { -c / b, -c / b };
		}
		double d = b * b - 4 * a * c;
		if (Math.abs(d) <= minUnit)
			d = 0;
		double r = Math.sqrt(d);
		return new double[] { (-b + r) / (2 * a), (-b - r) / (2 * a), };
	}

	/**
	 * Finds the roots of a quadratic equation using
	 * {@link MathUtils#findQuadraticRoots(double, double, double)
	 * findQuadraticRoots()}, and returns the positive root.
	 * <p>
	 * If both roots are negative, this method will return {@code NaN}.
	 * </p>
	 * 
	 * @param a The squared term coefficient
	 * @param b The linear term coefficient
	 * @param c The constant
	 * @return The positive root of the quadratic equation, or if both roots are
	 *         negative, {@code NaN}.
	 */
	public static double findPositiveQuadraticRoot(double a, double b, double c) {
		double[] roots = findQuadraticRoots(a, b, c);
		if (roots[0] >= 0)
			return roots[0];
		else if (roots[1] >= 0)
			return roots[1];
		else
			return Double.NaN;
	}

	/**
	 * Finds the roots of a quadratic equation using
	 * {@link MathUtils#findQuadraticRoots(double, double, double)
	 * findQuadraticRoots()}, and returns the positive root.
	 * <p>
	 * If both roots are negative, this method will return {@code NaN}.
	 * </p>
	 * 
	 * @param a       The squared term coefficient
	 * @param b       The linear term coefficient
	 * @param c       The constant
	 * @param minUnit The rounding limit for {@code b^2-4ac}; for more information
	 *                see
	 *                {@link #findQuadraticRoots(double, double, double, double)}.
	 * @return The positive root of the quadratic equation, or if both roots are
	 *         negative, {@code NaN}.
	 */
	public static double findPositiveQuadraticRoot(double a, double b, double c, double minUnit) {
		double[] roots = findQuadraticRoots(a, b, c, minUnit);
		if (roots[0] >= 0)
			return roots[0];
		else if (roots[1] >= 0)
			return roots[1];
		else
			return Double.NaN;
	}

	/**
	 * Finds the discriminant of a cubic polynomial.
	 * <ul>
	 * <li>If this value is positive, then the polynomial has 3 distinct real
	 * roots.</li>
	 * <li>If this value is zero, then the polynomial has a multiple root and all 3
	 * roots are real.</li>
	 * <li>If this value is negative, then the polynomial has one real root, and 2
	 * complex roots.</li>
	 * </ul>
	 * 
	 * @param a The cubed term coefficient
	 * @param b The squared term coefficient
	 * @param c The linear term coefficient
	 * @param d The constant term
	 * @return The discriminant of the polynomial
	 */
	public static double cubicDiscriminant(double a, double b, double c, double d) {
		// Formula can be found on Wikipedia:
		// https://en.wikipedia.org/wiki/Cubic_function#General_formula
		return 18 * a * b * c * d - 4 * Math.pow(b, 3) * d + Math.pow(b, 2) * Math.pow(c, 2) - 4 * a * Math.pow(c, 3)
				- 27 * Math.pow(a, 2) * Math.pow(d, 2);
	}

	/**
	 * Finds the real root of a cubic polynomial.
	 * <p>
	 * This method assumes that the discriminant of the polynomial is negative, that
	 * is, the polynomial has only one real root and 2 complex roots.
	 * </p>
	 * <p>
	 * If {@code a} is 0, this returns the same result as calling
	 * {@link #findPositiveQuadraticRoot(double, double, double)
	 * findPositiveQuadraticRoot(b, c, d)}.
	 * </p>
	 * <p>
	 * If the discriminant is greater than 0, this method will return {@code NaN}.
	 * </p>
	 * 
	 * @param a The cubed term coefficient
	 * @param b The squared term coefficient
	 * @param c The linear term coefficient
	 * @param d The constant term
	 * @return The real root
	 */
	public static double realCubicRoot(double a, double b, double c, double d) {
		if (a == 0.0) {
			return findPositiveQuadraticRoot(b, c, d);
		}

		// Formula can be found on Wikipedia:
		// https://en.wikipedia.org/wiki/Cubic_function#General_formula
		double d0 = b * b - 3 * a * c;
		double d1 = 2 * Math.pow(b, 3) - 9 * a * b * c + 27 * Math.pow(a, 2) * d;
		double C;
		// "In addition either sign in front of the square root may be chosen unless d0
		// = 0
		// in which case the sign must be chosen so that the two terms inside the cube
		// root do not cancel."
		if (d0 != 0) {
			C = Math.cbrt((d1 - Math.sqrt(Math.pow(d1, 2) - 4 * Math.pow(d0, 3))) / 2);
		} else {
			if (d1 >= 0) {
				// If d1 is greater than 0, then we must add, because the result of the square
				// root is always going to be positive
				C = Math.cbrt((d1 + Math.sqrt(Math.pow(d1, 2) - 4 * Math.pow(d0, 3))) / 2);
			} else {
				// If d1 is less than 0, we must subtract
				C = Math.cbrt((d1 - Math.sqrt(Math.pow(d1, 2) - 4 * Math.pow(d0, 3))) / 2);
			}
		}

		return -1 / (3 * a) * (b + C + d0 / C);
	}

	/**
	 * Calculates the curvature, given the derivatives and the second derivatives at
	 * the desired point.
	 * 
	 * @param xDeriv       The first derivative of x, with respect to t (dx/dt).
	 * @param xSecondDeriv The second derivative of x, with respect to t
	 *                     (d^2x/dt^2).
	 * @param yDeriv       The first derivative of y, with respect to t (dy/dt).
	 * @param ySecondDeriv The second derivative of y, with respect to t
	 *                     (d^2y/dt^2).
	 * @return The curvature at the point
	 */
	public static double curvature(double xDeriv, double xSecondDeriv, double yDeriv, double ySecondDeriv) {
		return (xDeriv * ySecondDeriv - yDeriv * xSecondDeriv) / Math.pow(xDeriv * xDeriv + yDeriv * yDeriv, 3.0 / 2.0);
	}

	/**
	 * Linearly interpolates between two scalars.
	 * <p>
	 * Note due to possible discontinuities, this method should not be used if
	 * interpolating between angles; use {@link #lerpAngle(double, double, double)}
	 * instead.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @param f The fraction of the way from the first number to the second number
	 * @return The result of the lerp
	 */
	public static double lerp(double a, double b, double f) {
		return (a * (1.0 - f)) + (b * f);
	}

	/**
	 * Linearly interpolates between two angles in radians.
	 * <p>
	 * This method will choose the shortest path around the unit circle, and
	 * therefore can interpolate between positive and negative angles.
	 * </p>
	 * 
	 * @param a The first angle
	 * @param b The second angle
	 * @param f The fraction of the way from the first angle to the second angle
	 * @return The result of the lerp
	 */
	public static double lerpAngle(double a, double b, double f) {
		// Magic
        // https://stackoverflow.com/questions/2708476/rotation-interpolation
        double theta = ((a - b) % (Math.PI * 2) + Math.PI * 3) % (Math.PI * 2) - Math.PI;
        return restrictAngle(a + f * theta);
	}

	/**
	 * Linearly interpolates between two normalized vectors, each representing an
	 * angle.
	 * <p>
	 * This method will choose the shortest path around the unit circle, and
	 * therefore can interpolate between positive and negative angles.
	 * </p>
	 * 
	 * @param a The normalized direction vector for the first angle
	 * @param b The normalized direction vector for the second angle
	 * @param f The fraction of the way from the first angle to the second angle
	 * @return The result of the lerp
	 * @see #lerpAngle(double, double, double)
	 */
	public static double lerpAngle(Vec2D a, Vec2D b, double f) {
		// Done by lerping the normalized direction vector
		Vec2D angle = Vec2D.lerp(a, b, f);
		return Math.atan2(angle.getY(), angle.getX());
	}

	/**
	 * Restricts the absolute value of the input to the range {@code [0, absMax]}.
	 * The sign of the number will be kept.
	 * 
	 * @param val    The value to restrict
	 * @param absMax The maximum allowed absolute value
	 * @return {@code val} if {@code Math.abs(val) <= absMax}, or
	 *         {@code Math.copySign(Math.min(absMax, Math.abs(val)), val)} otherwise
	 */
	public static double clampAbs(double val, double absMax) {
		return Math.abs(val) <= absMax ? val : Math.copySign(Math.min(absMax, Math.abs(val)), val);
	}

	/**
	 * Converts an angle in radians to be in the range (-&pi;, &pi;].
	 * 
	 * @param theta An angle in radians
	 * @return The same angle, converted to be in the range (-&pi;, &pi;]
	 */
	public static double restrictAngle(double theta) {
		theta %= Math.PI * 2;
		if (theta <= -Math.PI) {
			theta += Math.PI * 2;
		} else if (theta > Math.PI) {
			theta -= Math.PI * 2;
		}
		return theta;
	}

	/**
	 * Reflects an angle in radians across the line represented by another angle in
	 * radians.
	 * 
	 * @param theta The angle to reflect
	 * @param ref   The angle representing the line to reflect across
	 * @return The reflected angle
	 */
	public static double mirrorAngle(double theta, double ref) {
		return restrictAngle(theta - 2 * (theta - ref));
	}

	/**
	 * Finds the difference between two angles in radians.
	 * <p>
	 * The angles should be in the range (-&pi;, &pi;]. This method will always find
	 * the least of the two possible angles.
	 * </p>
	 * 
	 * @param src    The source angle
	 * @param target The target angle
	 * @return The smaller of the two angle differences
	 */
	public static double angleDiff(double src, double target) {
		double diff = target - src;
		if (diff > Math.PI) {
			diff -= 2 * Math.PI;
		} else if (diff <= -Math.PI) {
			diff += 2 * Math.PI;
		}

		return diff;
	}

	protected static double floatCompareThreshold = 1e-7;

	/**
	 * Retrieves the floating-point comparison threshold.
	 * <p>
	 * This is the threshold value used by the float comparison methods
	 * ({@link #floatEq(double, double)}, {@link #floatGt(double, double)}, ...). It
	 * defaults to {@code 1e-7}.
	 * </p>
	 * 
	 * @return The floating-point comparison threshold
	 */
	public static double getFloatCompareThreshold() {
		return floatCompareThreshold;
	}

	/**
	 * Sets the floating-point comparison threshold.
	 * <p>
	 * This is the threshold value used by the float comparison methods
	 * ({@link #floatEq(double, double)}, {@link #floatGt(double, double)}, ...). It
	 * defaults to {@code 1e-7}.
	 * </p>
	 * 
	 * @param threshold The floating-point comparison threshold
	 */
	public static void setFloatCompareThreshold(double threshold) {
		floatCompareThreshold = threshold;
	}

	/**
	 * Compares two floating-point numbers for equality using a threshold.
	 * <p>
	 * Because floating-point values have rounding errors, directly using {@code ==}
	 * for equality is not recommended. Instead, this method checks to see if the
	 * difference between the two arguments is less than a threshold to determine
	 * whether they're roughly equal.
	 * </p>
	 * <p>
	 * The threshold used can be obtained by calling
	 * {@link #getFloatCompareThreshold()}.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @return Whether the two numbers are roughly equal
	 * @see #getFloatCompareThreshold()
	 */
	public static boolean floatEq(double a, double b) {
		return Math.abs(a - b) <= floatCompareThreshold;
	}

	/**
	 * Compares two floating-point numbers to see if one is less than or equal to
	 * the other using a threshold.
	 * <p>
	 * Because floating-point values have rounding errors, directly using {@code <=}
	 * is not recommended. Instead, this method checks to see if the difference
	 * between the two arguments is less than a threshold to determine whether
	 * they're roughly equal, or if {@code a < b}.
	 * </p>
	 * <p>
	 * Equivalent to {@link #floatEq(double, double) floatEq(a, b)}
	 * {@code || a < b}.
	 * </p>
	 * <p>
	 * The threshold used can be obtained by calling
	 * {@link #getFloatCompareThreshold()}.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @return Whether the two numbers are roughly equal, or if {@code a < b}.
	 * @see #getFloatCompareThreshold()
	 */
	public static boolean floatLtEq(double a, double b) {
		return a < b || Math.abs(a - b) <= floatCompareThreshold;
	}

	/**
	 * Compares two floating-point numbers to see if one is greater than or equal to
	 * the other using a threshold.
	 * <p>
	 * Because floating-point values have rounding errors, directly using {@code >=}
	 * is not recommended. Instead, this method checks to see if the difference
	 * between the two arguments is less than a threshold to determine whether
	 * they're roughly equal, or if {@code a > b}.
	 * </p>
	 * <p>
	 * Equivalent to {@link #floatEq(double, double) floatEq(a, b)}
	 * {@code || a > b}.
	 * </p>
	 * <p>
	 * The threshold used can be obtained by calling
	 * {@link #getFloatCompareThreshold()}.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @return Whether the two numbers are roughly equal, or if {@code a > b}.
	 * @see #getFloatCompareThreshold()
	 */
	public static boolean floatGtEq(double a, double b) {
		return a > b || Math.abs(a - b) <= floatCompareThreshold;
	}

	/**
	 * Compares two floating-point numbers to see if one is less than the other
	 * using a threshold.
	 * <p>
	 * Equivalent to {@code !}{@link #floatGtEq(double, double) floatGtEq(a, b)}.
	 * </p>
	 * <p>
	 * The threshold used can be obtained by calling
	 * {@link #getFloatCompareThreshold()}.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @return Whether {@code a} is less than {@code b}
	 * @see #getFloatCompareThreshold()
	 */
	public static boolean floatLt(double a, double b) {
		return !floatGtEq(a, b);
	}

	/**
	 * Compares two floating-point numbers to see if one is greater than the other
	 * using a threshold.
	 * <p>
	 * Equivalent to {@code !}{@link #floatLtEq(double, double) floatLtEq(a, b)}.
	 * </p>
	 * <p>
	 * The threshold used can be obtained by calling
	 * {@link #getFloatCompareThreshold()}.
	 * </p>
	 * 
	 * @param a The first number
	 * @param b The second number
	 * @return Whether {@code a} is greater than {@code b}
	 * @see #getFloatCompareThreshold()
	 */
	public static boolean floatGt(double a, double b) {
		return !floatLtEq(a, b);
	}
}
