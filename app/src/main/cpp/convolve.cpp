#include "convolve.hh"
#include "circ_buffer.hh"
#include "impulse_response.hh"

static convolver* ySignalConvolver = NULL;

convolver::convolver() {
    xArray = buff->buffer;
    //xLength = buff->circular_buf_size();
    xLength = 211;

    hArray = impulseResponses->responseArray;
    hLength = impulseResponses->size;

    yLength = xLength + hLength - 1;

    //allocated appropriately sized float array for the output signal (m+n-1), set entire array to 0 to begin accumulation
    yArray = (float*) calloc(sizeof(float), yLength);
}

convolver::~convolver() {
    free(hArray);
    free(xArray);
    free(yArray);
}

float convolver::convolve() {
    /*
    float* tempHArray = (float*) calloc(yLength, sizeof(float));
    memcpy(tempHArray, hArray, hLength);
*/
    tempXArray = (float*) malloc(sizeof(float) * xLength);

    int currHead = buff->head;

    //we want to order the data from the circular buffer from oldest to newest, using the head as the break point
    memcpy(tempXArray, xArray + currHead, sizeof(float) * (xLength-currHead));
    memcpy(tempXArray + (xLength-currHead), xArray, sizeof(float) * currHead);

    // convolution operation
    for (int i = 0; i < xLength; i++)
    {
        for (int j = 0; j < hLength; j++)
        {
            yArray[i+j] += tempXArray[i] * hArray[j];
        }
    }

    return tempXArray[84];
}

float convolver::getTempXMember(int index) {
    return tempXArray[index];
}

float convolver::getYMember(int index) {
    return yArray[index];
}

float convolver::getHMember(int index) {
    return hArray[index];
}

float convolver::getXMember(int index) {
    return xArray[index];
}

size_t convolver::getYSize() {
    return yLength;
}

extern "C" {
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_Convolve_convolver(JNIEnv *javaEnvironment, jclass __unused obj) {
        ySignalConvolver = new convolver();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_convolve(JNIEnv *javaEnvironment, jclass __unused obj) {
        return ySignalConvolver->convolve();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getYMember(JNIEnv *javaEnvironment, jclass __unused obj, jint index) {
        return ySignalConvolver->getYMember(index);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getHMember(JNIEnv *javaEnvironment, jclass __unused obj, jint index) {
        return ySignalConvolver->getHMember(index);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getXMember(JNIEnv *javaEnvironment, jclass __unused obj, jint index) {
        return ySignalConvolver->getXMember(index);
    }

    JNIEXPORT jlong Java_weiner_noah_ctojavaconnector_Convolve_getYSize(JNIEnv *javaEnvironment, jclass __unused obj) {
        return ySignalConvolver->getYSize();
    }

    JNIEXPORT jlong Java_weiner_noah_ctojavaconnector_Convolve_getTempXMember(JNIEnv *javaEnvironment, jclass __unused obj, jint index) {
        return ySignalConvolver->getTempXMember(index);
    }
}
