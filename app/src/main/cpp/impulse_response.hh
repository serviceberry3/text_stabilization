#ifndef NOSHAKE_IMPULSE_RESPONSE_HH
#define NOSHAKE_IMPULSE_RESPONSE_HH

#include <stdlib.h>
#include <jni.h>
#include <math.h>
#include <assert.h>
#include "utils.h"

class impulse_resp_arr {
public:
    impulse_resp_arr(size_t sz, float e, float k);

    ~impulse_resp_arr();

    void impulse_response_arr_populate();

    float impulse_response_arr_get_value(int index);

    float impulse_response_arr_get_sum();

    int impulse_resp_arr_get_size();

    long impulse_resp_arr_get_data_address();

    //public static property so can be used by convolver
    float* responseArray;
    size_t size;
    float eValue;
    float kValue;
};

#endif //NOSHAKE_IMPULSE_RESPONSE_HH
