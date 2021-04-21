package weiner.noah;

public class NoShakeConstants {
    public static final float spring_const = 400f; //190
    public static final float dampener_frix_const = (float) (2.0 * Math.sqrt(spring_const));
    public static final float low_pass_alpha = 0.9f; //0.9
    public static final float yFactor = 2000f; //650 //latest: 1200
    public static final float e = 2.71828f;
    public static final float shaking_threshold = 0.1f;
    public static final int buffer_size = 211;
    public static final float extra_frix_const = 0.1f;
}
