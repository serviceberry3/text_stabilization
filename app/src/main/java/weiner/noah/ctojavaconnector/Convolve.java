package weiner.noah.ctojavaconnector;

public class Convolve {
    private long nativeObjPtr;

    public Convolve(CircBuffer circBuffer, ImpulseResponse impulseResponse) {
        convolver(circBuffer, impulseResponse);
    }

    public native void convolver(CircBuffer circBuffer, ImpulseResponse impulseResponse);

    public native void convolver_destroy();

    public native float convolve(int current_head);

    public native float getYMember(int index);

    public native float getHMember(int index);

    public native float getXMember(int index);

    public native float getTempXMember(int index);

    public native long getYSize(int axis);
}
