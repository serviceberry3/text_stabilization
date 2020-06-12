package weiner.noah.ctojavaconnector;

public class Convolve {
    public static native void convolver();

    public static native float convolve();

    public static native float getYMember(int index);

    public static native float getHMember(int index);

    public static native float getXMember(int index);

    public static native float getTempXMember(int index);

    public static native long getYSize();
}
