#ifndef NOSHAKE_CONVOLVE_HH
#define NOSHAKE_CONVOLVE_HH

#include <stdlib.h>
#include <jni.h>
#include "impulse_response.hh"
#include "circ_buffer.hh"

class convolver {
public:
    convolver();

    ~convolver();

    void convolve();

private:
    float* hArray;
    int hLength;
    float* xArray;
    int xLength;
    float* yArray;
    int yLength;
};

#endif //NOSHAKE_CONVOLVE_HH
