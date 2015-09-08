
package rstar;
/*Last update 29-Nov-97 */

/** A relation object represents the actual topological, distance,
* directional relations between two rectangles.
*/
public class relation
{
    public char T;      // Topological relation
    public double D;    // distance relation
    public double A;    // Directional relation

    /**Initializes relations between two given rectangles r1,r2
    */

    public relation(rectangle r1, rectangle r2)
    {
        this.D=distance(r1,r2);
        this.A=angle(r1,r2);
        this.T=topological(r1,r2);
    }

    /** Creates a new empty relation object.
    */

    public relation()
    {
        this.D=-1;
        this.A=-1;
        this.T=' ';
    }

    /** Calculates the distance between the centroids two rectangles.
    */

    public static double distance(rectangle r1 , rectangle r2)
    {
        double x1,y1,x2,y2;
        double d;
        x1=(r1.LX+r1.UX)/2;
        y1=(r1.LY+r1.UY)/2;
        x2=(r2.LX+r2.UX)/2;
        y2=(r2.LY+r2.UY)/2;
        d=Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
        return d;
    }

    /** Calculates the angle between the centroids of two rectangles.
    */

    public static double angle(rectangle r1, rectangle r2)
    {
        double x1,y1,x2,y2;
        double x,y;
        double u,d;
        x1=(r1.LX+r1.UX)/2;
        y1=(r1.LY+r1.UY)/2;
        x2=(r2.LX+r2.UX)/2;
        y2=(r2.LY+r2.UY)/2;
        x=x1-x2;
        y=y1-y2;
        u=Math.atan2(x,y);
        u-=Math.PI/2;
        if (u<-Math.PI) u+=2*Math.PI;
        if ((x>=0) && (y>=0))
        {
            d=u*180/Math.PI;
        }
        if ((x>=0) && (y>=0))
        {
            d=u;
        }

        if (u>=0)
        {
            d=u*180/Math.PI;
        }
        else
        {
            d=360+u*180/Math.PI;
        }
        return d;
    }

    /** Calculates the topological relation between two rectangles.
    */

    public static char topological(rectangle r1,rectangle r2)
    {
        char c;
        if ((r1.LX == r2.LX) &&
            (r1.LY == r2.LY) &&
            (r1.UX == r2.UX) &&
            (r1.UY == r2.UY) ) return 'E';  // EQUAL
        if ((r1.LX > r2.LX) &&
            (r1.LY > r2.LY) &&
            (r1.UX < r2.UX) &&
            (r1.UY < r2.UY) ) return 'I';   // INSIDE
        if ((r1.LX < r2.LX) &&
            (r1.LY < r2.LY) &&
            (r1.UX > r2.UX) &&
            (r1.UY > r2.UY) ) return 'C';   // CONTAINS
        if ((r1.LX >= r2.LX) &&
            (r1.LY >= r2.LY) &&
            (r1.UX <= r2.UX) &&
            (r1.UY <= r2.UY) ) return 'B';  // COVER BY
        if ((r1.LX <= r2.LX) &&
            (r1.LY <= r2.LY) &&
            (r1.UX >= r2.UX) &&
            (r1.UY >= r2.UY) ) return 'V';  // COVERS
        if ((r1.LX < r2.UX) &&
            (r1.LY < r2.UY) &&
            (r1.UX > r2.LX) &&
            (r1.UY > r2.LY) ) return 'O';   // OVERLAP
        if ((r1.LX <= r2.UX) &&
            (r1.LY <= r2.UY) &&
            (r1.UX >= r2.LX) &&
            (r1.UY >= r2.LY) ) return 'M';  // MEET
        return 'D';                         // DISJOIN
    }

    /** Calculates the converse of a given topological relation.
    */

    public static char converseTopological(char t)
    {
        switch (t)
        {
                case 'D':
                    return 'D';
                case 'O':
                    return 'O';
                case 'M':
                    return 'M';
                case 'E':
                    return 'E';
                case 'V':
                    return 'B';
                case 'B':
                    return 'V';
                case 'C':
                    return 'I';
                case 'I':
                    return 'C';
                default:
                    return ' ' ;
        }
    }
}