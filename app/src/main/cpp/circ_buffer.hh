#include <jni.h>
#include <stdio.h>
#include <stdbool.h>
#include <assert.h>
#include <stdlib.h>

class circular_buffer {
public:
    //construct an instance of a circular buffer using a design
    circular_buffer(size_t sz);

    //destroy the circular buffer instance
    ~circular_buffer();

    //reset the circular buffer to empty, head == tail
    void circular_buf_reset();

    void retreat_pointer();

    void advance_pointer();

    //put version 1 continues to add data if the buffer is full
    //old data is overwritten
    void circular_buf_put(float data);

    //retrieve a value from the buffer
    //returns 0 on success, -1 if the buffer is empty
    float circular_buf_get();

    //returns true if the buffer is empty
    bool circular_buf_empty();

    //returns true if the buffer is full
    bool circular_buf_full();

    //returns the maximum capacity of the buffer
    size_t circular_buf_capacity();

    //returns the current number of elements in the buffer
    size_t circular_buf_size();

private:
    float* buffer;
    size_t head;
    size_t tail;
    size_t max; //maximum size of the buffer
    bool full;
};




