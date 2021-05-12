package weiner.noah.constants;

public class NaiveConstants {
    public static final float NANOSEC_TO_SEC = 1.0f / 1000000000.0f;
    public static final float MAX_ACC = 8.0f;
    public static final float MAX_POS_SHIFT = 2000.0f;
    public static final float MAX_ZOOM_FACTOR = 0.2f;

    //parameter for low-pass filter
    public static final float LOW_PASS_ALPHA = 0.95f;

    //adjustable parameter that scales up velocity when integrating position
    public static final int VELOCITY_AMPL_DEFAULT = 30000;

    public static final float VELOCITY_FRICTION_DEFAULT = 0.1f;
    public static final float POSITION_FRICTION_DEFAULT = 0.1f;
}
