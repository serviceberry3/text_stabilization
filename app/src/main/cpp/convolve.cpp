#include "convolve.hh"

int x[15],h[15],y[15];

int convolve (void)
{
    int i,j,m,n;

    // padding of zeroes
    for (i = m; i <= m + n - 1; i++)
        x[i] = 0;

    for (i = n; i <= m + n - 1; i++)
        h[i] = 0;

    // convolution operation
    for (i = 0; i < m + n - 1; i++)
    {
        y[i]=0;

        for (j=0; j<=i; j++)
        {
            y[i] += x[j] * h[i-j];
        }
    }

}
