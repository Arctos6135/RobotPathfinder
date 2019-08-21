#pragma once

#include "paths.h"
#include "trajectory/tankdrivemoment.h"
#include "robotspecs.h"
#include "trajectoryparams.h"
#include "math/vec2d.h"
#include "math/rpfmath.h"
#include "basictrajectory.h"
#include <memory>
#include <vector>
#include <stdexcept>

namespace rpf {
    class TankDriveTrajectory {
    public:
        TankDriveTrajectory(const BasicTrajectory &traj);

        inline std::shared_ptr<Path> get_path() {
            return path;
        }
        inline std::shared_ptr<const Path> get_path() const {
            return path;
        }
        inline std::vector<TankDriveMoment>& get_moments() {
            return moments;
        }
        inline const std::vector<TankDriveMoment>& get_moments() const {
            return moments;
        }
        inline double get_init_facing() const {
            return init_facing;
        }

        inline RobotSpecs& get_specs() {
            return specs;
        }
        inline const RobotSpecs& get_specs() const {
            return specs;
        }
        inline TrajectoryParams& get_params() {
            return params;
        }
        inline const TrajectoryParams& get_params() const {
            return params;
        }
        
        inline double total_time() const {
            return moments[moments.size() - 1].time;
        }

        TankDriveMoment get(double) const;

        std::shared_ptr<TankDriveTrajectory> mirror_lr() const;
        std::shared_ptr<TankDriveTrajectory> mirror_fb() const;
        std::shared_ptr<TankDriveTrajectory> retrace() const;

    protected:

        TankDriveTrajectory(std::shared_ptr<Path> path, std::vector<TankDriveMoment> &&moments, 
                bool backwards, const RobotSpecs &specs, const TrajectoryParams &params)
                : path(path), moments(moments), backwards(backwards), specs(specs), params(params), init_facing(moments[0].init_facing) {}

        std::shared_ptr<Path> path;
        std::vector<TankDriveMoment> moments;

        bool backwards = false;

        RobotSpecs specs;
        TrajectoryParams params;

        double init_facing;
    };
}
