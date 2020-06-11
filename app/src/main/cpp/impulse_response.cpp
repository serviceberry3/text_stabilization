#include "impulse_response.hh"

//redeclaration
float impulse_resp_arr::eValue = 0;
size_t impulse_resp_arr::size = 0;
float* impulse_resp_arr::responseArray = NULL;

impulse_resp_arr::impulse_resp_arr(size_t sz, float e) {
    eValue = e;
    size = sz;
    responseArray = (float*) memalign(16, sizeof(float)*sz);
}

impulse_resp_arr::~impulse_resp_arr() {
    //free(responseArray);
}

//fill the array with the appropriate H(t) result based on spring constant k selected
void impulse_resp_arr::impulse_response_arr_populate(float k) {
    //divide 4.0 seconds by the size of the array
    float timeIncrement = 4.0f/size;

    for (int i=0; i<size; i++) {
        //fill in this spot in the array with the appropriate H(t) value
        responseArray[i] = (timeIncrement * i) * (eValue);
    }
}

