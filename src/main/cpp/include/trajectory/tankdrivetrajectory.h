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
        TankDriveTrajectory(const BasicTrajectory &traj) : path(traj.path), specs(traj.specs), params(traj.params), init_facing(traj.init_facing) {
            if(!params.is_tank) {
                throw std::invalid_argument("Base trajectory must be tank");
            }
            
            path->set_base(specs.base_width / 2);
            moments.reserve(traj.moments.size());
            if(!std::isnan(params.waypoints[0].velocity)) {
                double v = traj.moments[0].vel;
                double d = v / traj.pathr[0] * specs.base_width / 2;

                moments.push_back(TankDriveMoment(0, 0, v - d, v + d, 0, 0, traj.moments[0].heading, 0));
            }
            else {
                moments.push_back(TankDriveMoment(0, 0, 0, 0, 0, 0, traj.moments[0].heading, 0));
            }

            auto init = path->wheels_at(0);
            moments[0].init_facing = traj.init_facing;
            
            for(size_t i = 1; i < traj.moments.size(); i ++) {
                auto wheels = path->wheels_at(traj.patht[i]);
                double dl = init.first.dist(wheels.first);
                double dr = init.second.dist(wheels.second);
                double dt = traj.moments[i].time - traj.moments[i - 1].time;

                init = wheels;
                double d = traj.moments[i].vel / traj.pathr[i] * (specs.base_width / 2);
                double lv = rpf::rabs(traj.moments[i].vel - d, specs.max_v);
                double rv = rpf::rabs(traj.moments[i].vel + d, specs.max_v);

                if(lv < 0) {
                    dl = -dl;
                }
                if(rv < 0) {
                    dr = -dr;
                }

                moments.push_back(TankDriveMoment(moments[i - 1].l_dist + dl, moments[i - 1].r_dist + dr, lv, rv, 
                        0, 0, traj.moments[i].heading, traj.moments[i].time, traj.init_facing));
                moments[i - 1].l_accel = (lv - moments[i - 1].l_vel) / dt;
                moments[i - 1].r_accel = (rv - moments[i - 1].r_vel) / dt;
            }
        }

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
