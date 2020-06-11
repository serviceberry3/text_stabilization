#include "impulse_response.hh"

//redeclaration
float impulse_resp_arr::kValue = 0;
float impulse_resp_arr::eValue = 0;
size_t impulse_resp_arr::size = 0;
float* impulse_resp_arr::responseArray = NULL;

impulse_resp_arr::impulse_resp_arr(size_t sz, float e, float k) {
    eValue = e;
    kValue = k;
    size = sz;
    responseArray = (float*) memalign(16, sizeof(float)*sz);
}

impulse_resp_arr::~impulse_resp_arr() {
    free(responseArray);
}

//fill the array with the appropriate H(t) result based on spring constant k selected
void impulse_resp_arr::impulse_response_arr_populate() {
    //divide 4.0 seconds by the size of the array
    float timeIncrement = 4.0f/size;

    for (int i=0; i<size; i++) {
        //fill in this spot in the array with the appropriate H(t) value
        responseArray[i] = ((float)(timeIncrement * i) * pow(eValue, -timeIncrement * sqrtf(kValue)));
    }
}

//Java interface functions
extern "C" {
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1resp_1arr(JNIEnv *javaEnvironment, jclass __unused obj, jlong sz, jfloat e, jfloat k) {
        impulseResponses = new impulse_resp_arr(sz, e, k);
    }

    JNIEXPORT void Java_weiner_noah_ctojavaconnector_ImpulseResponse_impulse_1response_1arr_1populate(JNIEnv *javaEnvironment, jclass __unused obj) {
        impulseResponses->impulse_response_arr_populate();
    }
}

