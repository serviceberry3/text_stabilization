package weiner.noah.ctojavaconnector;

public class ImpulseResponse {
    //pointer to the underlying cpp object
    private long nativeObjPtr;

    public ImpulseResponse(int sz, float e, float k) {
        impulse_resp_arr(sz, e, k);
    }

    public native void impulse_resp_arr(long sz, float e, float k);

    public native void impulse_resp_arr_destroy();

    public native void impulse_response_arr_populate();

    public native float impulse_response_arr_get_value(int index);

    public native float impulse_response_arr_get_sum();

    public native int impulse_response_arr_get_size();

    public native int impulse_response_arr_get_data_address();
}
