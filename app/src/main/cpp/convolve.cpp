#include "convolve.hh"
#include "circ_buffer.hh"
#include "impulse_response.hh"

static convolver* ySignalConvolver = NULL;

convolver::convolver() {
    xArray = buff->buffer;
    xLength = buff->circular_buf_size();

    hArray = impulseResponses->responseArray;
    hLength = impulseResponses->size;

    yLength = xLength + hLength - 1;

    //allocated appropriately sized float array for the output signal (m+n-1)
    yArray = (float*) memalign(16, sizeof(float)* yLength);
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

extern "C" {
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_Convolve_convolver(JNIEnv *javaEnvironment, jclass __unused obj) {
        ySignalConvolver = new convolver();
    }

    JNIEXPORT void Java_weiner_noah_ctojavaconnector_Convolve_convolve(JNIEnv *javaEnvironment, jclass __unused obj) {
        ySignalConvolver->convolve();
    }
}
