#include "impulse_response.hh"

#define HZ 211
#define TAU 0


impulse_resp_arr::impulse_resp_arr(size_t sz, float e, float k) {
    eValue = e;
    kValue = k;
    size = sz;
    responseArray = (float*) memalign(16, sizeof(float) * sz);
}

impulse_resp_arr::~impulse_resp_arr() {
    free(responseArray);
}

//fill the array with the appropriate H(t) result based on spring constant k selected
void impulse_resp_arr::impulse_response_arr_populate() {
    //divide 4.0 seconds by the size of the array
    float sqrtK = sqrt(kValue);
    /*
    float timeIncrement = 4.0f/size;


    for (int i=0; i<size; i++) {
        float currTime = timeIncrement * i;
        //fill in this spot in the array with the appropriate H(t) value
        responseArray[i] = ((float)(currTime) * pow(eValue, -currTime * sqrtK));
    }
    */

    for (int t = 0; t < size; t++) {
        responseArray[size - 1 - t] = (float)(t + TAU) / HZ * pow(eValue, -(float)(t + TAU) / HZ * sqrtK);
    }
}

float impulse_resp_arr::impulse_response_arr_get_value(int index) {
    return responseArray[index];
}

float impulse_resp_arr::impulse_response_arr_get_sum() {
    assert(responseArray != nullptr);
    assert(size > 0);

    float sum = 0;

    for (int i = 0; i < size; i++) {
        sum += responseArray[i];
    }
    return sum;
}

int impulse_resp_arr::impulse_resp_arr_get_size() {
    return size;
}

//get address of underlying float buffer
long impulse_resp_arr::impulse_resp_arr_get_data_address() {
    return (long)responseArray;
}

//Java interface functions
extern "C" {
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1resp_1arr(JNIEnv *javaEnvironment, jobject obj, jlong sz, jfloat e, jfloat k) {
        //construct an impulse response instance and store address of that cpp object in the passed ImpuleResponse java object
        javaEnvironment->SetLongField(obj, getPtrFieldId(javaEnvironment, obj),(jlong) new impulse_resp_arr(sz, e, k));
    }

    //destructor
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1resp_1arr_1destroy(JNIEnv *javaEnvironment, jobject obj) {
        //get address of the cpp impulse_resp_arr obj from the passed ImpulseResponse java object
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));

        //destroy this instance of impulse_resp_arr
        delete cppImpRespObj;
    }

    JNIEXPORT void Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1populate(JNIEnv *javaEnvironment, jobject obj) {
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        cppImpRespObj->impulse_response_arr_populate();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1get_1sum(JNIEnv *javaEnvironment, jobject obj) {
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppImpRespObj->impulse_response_arr_get_sum();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1get_1value(JNIEnv *javaEnvironment, jobject obj, jint index) {
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppImpRespObj->impulse_response_arr_get_value(index);
    }

    JNIEXPORT jint Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1get_1size(JNIEnv *javaEnvironment, jobject obj) {
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppImpRespObj->impulse_resp_arr_get_size();
    }

    JNIEXPORT jlong Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1get_1data_1address(JNIEnv *javaEnvironment, jobject obj) {
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppImpRespObj->impulse_resp_arr_get_data_address();
    }

}

