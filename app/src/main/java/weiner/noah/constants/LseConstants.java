package weiner.noah.constants;

public class LseConstants {
    public static final float RHO = 0.9f;
    public static final float GAMMA = 0.1f;

    //"weight" of the autoregressive model
    public static final float LAMBDA = 0.98f;

    //window size of filtering
    public static final int n = 12;

    //if this is set to true, we use kinematics-derived equation D[k] = D[k−1] + (∆t)2·a[k]
    //else we use equation D[k] = ρ·D[k − 1] + γ·a[k]
    public static final boolean USE_PHYSICS = true;

    //could calculate actual meters per pixel of the screen, or just use this scaling factor
    public static final float Y_FACTOR = 2000f;

    public static final float c = 1.0f;
}
