package weiner.noah.ctojavaconnector;

public class CircBuffer {
    //JAVA C++ INTERFACE FUNCTION PROTOTYPES
    public static native void circular_buffer(long sz);

    //reset the circular buffer to empty, head == tail
    public static native void circular_buf_reset();

    public static native void retreat_pointer();

    public static native void advance_pointer();

    //add data to the queue; old data is overwritten if buffer is full
    public static native void circular_buf_put(float data);

    //retrieve a value from the buffer
    //returns 0 on success, -1 if the buffer is empty
    public static native float circular_buf_get();

    //returns true if the buffer is empty
    public static native boolean circular_buf_empty();

    //returns true if the buffer is full
    public static native boolean circular_buf_full();

    //returns the maximum capacity of the buffer
    public static native long circular_buf_capacity();

    //returns the current number of elements in the buffer
    public static native long circular_buf_size();

    public static native float aggregate_last_n_entries(int n);
}
