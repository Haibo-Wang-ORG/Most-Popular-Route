
package rstar;
/*Last update 6-Nov-97 */
/* Updated by Josephine Wong 23 November 1997
   Changed to use User Specified values rather than Constants defined.
*/

import java.lang.Integer;
import java.awt.*;

public class rectangle
{
    public int id;
    public int UX,UY,LX,LY;
    public short Prop;

    public rectangle(int i)
    {
        this.id=i;
//        this.LX=(int) ((Constants.MAXCOORD-1)*Math.random());
//        this.LY=(int) ((Constants.MAXCOORD-1)*Math.random());
        this.LX=(int) ((UserInterface.getMAXCOORD()-1)*Math.random());
        this.LY=(int) ((UserInterface.getMAXCOORD()-1)*Math.random());
        do
        {
            this.UX=(int) (Constants.min (
//                                Constants.MAXCOORD-this.LX,
//                                Constants.MAXWIDTH
                                UserInterface.getMAXCOORD()-this.LX,
                                UserInterface.getMAXWIDTH()
                            )*Math.random()+this.LX);
        } while (UX == LX);
                           
        do
        {
            this.UY=(int) (Constants.min ( 
//                                Constants.MAXCOORD-this.LY,
//                                Constants.MAXWIDTH
                                UserInterface.getMAXCOORD()-this.LY,
                                UserInterface.getMAXWIDTH()
                            )*Math.random()+this.LY);
        } while (UX == LX);
                            
        this.Prop=(short) (5*Math.random() + 1);
    }

    public void print()
    {
      System.out.println(this.LX+" , "+this.LY);
      System.out.println(this.UX+" , "+this.UY);
    }
    
    public Rectangle toRectangle()
    {
        return new Rectangle(LX, LY, UX-LX, UY-LY);
    }
}
