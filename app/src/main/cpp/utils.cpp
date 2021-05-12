//
// Created by nodog on 5/12/21.
//
#include "utils.h"

//Get pointer field straight from the java class, e.g. `CircBuffer`
jfieldID getPtrFieldId(JNIEnv* env, jobject obj)
{
    //get class tied to the passed object (e.g. `CircBuffer`)
    jclass c = env->GetObjectClass(obj);

    //get object property called "nativeObjPtr," which corresponds to `private long nativeObjPtr` in, e.g., `CircBuffer.java`
    jfieldID ptrFieldId = env->GetFieldID(c, "nativeObjPtr", "J");

    //delete reference to the class
    env->DeleteLocalRef(c);

    //return the native object pointer of the passed java object, e.g., CircBuffer
    return ptrFieldId;
}

