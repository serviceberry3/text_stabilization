package weiner.noah.constants;

public class NoShakeConstants {
    public static final float SPRING_CONST = 250f;
    public static final float DAMPENER_FRIX_CONST = (float) (2.0 * Math.sqrt(SPRING_CONST));
    public static final float LOW_PASS_ALPHA = 0.9f;
    public static final float Y_FACTOR = 2000f;
    public static final float E = 2.71828f;
    public static final float SHAKING_THRESHOLD = 0.1f;
    public static final int BUFFER_SIZE = 211;
    public static final float EXTRA_FRIX_CONST = 0.1f;
    public static final float TO_ZERO_THRESH = 0.5f;

    public static final float POSITION_FRICTION_DEFAULT = 0.1f;
}
