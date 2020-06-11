#ifndef NOSHAKE_IMPULSE_RESPONSE_HH
#define NOSHAKE_IMPULSE_RESPONSE_HH

#include <stdlib.h>
#include <jni.h>
#include <math.h>

class impulse_resp_arr {
public:
    impulse_resp_arr(size_t sz, float e, float k);

    ~impulse_resp_arr();

    void impulse_response_arr_populate();

    //public static property so can be used by convolver
    static float* responseArray;
    static size_t size;
    static float eValue;
    static float kValue;
};

static impulse_resp_arr* impulseResponses;

#endif //NOSHAKE_IMPULSE_RESPONSE_HH
