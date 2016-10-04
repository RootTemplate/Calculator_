#include "GrapherDPoints.h"

static const double COMPUTING_ERROR = 0.000000001;

byte getDPoint(double before, double prev, double next)
{
    double prevM = prev < 0 ? -prev : prev;
    double nextM = next < 0 ? -next : next;

    byte result = 0;
    if(nextM < COMPUTING_ERROR)
        result += DPOINT_WEAK;
    if(prevM < COMPUTING_ERROR)
        result += DPOINT_WEAK;
    if(result == 0 && next == next && prev == prev) {
        if(next * prev < 0) // Different signs
            result = DPOINT_STRONG;
        else if(before == before && (prev - before) * (next - prev) <= 0 && next * (2 * prev - before) < 0) {
            if(next > 0 && prev - before < next - prev)
                result = DPOINT_MIDDLE;
            else if(next < 0 && prev - before > next - prev)
                result = DPOINT_MIDDLE;
        }
    }


    return result;
}

double getComputingError()
{
    return COMPUTING_ERROR;
}
