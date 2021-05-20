package weiner.noah.ctojavaconnector;

public class CircBuffer {
    private long nativeObjPtr;

    public CircBuffer(int sz) {
        circular_buffer(sz);
    }

    //JAVA C++ INTERFACE FUNCTION PROTOTYPES
    public native void circular_buffer(int sz);

    public native void circular_buffer_destroy();

    //reset the circular buffer to empty, head == tail
    public native void circular_buf_reset();

    public native void retreat_pointer();

    public native void advance_pointer();

    //add data to the queue; old data is overwritten if buffer is full
    public native int circular_buf_put(float data);

    public native float circular_buf_get_past_entry_flat(int back);

    //retrieve a value from the buffer
    //returns 0 on success, -1 if the buffer is empty
    public native float circular_buf_get();

    //returns true if the buffer is empty
    public native boolean circular_buf_empty();

    //returns true if the buffer is full
    public native boolean circular_buf_full();

    //returns the maximum capacity of the buffer
    public native int circular_buf_capacity();

    //returns the current number of elements in the buffer
    public native int circular_buf_size();

    public native float aggregate_last_n_entries(int n);

    public native int circular_buf_get_head();

    public native long circular_buf_address();
}
