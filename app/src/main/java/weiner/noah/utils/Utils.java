package weiner.noah.utils;

public class Utils {
    public static float rangeValue(float value, float min, float max)
    {
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    public static void lowPassFilter(float[] input, float[] output, float alpha)
    {
        for (int i = 0; i < input.length; i++)
            output[i] = output[i] + alpha * (input[i] - output[i]);
    }

    public static float fixNanOrInfinite(float value)
    {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0;
        return value;
    }
}
