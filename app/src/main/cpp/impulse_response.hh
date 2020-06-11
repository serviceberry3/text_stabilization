#ifndef NOSHAKE_IMPULSE_RESPONSE_HH
#define NOSHAKE_IMPULSE_RESPONSE_HH

#include <stdlib.h>
#include <jni.h>

class impulse_resp_arr {
public:
    impulse_resp_arr(size_t sz);

    ~impulse_resp_arr();

    void impulse_response_arr_populate(float k);
};

#endif //NOSHAKE_IMPULSE_RESPONSE_HH
