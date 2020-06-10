package weiner.noah.utils;

public class Utils {
    public static float rangeValue(float value, float min, float max)
    {
        //apply boundaries to a given value
        if (value > max) return max;
        if (value < min) return min;
        return value;
    }

    public static void lowPassFilter(float[] input, float[] output, float alpha)
    {
        //iterate through each element of the input float array
        for (int i = 0; i < input.length; i++)
            //set that slot in the output array to its previous value plus alphaConstant * (change in the value since last reading)
            output[i] = output[i] + (alpha * (input[i] - output[i]));
    }

    public static float fixNanOrInfinite(float value)
    {
        //change NaN or infinity to 0
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0;
        return value;
    }
}
