#include <jni.h>

JNIEXPORT jstring Java_weiner_noah_noshake_MainActivity_stringFromJNI(JNIEnv *env, jobject this) {
    return (*env)->NewStringUTF(env, "Hello World!");
}
