package rstar;
/*Last update 4-Nov-97 */

import java.io.*;
import java.awt.*;

/**
*  A class to compare the similarity between the Query and the Image database
*/

public class similarity
{
    public static final double da=45;
    public static final double dap=5;
    public static final double ddp=0;
    
    public static double angleSimilarity(double target, double actual)
    {
        if ((target==0) && (actual>315)) target=360;
        if (Math.abs(target-actual)>=da) return 0;
        if (Math.abs(target-actual)<=dap) return 1;
        if (actual<target)
        {
            return (actual-target+da)/(da-dap);
        }
        else
        {
            return (da+target-actual)/(da-dap);
        }
    }

}