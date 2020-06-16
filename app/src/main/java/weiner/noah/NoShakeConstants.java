package weiner.noah;

public class NoShakeConstants {
    public static final float spring_const = 130f; //190
    public static final float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    public static final float alpha = 0.8f;
    public static final float yFactor = 650f; //650
    public static final float e = 2.71828f;
    public static final float shaking_threshold = 0.1f;
    public static final int buffer_size = 211;
}
