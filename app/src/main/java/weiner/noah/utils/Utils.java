package weiner.noah.utils;

public class Utils {
    public static float rangeValue(float value, float min, float max)
    {
        //apply boundaries to a given value
        if (value > max) return max;

        //otherwise if val less than min return min, otherwise return value
        return Math.max(value, min);
    }

    public static void lowPassFilter(float[] input, float[] output, float alpha)
    {
        //iterate through each element of the input float array
        for (int i = 0; i < input.length; i++) {
            //set that slot in the output array to its previous value plus alphaConstant * (change in the value since last reading)
            output[i] = output[i] + (alpha * (input[i] - output[i])); //we only allow the acceleration reading to change by 85% of its actual change

            //a second way to implement
            //output[i] = input[i] - (alpha * output[i] + (1-alpha) * input[i]);
        }
    }

    public static float fixNanOrInfinite(float value)
    {
        //change NaN or infinity to 0
        if (Float.isNaN(value) || Float.isInfinite(value)) return 0;
        return value;
    }

    //multiply a 1xn matrix by an nx1 matrix (this is really a dot product fxn)
    public static float singleDimMatrixMult(float[] m1, float[] m2) {
        //make sure dimensions are same
        if (m1.length != m2.length) {
            return -1000000;
        }

        float prod = 0;

        for (int i = 0; i < m1.length; i++) {
            prod += m1[i] * m2[i];
        }

        return prod;
    }

    public static float[][] matrixMult(float[][] m1, float[][] m2) {
        //num cols in first must equal num rows in second
        if (m1[0].length != m2.length) {
            return null;
        }

        //resulting matrix is always num rows of first x num cols of second
        float[][] result = new float[m2[0].length][m1.length];

        for (int m1row = 0; m1row < m1.length; m1row++) {
            for (int m2col = 0; m2col < m2[0].length; m2col++) {
                result[m1row][m2col] = singleDimMatrixMult(m1[m1row], m2[m2col]);
            }
        }

        return result;
    }

    //multiply a matrix by a scalar
    public static float[][] scalarMatrixMult(float[][] m, float scalar) {
        //iterate over all rows
        for (int i = 0; i < m.length; i++) {
            //iterate over all entries in the row (cols)
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] *= scalar;
            }
        }

        return m;
    }

    //add scalar to a matrix
    public static float[][] scalarMatrixAddition(float[][] m, float addend) {
        //iterate over all rows
        for (int i = 0; i < m.length; i++) {
            //iterate over all entries in the row (cols)
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] += addend;
            }
        }

        return m;
    }

    public static float[][] matrixAddition(float[][] m1, float[][] m2) {
        //matrices must have identical dimensions
        if (m1.length != m2.length || m1[0].length != m2[0].length) {
            return null;
        }

        //resulting matrix will have same dims
        float[][] result = new float[m1.length][m1[0].length];

        //iterate over all rows
        for (int i = 0; i < m1.length; i++) {
            //iterate over all entries in the row (cols)
            for (int j = 0; j < m1[0].length; j++) {
                result[i][j] = m1[i][j] + m2[i][j];
            }
        }

        return result;
    }

    //divide matrix by scalar
    public static float[][] scalarMatrixDivision(float[][] m, float divisor) {
        //iterate over all rows
        for (int i = 0; i < m.length; i++) {
            //iterate over all entries in the row (cols)
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] /= divisor;
            }
        }

        return m;
    }

    //divide two 1x1 matrices
    public static float singleValMatrixDiv(float[][] m1, float[][] m2) {
        return m1[0][0] / m2[0][0];
    }

    public static float[][] flatMatrixTranspose(float[][] m) {
        //swap dims
        float[][] result = new float[m[0].length][m.length];

        //transpose two flat matrices
        for (int row = 0; row < m.length; row++) {
            for (int col = 0; col < m[0].length; col++) {
                result[col][row] = m[row][col];
            }
        }

        return result;
    }
}
