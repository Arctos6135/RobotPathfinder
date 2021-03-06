package com.arctos6135.robotpathfinder.tests.motionprofile.followable.profiles;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import com.arctos6135.robotpathfinder.core.RobotSpecs;
import com.arctos6135.robotpathfinder.core.trajectory.TankDriveMoment;
import com.arctos6135.robotpathfinder.follower.DynamicFollowable;
import com.arctos6135.robotpathfinder.follower.Followable;
import com.arctos6135.robotpathfinder.math.MathUtils;
import com.arctos6135.robotpathfinder.motionprofile.followable.profiles.TrapezoidalTankDriveProfile;
import com.arctos6135.robotpathfinder.tests.TestHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * This class contains tests for {@link TrapezoidalTankDriveProfile}.
 * 
 * @author Tyler Tian
 */
public class TrapezoidalTankDriveProfileTest {

    @Rule
    public TestName testName = new TestName();

    /**
     * Performs basic testing on {@link TrapezoidalTankDriveProfile}.
     * 
     * This test creates a {@link TrapezoidalTankDriveProfile} and asserts that the
     * starting position and velocity and end velocity are all 0, the end position
     * is as expected, the starting acceleration is the max acceleration, and the
     * end acceleration is negative the max acceleration for each of the sides.
     */
    @Test
    public void testTrapezoidalTankDriveProfile() {
        TestHelper helper = new TestHelper(getClass(), testName);

        double maxV = helper.getDouble("maxV", 1000);
        double maxA = helper.getDouble("maxA", 1000);
        double distance = helper.getDouble("distance", 1000);

        RobotSpecs specs = new RobotSpecs(maxV, maxA);

        Followable<TankDriveMoment> f = new TrapezoidalTankDriveProfile(specs, distance);
        TankDriveMoment begin = f.get(0);
        TankDriveMoment end = f.get(f.totalTime());
        assertThat("Left Position at end time should be close to the specified position", begin.getLeftPosition(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Position at the start time should be 0", end.getLeftPosition(),
                closeTo(distance, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Velocity at the start time should be 0", begin.getLeftVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Velocity at the end time should be 0", end.getLeftVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Acceleration at the start time should be max acceleration", begin.getLeftAcceleration(),
                closeTo(maxA, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Acceleration at the end time should be negative max acceleration", end.getLeftAcceleration(),
                closeTo(-maxA, MathUtils.getFloatCompareThreshold()));

        assertThat("Right Position at end time should be close to the specified position", begin.getRightPosition(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Position at the start time should be 0", end.getRightPosition(),
                closeTo(distance, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Velocity at the start time should be 0", begin.getRightVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Velocity at the end time should be 0", end.getRightVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Acceleration at the start time should be max acceleration", begin.getRightAcceleration(),
                closeTo(maxA, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Acceleration at the end time should be negative max acceleration", end.getRightAcceleration(),
                closeTo(-maxA, MathUtils.getFloatCompareThreshold()));
    }

    /**
     * Performs basic testing on {@link TrapezoidalTankDriveProfile} using a
     * negative position.
     * 
     * This test is identical to {@link #testTrapezoidalTankDriveProfile()} except
     * that it uses a negative position to construct the
     * {@link TrapezoidalTankDriveProfile} so that it goes backwards.
     */
    @Test
    public void testTrapezoidalTankDriveProfileReversed() {
        TestHelper helper = new TestHelper(getClass(), testName);

        double maxV = helper.getDouble("maxV", 1000);
        double maxA = helper.getDouble("maxA", 1000);
        double distance = helper.getDouble("distance", -1000, 0);

        RobotSpecs specs = new RobotSpecs(maxV, maxA);

        Followable<TankDriveMoment> f = new TrapezoidalTankDriveProfile(specs, distance);
        TankDriveMoment begin = f.get(0);
        TankDriveMoment end = f.get(f.totalTime());
        assertThat("Left Position at end time should be close to the specified position", begin.getLeftPosition(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Position at the start time should be 0", end.getLeftPosition(),
                closeTo(distance, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Velocity at the start time should be 0", begin.getLeftVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Velocity at the end time should be 0", end.getLeftVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Acceleration at the start time should be negative max acceleration",
                begin.getLeftAcceleration(), closeTo(-maxA, MathUtils.getFloatCompareThreshold()));
        assertThat("Left Acceleration at the end time should be max acceleration", end.getLeftAcceleration(),
                closeTo(maxA, MathUtils.getFloatCompareThreshold()));

        assertThat("Right Position at end time should be close to the specified position", begin.getRightPosition(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Position at the start time should be 0", end.getRightPosition(),
                closeTo(distance, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Velocity at the start time should be 0", begin.getRightVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Velocity at the end time should be 0", end.getRightVelocity(),
                closeTo(0.0, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Acceleration at the start time should be negative max acceleration",
                begin.getRightAcceleration(), closeTo(-maxA, MathUtils.getFloatCompareThreshold()));
        assertThat("Right Acceleration at the end time should be max acceleration", end.getRightAcceleration(),
                closeTo(maxA, MathUtils.getFloatCompareThreshold()));
    }

    /**
     * Performs testing on many points of a {@link TrapezoidalTankDriveProfile}.
     * 
     * This test creates a {@link TrapezoidalTankDriveProfile} and loops through
     * 1000 points in time, asserting that the position is between 0 and the
     * specified end position, the velocity is between 0 and the specified max
     * velocity, and the acceleration is between the negative max acceleration and
     * max acceleration for both sides.
     */
    @Test
    public void testTrapezoidalTankDriveProfileAdvanced() {
        TestHelper helper = new TestHelper(getClass(), testName);

        double maxV = helper.getDouble("maxV", 1000);
        double maxA = helper.getDouble("maxA", 1000);
        double distance = helper.getDouble("distance", 1000);

        RobotSpecs specs = new RobotSpecs(maxV, maxA);

        Followable<TankDriveMoment> f = new TrapezoidalTankDriveProfile(specs, distance);

        double dt = f.totalTime() / 1000;
        for (double t = 0; t < f.totalTime(); t += dt) {
            TankDriveMoment m = f.get(t);
            assertThat("Left position should be within the expected range", m.getLeftPosition(),
                    either(lessThan(distance)).or(closeTo(distance, MathUtils.getFloatCompareThreshold())));
            assertThat("Left position should be within the expected range", m.getLeftPosition(),
                    either(greaterThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Left velocity should be within the expected range", m.getLeftVelocity(),
                    either(lessThan((maxV))).or(closeTo((maxV), MathUtils.getFloatCompareThreshold())));
            assertThat("Left velocity should be within the expected range", m.getLeftVelocity(),
                    either(greaterThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Left acceleration should be within the expected range", Math.abs(m.getLeftAcceleration()),
                    either(lessThan((maxA))).or(closeTo((maxA), MathUtils.getFloatCompareThreshold())));

            assertThat("Right position should be within the expected range", m.getRightPosition(),
                    either(lessThan(distance)).or(closeTo(distance, MathUtils.getFloatCompareThreshold())));
            assertThat("Right position should be within the expected range", m.getRightPosition(),
                    either(greaterThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Right velocity should be within the expected range", m.getRightVelocity(),
                    either(lessThan((maxV))).or(closeTo((maxV), MathUtils.getFloatCompareThreshold())));
            assertThat("Right velocity should be within the expected range", m.getRightVelocity(),
                    either(greaterThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Right acceleration should be within the expected range", Math.abs(m.getRightAcceleration()),
                    either(lessThan((maxA))).or(closeTo((maxA), MathUtils.getFloatCompareThreshold())));
        }
    }

    /**
     * Performs testing on many points of a {@link TrapezoidalTankDriveProfile}
     * using a negative position.
     * 
     * This test is identical to {@link #testTrapezoidalTankDriveProfileAdvanced()}
     * except that it uses a negative position to construct the
     * {@link TrapezoidalTankDriveProfile} so that it goes backwards.
     */
    @Test
    public void testTrapezoidalTankDriveProfileAdvancedReversed() {
        TestHelper helper = new TestHelper(getClass(), testName);

        double maxV = helper.getDouble("maxV", 1000);
        double maxA = helper.getDouble("maxA", 1000);
        double distance = helper.getDouble("distance", -1000, 0);

        RobotSpecs specs = new RobotSpecs(maxV, maxA);

        Followable<TankDriveMoment> f = new TrapezoidalTankDriveProfile(specs, distance);

        double dt = f.totalTime() / 1000;
        for (double t = 0; t < f.totalTime(); t += dt) {
            TankDriveMoment m = f.get(t);
            assertThat("Left position should be within the expected range", m.getLeftPosition(),
                    either(greaterThan((distance))).or(closeTo((distance), MathUtils.getFloatCompareThreshold())));
            assertThat("Left position should be within the expected range", m.getLeftPosition(),
                    either(lessThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Left velocity should be within the expected range", -m.getLeftVelocity(),
                    either(lessThan((maxV))).or(closeTo((maxV), MathUtils.getFloatCompareThreshold())));
            assertThat("Left velocity should be within the expected range", m.getLeftVelocity(),
                    either(lessThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Left acceleration should be within the expected range", Math.abs(m.getLeftAcceleration()),
                    either(lessThan((maxA))).or(closeTo((maxA), MathUtils.getFloatCompareThreshold())));

            assertThat("Right position should be within the expected range", m.getRightPosition(),
                    either(greaterThan((distance))).or(closeTo((distance), MathUtils.getFloatCompareThreshold())));
            assertThat("Right position should be within the expected range", m.getRightPosition(),
                    either(lessThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Right velocity should be within the expected range", -m.getRightVelocity(),
                    either(lessThan((maxV))).or(closeTo((maxV), MathUtils.getFloatCompareThreshold())));
            assertThat("Right velocity should be within the expected range", m.getRightVelocity(),
                    either(lessThan((0.0))).or(closeTo((0.0), MathUtils.getFloatCompareThreshold())));

            assertThat("Right acceleration should be within the expected range", Math.abs(m.getRightAcceleration()),
                    either(lessThan((maxA))).or(closeTo((maxA), MathUtils.getFloatCompareThreshold())));
        }
    }

    /**
     * Performs full testing on {@link TrapezoidalTankDriveProfile#copy()}.
     * 
     * This test constructs an instance, and calls the copy method on it to create a
     * copy. It then uses {@link TestHelper#assertAllFieldsEqual(Object, Object)} to
     * compare the two objects for equality.
     */
    @Test
    public void testTrapezoidalTankDriveProfileCopy() {
        TestHelper helper = new TestHelper(getClass(), testName);

        double maxV = helper.getDouble("maxV", 1000);
        double maxA = helper.getDouble("maxA", 1000);
        double distance = helper.getDouble("distance", 1000);

        RobotSpecs specs = new RobotSpecs(maxV, maxA);

        DynamicFollowable<TankDriveMoment> f = new TrapezoidalTankDriveProfile(specs, distance);

        TestHelper.assertAllFieldsEqual(f, f.copy());
    }
}
