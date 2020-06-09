#include "circ_buffer.h"


JNIEXPORT jstring Java_weiner_noah_noshake_MainActivity_stringFromJNI(JNIEnv *env, jobject this) {
    return (*env)->NewStringUTF(env, "Hello World!");
}


JNIEXPORT jstring Java_weiner_noah_noshake_MainActivity_hello(JNIEnv *env, jobject this) {
    if (tester())
        return (*env)->NewStringUTF(env, "Test successful");
    else
        return NULL;
}



