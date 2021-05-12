#ifndef NOSHAKE_UTILS_H
#define NOSHAKE_UTILS_H

#include <jni.h>
#include <stdio.h>
#include <stdbool.h>
#include <assert.h>
#include <stdlib.h>

//Get native pointer field (an address in memory) straight from the passed java class
jfieldID getPtrFieldId(JNIEnv* env, jobject obj);

#endif //NOSHAKE_UTILS_H
