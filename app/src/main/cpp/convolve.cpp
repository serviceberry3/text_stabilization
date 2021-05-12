#include "convolve.hh"



convolver::convolver(circular_buffer* xData, impulse_resp_arr* impulseRespData) {
    //we call "X" the acceleration data array, not meaning the x axis
    xArray = (float*) xData->circular_buf_address();
    xLength = 211; //FIXME: don't hardcode

    //we call "H" the impulse reponse data array
    hArray = (float*) impulseRespData->impulse_resp_arr_get_data_address();
    hLength = impulseRespData->impulse_resp_arr_get_size();

    //we call y the convolved result array
    yLength = xLength + hLength - 1;

    //allocated appropriately sized float array for the output signal (m + n - 1), set entire array to 0 to begin accumulation
    yArray = (float*) calloc(sizeof(float), yLength);
}

convolver::~convolver() {
    //free(hArray);
    free(xArray);
    free(tempXArray);
    free(yArray);
}

float convolver::convolve(int current_head) {
    /*
    float* tempHArray = (float*) calloc(yLength, sizeof(float));
    memcpy(tempHArray, hArray, hLength);
    */
    tempXArray = (float*) malloc(sizeof(float) * xLength);

    int currHead = current_head;

    //we want to order the data from the circular buffer from oldest to newest, using the head as the break point
    memcpy(tempXArray, xArray + currHead, sizeof(float) * (xLength-currHead));
    memcpy(tempXArray + (xLength - currHead), xArray, sizeof(float) * currHead);

    // convolution operation
    for (int i = 0; i < xLength; i++)
    {
        for (int j = 0; j < hLength; j++)
        {
            yArray[i + j] += tempXArray[i] * hArray[j];
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
    //constructor
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_Convolve_convolver(JNIEnv *javaEnvironment, jobject obj, jobject circ_buff_obj,
                                                                        jobject imp_resp_obj) {
        //get native object addies of the passed circular buffer and imp response array
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(circ_buff_obj, getPtrFieldId(javaEnvironment, circ_buff_obj));
        auto* cppImpRespObj = (impulse_resp_arr*) javaEnvironment->GetLongField(imp_resp_obj, getPtrFieldId(javaEnvironment, imp_resp_obj));

        //construct a convolver instance using the accel data buffer and imp resp data array, store addy of that cpp object in the passed Convolve java object
        javaEnvironment->SetLongField(obj, getPtrFieldId(javaEnvironment, obj),(jlong) new convolver(cppCircBuffObj, cppImpRespObj));
    }

    //destructor
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_Convolve_convolver_1destroy(JNIEnv *javaEnvironment, jobject obj) {
        //get address of the cpp convolver obj from the passed Convolve java object
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));

        //destroy this instance of convolver
        delete cppConvObj;
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_convolve(JNIEnv *javaEnvironment, jobject obj, jint current_head) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->convolve(current_head);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getYMember(JNIEnv *javaEnvironment, jobject obj, jint index) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->getYMember(index);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getHMember(JNIEnv *javaEnvironment, jobject obj, jint index) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->getHMember(index);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getXMember(JNIEnv *javaEnvironment, jobject obj, jint index) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->getXMember(index);
    }

    JNIEXPORT jlong Java_weiner_noah_ctojavaconnector_Convolve_getYSize(JNIEnv *javaEnvironment, jobject obj) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->getYSize();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_Convolve_getTempXMember(JNIEnv *javaEnvironment, jobject obj, jint index) {
        auto* cppConvObj = (convolver*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppConvObj->getTempXMember(index);
    }
}
