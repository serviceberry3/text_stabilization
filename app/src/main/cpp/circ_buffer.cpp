#include "circ_buffer.hh"

//a circular buffer instance
static circular_buffer* buff = NULL;

circular_buffer::circular_buffer(size_t sz) {
    buffer = (float*) memalign(16, sizeof(float)*sz);
}


circular_buffer::~circular_buffer() {
    free(buffer);
}

void circular_buffer::circular_buf_reset() {
    head=tail=0;
    full=false;
}

void circular_buffer::circular_buf_put(float data) {
    buffer[head] = data;
    advance_pointer();
}

float circular_buffer::circular_buf_get() {
    int r = -1;

    if (!circular_buf_empty()) {
        retreat_pointer();
        return buffer[tail];
    }
    else {
        return NULL;
    }
}

void circular_buffer::retreat_pointer() {
    full=false;
    tail = (tail+1) % max;
}

void circular_buffer::advance_pointer() {
    if (full) {
        tail = (tail+1) % max;
    }

    head = (head+1) % max;

    //check if the advancement made head equal to tail, which means the circular queue is now full
    full = (head == tail);
}

bool circular_buffer::circular_buf_empty() {
    return (!full && (head==tail));
}

//java interface functions
extern "C" {
    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_circular_1buffer(JNIEnv *javaEnvironment, jobject __unused obj, jlong sz) {
        buff = new circular_buffer((size_t) sz);
    }

    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_circular_1buf_1reset(JNIEnv* __unused javaEnvironment, jobject __unused obj) {
        buff->circular_buf_reset();
    }

    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_circular_1buf_1put(JNIEnv* __unused javaEnvironment, jobject __unused obj, jfloat data) {
        buff->circular_buf_put(data);
    }

    JNIEXPORT jfloat Java_weiner_noah_noshake_MainActivity_circular_1buf_1get(JNIEnv* __unused javaEnvironment, jobject __unused obj) {
        buff->circular_buf_get();
    }

    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_retreat_1pointer(JNIEnv* __unused javaEnvironment, jobject __unused obj) {
        buff->retreat_pointer();
    }

    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_advance_1pointer(JNIEnv* __unused javaEnvironment, jobject __unused obj) {
        buff->advance_pointer();
    }

    JNIEXPORT void Java_weiner_noah_noshake_MainActivity_circular_1buf_1empty(JNIEnv* __unused javaEnvironment, jobject __unused obj) {
        buff->circular_buf_empty();
    }

}





