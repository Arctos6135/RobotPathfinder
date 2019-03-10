#pragma once

#include <limits>
#include "math/rpfmath.h"

namespace rpf {
    struct BasicMoment {

        BasicMoment() {}
        BasicMoment(double d, double v, double a, double h, double initf) : dist(d), vel(v), accel(a), init_facing(initf) {}
        BasicMoment(double d, double v, double a, double h, double t, double initf) : dist(d), vel(v), accel(a), time(t),
                init_facing(initf) {}
        BasicMoment(double d, double v, double a, double h) : dist(d), vel(v), accel(a), init_facing(std::numeric_limits<double>::quiet_NaN()) {}
        
        double dist;
        double vel;
        double accel;
        double heading;
        double time;

        double init_facing;
        bool backwards = false;

        double get_afacing() {
            return backwards ? -heading : heading;
        }
        double get_rfacing() {
            return rangle(get_afacing() - init_facing);
        }
    };
}