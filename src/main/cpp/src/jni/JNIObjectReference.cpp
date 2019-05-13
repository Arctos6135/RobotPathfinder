#include "jni/com_arctos6135_robotpathfinder_core_lifecycle_JNIObjectReference.h"
#include "trajectories.h"
#include "jni/jniutil.h"
#include "jni/instlists.h"
#include <algorithm>

JNIEXPORT void JNICALL Java_com_arctos6135_robotpathfinder_core_lifecycle_JNIObjectReference__1freeObject(JNIEnv *env, jclass clazz, jlong ptr) {
    if(!ptr) {
        return;
    }

    if(rpf::remove_instance(pinstances, reinterpret_cast<rpf::Path *>(ptr))) {
        return;
    }
    if(rpf::remove_instance(btinstances, reinterpret_cast<rpf::BasicTrajectory *>(ptr))) {
        return;
    }
    if(rpf::remove_instance(ttinstances, reinterpret_cast<rpf::TankDriveTrajectory *>(ptr))) {
        return;
    }
}

