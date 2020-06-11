#include "convolve.hh"
#include "circ_buffer.hh"
#include "impulse_response.hh"

static convolver* ySignalConvolver = NULL;

convolver::convolver() {
    //xArray = buff->buffer;
    //hArray = impulseResponses->responseArray;
}

convolver::~convolver() {
    free(hArray);
    free(xArray);
    free(yArray);
}

void convolver::convolve() {
    // padding of zeroes in the output array
    for (int i = xLength; i <= xLength + hLength - 1; i++)
        xArray[i] = 0;

    for (int i = hLength; i <= xLength + hLength - 1; i++)
        hArray[i] = 0;

    // convolution operation
    for (int i = 0; i < xLength + hLength - 1; i++)
    {
        yArray[i]=0;

        for (int j = 0; j <= i; j++)
        {
            yArray[i] += xArray[j] * hArray[i-j];
        }
    }
}
