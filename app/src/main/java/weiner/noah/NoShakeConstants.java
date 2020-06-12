package weiner.noah;

public class NoShakeConstants {
    public static final float spring_const = 12f;
    public static final float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    public static final float alpha = 0.8f;
    public static final float yFactor = 600f;
    public static final float e = 2.71828f;
    public static final float shaking_threshold = 0.3f;
    public static final int buffer_size = 211;
}
