#include "circ_buffer.hh"


circular_buffer::circular_buffer(size_t sz) {
    //buffer = (float*) memalign(16, sizeof(float) * sz);

    //clear all buffer entries to 0
    buffer = (float*) calloc(sizeof(float), sz);
    buff_address = (long) buffer;
    max = sz;
    circular_buf_reset();
    assert(circular_buf_empty());
}


circular_buffer::~circular_buffer() {
    free(buffer);
}

void circular_buffer::circular_buf_reset() {
    head = tail = 0;
    full = false;
}

int circular_buffer::circular_buf_put(float data) {
    buffer[head] = data;
    advance_pointer();
    return head;
}

//get one member from the queue--namely, the FIRST-IN data that's currently located at the tail position
float circular_buffer::circular_buf_get() {
    //error code of -10000 is safe for our purposes
    float r = -10000;

    if (!circular_buf_empty()) {
        r = buffer[tail];
        retreat_pointer();
    }

    //return data at tail, otherwise return -10000;
    return r;
}

//get a value from the buffer at time point latest - back, i.e., calling with back=1 returns a[k-1], where k is current time
float circular_buffer::circular_buf_get_past_entry_flat(int back) {
    //make sure raw buffer are non-NULL
    //TODO: maybe find a better way to do this, since this is supposed to serve as a sort of user-blind API
    assert(buffer != nullptr);

    //make sure the requested n is not greater than the current population of the queue
    size_t size = circular_buf_size();
    if (back > size - 1) { //for example, if user requests k-1 but size is currently only 1
        return 0;
    }

    //initialize an average
    float average = 0;

    //find the head of the queue as an integer. This is the position of the last added data
    int position_of_last_added_data = (int) head - 1;

    int position_requested = position_of_last_added_data - back;

    //reset when we hit 0
    if (position_requested < 0) {
        position_requested = (int) max + position_requested;
    }

    //return data found
    return buffer[position_requested];
}

void circular_buffer::retreat_pointer() {
    full = false;
    tail = (tail + 1) % max;
}

int circular_buffer::circular_buf_get_head() {
    return head;
}

int circular_buffer::circular_buf_capacity() {
    return max;
}

//advance the queue head (used after adding data), and possibly advance the tail if the buffer is already full
void circular_buffer::advance_pointer() {
    //if the buffer is full (head=tail), we need to throw OUT the the FIRST-IN data by advancing the tail as well (it's a FIFO queue)
    if (full) {
        tail = (tail + 1) % max;
    }

    //advance the head no matter what
    head = (head + 1) % max;

    //check if the advancement made head equal to tail, which means the circular queue is now full
    full = (head == tail); //if it was full before the advance it'll be full after too
}

bool circular_buffer::circular_buf_empty() {
    //boolean of negation of full anded with head=tail
    return (!full && (head == tail));
}

//check if the circular buffer is full
bool circular_buffer::circular_buf_full() {
    return full;
}

//take the average of the last n entries behind the queue head. Used to determine if the device is shaking.
float circular_buffer::aggregate_last_n_entries(int n) {
    //make sure raw buffer are non-NULL
    //TODO: maybe find a better way to do this, since this is supposed to serve as a sort of user-blind API
    assert(buffer != nullptr);

    //make sure the requested n is not greater than the current population of the queue
    size_t size = circular_buf_size();
    if (n > size) {
        return -1;
    }

    //initialize an average
    float average = 0;

    //find the head of the queue as an integer
    int position = (int) head - 1;

    int cutoff = position - n;
    if (cutoff < 0) {
        cutoff = (int) max + cutoff;
    }

    //run back n spaces in the queue, adding all entries to the average
    for (int i = position; i != cutoff; i--) {
        //i could become negative if the head was at a low number, so need to correct that
        if (i < 0) {
            //change i to the correct index of the buffer
            i = (int) max + i;
            if (i == cutoff) {
                return average / (float)n;
            }
        }

        //add absolute value acceleration reading the the average
        average += abs(buffer[i]);
    }

    //divide average by number of elements read to get aggregate reading
    return average / (float)n;
}

int circular_buffer::circular_buf_size() {
    //if the buffer is full, our size is the max
    size_t size = max;

    if (!full)
    {
        //if circular buffer is not full and head is greater than tail, find difference to get current size
        if (head >= tail)
        {
            size = head - tail;
        }

        //otherwise we've taken out stuff past the head (which means the buffer was full, so the current size is the maximum minus
        // however much has been taken out (space between head and tail)
        else
        {
            size = (max + head - tail);
        }
    }
    return size;
}

long circular_buffer::circular_buf_address() {
    return buff_address;
}


//java interface functions
extern "C" {
    //constructor
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buffer(JNIEnv *javaEnvironment, jobject obj, jint sz) {
        //construct a circular buffer instance and store address of that cpp object in the passed CircBuffer java object
        javaEnvironment->SetLongField(obj, getPtrFieldId(javaEnvironment, obj),(jlong) new circular_buffer(sz));
    }

    //destructor
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buffer_1destroy(JNIEnv *javaEnvironment, jobject obj) {
        //get address of the cpp circular_buffer obj from the passed CircBuffer java object
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));

        //destroy this instance of circular_buffer
        delete cppCircBuffObj;
    }

    //reset the circular buffer
    JNIEXPORT void Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1reset(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        cppCircBuffObj->circular_buf_reset();
    }

    JNIEXPORT jint Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1put(JNIEnv* __unused javaEnvironment, jobject obj, jfloat data) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_put(data);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1get_1past_1entry_1flat(JNIEnv* __unused javaEnvironment, jobject obj, jint back) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_get_past_entry_flat(back);
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1get(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_get();
    }

    JNIEXPORT void Java_weiner_noah_ctojavaconnector_CircBuffer_retreat_1pointer(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        cppCircBuffObj->retreat_pointer();
    }

    JNIEXPORT void Java_weiner_noah_ctojavaconnector_CircBuffer_advance_1pointer(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        cppCircBuffObj->advance_pointer();
    }

    JNIEXPORT jboolean Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1empty(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_empty();
    }

    JNIEXPORT jint Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1size(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_size();
    }

    JNIEXPORT jfloat Java_weiner_noah_ctojavaconnector_CircBuffer_aggregate_1last_1n_1entries(JNIEnv* __unused javaEnvironment, jobject obj, jint n) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->aggregate_last_n_entries(n);
    }

    JNIEXPORT jboolean Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1full(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_full();
    }

    JNIEXPORT jint Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1capacity(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_capacity();
    }

    JNIEXPORT jint Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1get_1head(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_get_head();
    }

    JNIEXPORT jlong Java_weiner_noah_ctojavaconnector_CircBuffer_circular_1buf_1address(JNIEnv* __unused javaEnvironment, jobject obj) {
        auto* cppCircBuffObj = (circular_buffer*) javaEnvironment->GetLongField(obj, getPtrFieldId(javaEnvironment, obj));
        return cppCircBuffObj->circular_buf_address();
    }
}