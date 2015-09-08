package rstar;

/*Last update 29-Nov-97 */

public class relationSet{

    public double D[];          //Distance relations
    public short A;             //Directional relations.
    public short T;             //Topological relations

    /** Constructs a new relationSet. All the directional, Topological,
    *   and Distance relations are allowed.
    */

    public relationSet(){

        D=new double[2];
        D[0]=-1;
        D[1]=-1;
        A=255;             // All the directional relations
        T=255;             // All the topological relations
    }

    /* Returns the converse of the given relationSet
    */

    public static relationSet converse(relationSet r){

        relationSet temp=new relationSet();
        temp.D[0]=r.D[0];
        temp.D[1]=r.D[1];
        temp.A=converseAngle(r.A);
        temp.T=converseTopo(r.T);
        return temp;
    }

    /* Returns the converse of the given set of angle relations
    */

    public static short converseAngle(short angles_){

        short result=0;
        byte  b;

        for (byte n=0;n<8;n++){
            if (getBit(angles_,n)==0) continue;
            if (n<4)
                  b=(byte) (4+n);
            else b=(byte) (n-4);
            result=relationSet.setBit(result,b);
        }
        return result;
    }

    /* Returns the converse of the given set of Topological relations
    */

    public static short converseTopo(short topo_){

        short result=0;
        char c1,c2;

        for (byte n=0;n<8;n++){
            if (getBit(topo_,n)==0) continue;
            c1=topoMapping(n);
            c2=relation.converseTopological(c1);
            result=setBit(result,topoMapping(c2));

        }
        return result;
    }

    /* calculates : 2 in the power of n
    */

    public static short p2(byte n){

        short result;

        result=(short) Math.pow(2,(double) n);
        int res = (int) result;
        return result;
    }

    /* Returns the n-th bit of t ( 0<= n <=7).
    */

    public static int getBit(short t,byte n){
        int t0,t1;

        t0=p2(n);
        t1= t&t0;
        if (t1>0) return 1;
        else return 0;
    }

    /* Sets the n-th bit of t ( 0<= n <=7).
    */

    public static short setBit(short t,byte n){

        int t0,t1;

        t0=p2(n);
        t1=t|t0;

        t=(short) t1;
        return t;
    }

    /* Returns the first non-zero bit of t,
    *  8 if none.
    */

    public static byte getFirstBit(short t){

        for (byte j=0;j<8;j++)
            if (getBit(t,j)==1) return j;
        return 8;
    }

    /* Returns a string containing all directional relations
    *  in the relationSet. Returns a combination of
       "E","NE","N","NW","W","SW","S","SE" .
    */

    public String printDir(){

        String d[]={"E","NE","N","NW","W","SW","S","SE"};
        String s="";

        for (byte n=0;n<8;n++){
            if (getBit(A,n)>0){
                s+=d[n];
                s+=" ";
            }
        }
        return s;
    }

    /* Returns a string containing all directional relations
    *  in the relationSet. Returns a combination of
    *  "D","M","E","I","B","C","V","O"
    */

    public String printTopo(){

        String s="";

        for (byte n=0;n<8;n++){
            if (getBit(T,n)>0) s+=topoMapping(n);

        }
        return s;
    }

    /* Maps the symbol of a topological relation to
    *  a byte in the range 0-7.
    *  Returns 8, if symbol is not a topological relation.
    */

    public static byte topoMapping(char c){

        switch (c){
            case 'D':
                return 0;
            case 'M':
                return 1;
            case 'E':
                return 2;
            case 'I':
                return 3;
            case 'B':
                return 4;
            case 'C':
                return 5;
            case 'V':
                return 6;
            case 'O':
                return 7;
        }
        return 8;
    }

    /* Maps a byte in the range 0-7
    *  to the symbol of a topological relation
    *  Returns ' ', if the given byte is not a valid
    *  topological relation.
    */

    public static char topoMapping(byte n){

        switch (n)
        {
            case 0:
                return 'D';
            case 1:
                return 'M';
            case 2:
                return 'E';
            case 3:
                return 'I';
            case 4:
                return 'B';
            case 5:
                return 'C';
            case 6:
                return 'V';
            case 7:
                return 'O';
        }
        return ' ';
    }


    /* Given a string containing one of the directional relations,
    *  sets the appropriate bit of the member variable.
    */

    public void giveDir(String s)
    {
        String d[]={"E","NE","N","NW","W","SW","S","SE"};
        short t=0;

        for (byte n=0;n<8;n++)
            if (s.compareTo(d[n])==0)
            {
                t=setBit(t,n);
                A=t;
            }
    }

    /*Given a string containing a topological relation
    * sets the appropriate bit of the member variable .
    */

    public void giveTopo(char c){

        short t=0;
        byte n=topoMapping(c);

        T=setBit(t,n);
    }

    /* Computes the intersection of two sets of relations.
    * i.e. intersection(1001,1100)-->1000
    */

    public static short intersection(short r1, short r2){
        short r=0;

        r=(short) (r1&r2);
        return r;
    }

    /* Computes the intersection of two relationSets.
    */

    public static relationSet intersection(relationSet r1, relationSet r2)
    {
            relationSet temp=new relationSet();

            temp.A=(short) (r1.A&r2.A);         // directional relation intersection
            temp.T=(short) (r1.T&r2.T);         // topological relation intersection

        //if both distance ranges defined calculate new range as follows.
        if ((r1.D[0]>-1) && (r2.D[0]>-1))
        {
            temp.D[0]=Math.max(r1.D[0],r2.D[0]);
            temp.D[1]=Math.min(r1.D[1],r2.D[1]);
        }
        //if only one distance range defined then new range is equal to that range.
        if ((r1.D[0]>-1) && (r2.D[0]==-1))
        {
            temp.D[0]=r1.D[0];
            temp.D[1]=r1.D[1];
        }
        if ((r1.D[0]==-1) && (r2.D[0]>-1))
        {
            temp.D[0]=r2.D[0];
            temp.D[1]=r2.D[1];
        }
        // if none of the distance ranges defined then new range also not defined.
        return temp;
    }


    /* Computes the composition of two directional relations.
    *   i,j belong to [0,7]. i.e any of the 8 possible directional
    *   relations.
    *   It is called by dirComposition (relationSet r1, relationSet r2)
    *   if there is no distance relations defined in any of the two given
    *   relationSets.
    */

    public static short dirComposition(byte i, byte j)
    {
        byte n,x,k,l;
        short t=0,t0=0;

        if (i==j)
        {
            t=setBit(t,i);
            return t;
        }
        if (Math.abs(i-j)==4)
        {
            t=255;
            return t;
        }
        n=(byte) Math.min(i,j);
        x=(byte) Math.max(i,j);
        k=(byte) (x-n);
        if ((k==1) || (k==2) || (k==3))
        {
            for (l=n;l<=x;l++)
               t=setBit(t,l);
            return t;
        }
        n+=8;
        byte temp;
        temp=x;
        x=n;
        n=temp;
        k=(byte) (x-n);
        if ((k==1) || (k==2) || (k==3))
        {
            for (l=n;l<=x;l++)
                t=setBit(t,l);
        }
        for (l=0;l<8;l++)
            if (getBit(t,l)>0) t0=setBit(t0,l);
        for (l=8;l<=14;l++)
            if (getBit(t,l)>0) t0=setBit(t0,(byte) (l-8));
        return t0;
    }

    /* Computes the combined distance and directional composition of
    *   two relations.
    *   i,j is the directional relations ,
    *   d1[],d2[] are the distance relations.
    *   It is called by dirComposition (relationSet r1, relationSet r2)
    *   if distance relations are defined in both the two given
    *   relationSets.
    *   Calculations done according to the paper.
    */

    public static relationSet dirComposition
                  (byte i, byte j,double d1[],double d2[])
    {
        byte n,x,k,l;

        short t=0,t0=0;
        relationSet temp=new relationSet();
        double s2=Math.sqrt(2);
  //      System.out.print(i+","+j+":"+d1[0]+" to "+d1[1]+" , "+d2[0]+" to "+d2[1]+".");
        if (i==j)
        {
            t=setBit(t,i);
            temp.A=t;
            temp.D[0]=d1[0]+d2[0];
            temp.D[1]=d1[1]+d2[1];
  //          System.out.println("Ret: A:"+temp.A+" D: "+temp.D[0]+" to "+temp.D[1]);
            return temp;
        }
        if (Math.abs(i-j)==4)
        {
            t=setBit(t,i);
            t=setBit(t,j);
            temp.A=t;
            temp.D[0]=-1;
            temp.D[1]=-1;


            if ( (d1[0]<=d2[1]) && (d1[1]>=d2[0]) )
                temp.D[0]=0; //if overlapping ranges then minimum distance=0.
            else
                temp.D[0]=Math.min(Math.abs(d1[0]-d2[1]),Math.abs(d2[0]-d1[1]));

            temp.D[1]=Math.max(Math.abs(d1[1]-d2[0]),Math.abs(d1[0]-d2[1]));
            return temp;
        }
        n=(byte) Math.min(i,j);
        x=(byte) Math.max(i,j);
        k=(byte) (x-n);
        if ((k==1) || ((n+8-x)==1))
        {
            if ((d1[0]==0) && (d2[0]==0))
            {
                t=setBit(t,j); //these lines take care of the situation
                t=setBit(t,i); // when some  of the ranges is [0,0]
            } // e.g., if composition is NE;N and the first range is [0,0] then the
            if (d2[1]!=0) t=setBit(t,j); // only possible relation is N
            if (d1[1]!=0) t=setBit(t,i);
            temp.A=t;
            temp.D[0]=Math.sqrt(d1[0]*d1[0]+d2[0]*d2[0]+s2*d1[0]*d2[0]);
            temp.D[1]=Math.sqrt(d1[1]*d1[1]+d2[1]*d2[1]+s2*d1[1]*d2[1]);
            return temp;
        }
        if (k==2)
        {
            temp.D[0]=Math.sqrt(d1[0]*d1[0]+d2[0]*d2[0]);
            temp.D[1]=Math.sqrt(d1[1]*d1[1]+d2[1]*d2[1]);
            temp.A=angle2(i,j,d1,d2);
            return temp;
        }
        if (k==3)
        {
            temp.D=dist3(d1,d2);
            temp.A=angle3(i,j,d1,d2);
//            System.out.println("3.1. Ret: A:"+temp.A+" D: "+temp.D[0]+" to "+temp.D[1]);
            return temp;
        }
        if (i>j)
        {
            if ((j+8-i)==2)
            {
                temp.D[0]=Math.sqrt(d1[0]*d1[0]+d2[0]*d2[0]);
                temp.D[1]=Math.sqrt(d1[1]*d1[1]+d2[1]*d2[1]);
                t=angle2((byte) (j+8),i,d2,d1);
            }
            else
            {
                temp.D=dist3(d2,d1);
                t=angle3((byte) (j+8),i,d2,d1);
            }
        }
        else
        {
            if ((i+8-j)==2)
            {
                temp.D[0]=Math.sqrt(d1[0]*d1[0]+d2[0]*d2[0]);
                temp.D[1]=Math.sqrt(d1[1]*d1[1]+d2[1]*d2[1]);
                t=angle2((byte) (i+8),j,d1,d2);
            }
            else
            {
                temp.D=dist3(d1,d2);
                t=angle3((byte) (i+8),j,d1,d2);
            }
        }
        for (l=0;l<8;l++)
            if (getBit(t,l)>0) t0=setBit(t0,l);
        for (l=8;l<=14;l++)
            if (getBit(t,l)>0) t0=setBit(t0,(byte) (l-8));
        temp.A=t0;
 //       System.out.println("Gen. Ret: A:"+temp.A+" D: "+temp.D[0]+" to "+temp.D[1]);
        return temp;
    }

    /* Used by dirComposition(byte,byte,double[],double[]).
    */

    public static double[] dist3(double[] d1, double[] d2)
    {
        double[] d=new double[2];
        double[] m=new double[4];
        double s2=Math.sqrt(2);
        d[0]=1000;
        d[1]=0;
        m[0]=Math.sqrt(d1[0]*d1[0]+d2[0]*d2[0]-s2*d1[0]*d2[0]);
        m[1]=Math.sqrt(d1[1]*d1[1]+d2[1]*d2[1]-s2*d1[1]*d2[1]);
        m[2]=Math.sqrt(d1[1]*d1[1]+d2[0]*d2[0]-s2*d1[1]*d2[0]);
        m[3]=Math.sqrt(d1[0]*d1[0]+d2[1]*d2[1]-s2*d1[0]*d2[1]);
        for (int i=0;i<=3;i++)
        {
           d[0]=Math.min(d[0],m[i]);
           d[1]=Math.max(d[1],m[i]);
        }
        if (((d1[0]*s2/2)>=d2[0]) && ((d1[0]*s2/2)<=d2[1]))
            d[0]=Math.min(d[0],(d1[0]*s2/2));
        if (((d1[1]*s2/2)>=d2[0]) && ((d1[1]*s2/2)<=d2[1]))
            d[0]=Math.min(d[0],(d1[1]*s2/2));
        if (((d2[0]*s2/2)>=d1[0]) && ((d2[0]*s2/2)<=d1[1]))
            d[0]=Math.min(d[0],(d2[0]*s2/2));
        if (((d2[1]*s2/2)>=d1[0]) && ((d2[1]*s2/2)<=d1[1]))
            d[0]=Math.min(d[0],(d2[1]*s2/2));
   //     if ((d1[1]*s2/2)>=d2[1]) d[1]=Math.max(d[1],(d1[1]*s2/2));
  //      if ((d2[1]*s2/2)>=d1[1]) d[1]=Math.max(d[1],(d2[1]*s2/2));
        return d;
    }

    /* Used by dirComposition(byte,byte,double[],double[]).
    */

    public static short angle3(byte i,byte j,double d1[],double d2[])
    {
        if (j>i) return angle3(j,i,d2,d1);

        short t=0;
        byte l;
        double s2=Math.sqrt(2);

        if ((d1[1]==0) && (d2[1]==0))
        {
            for (l=j;l<=i;l++)
                t=setBit(t,l);
            return t;
        }
        if (d2[1]==0)
        {
            t=setBit(t,i);
            return t;
        }
        if (d1[1]==0)
        {
            t=setBit(t,j);
            return t;
        }
        if ((d1[0]==d1[1]) && (d2[0]==d2[1]) && (Math.abs(d1[0]-s2*d2[0])<0.001))
        {
            t=setBit(t,(byte) (i-1));
            return t;
        }
        if ((d1[0]==d1[1]) && (d2[0]==d2[1]) && (Math.abs(d1[0]-(s2*d2[0]/2))<0.001))
        {
            t=setBit(t,(byte) (i-2));
            return t;
        }
        if ((d1[0]==d1[1]) && (d2[0]==d2[1]) && (Math.abs(d2[0]-s2*d1[0])<0.001))
        {
            t=setBit(t,(byte) (i-2));
            return t;
        }
        if ((d1[0]==d1[1]) && (d2[0]==d2[1]) && (Math.abs(d2[0]-(s2*d1[0]/2))<0.001))
        {
            t=setBit(t,(byte) (i-1));
            return t;
        }
        if (d1[0]>s2*d2[1])
        {
            t=setBit(t,i);
            t=setBit(t,(byte) (i-1));
            return t;
        }
        if (d2[0]>s2*d1[1])
        {
            t=setBit(t,j);
            t=setBit(t,(byte) (j+1));
            return t;
        }
        if (d1[1]<(s2*d2[0]/2))
        {
            t=setBit(t,j);
            t=setBit(t,(byte) (j+1));
            return t;
        }
        if (d2[1]<(s2*d1[0]/2))
        {
            t=setBit(t,i);
            t=setBit(t,(byte) (i+1));
            return t;
        }
        if (((s2*d2[1]/2)<d1[0]) && (d1[1]<=s2*d2[0]))
        {
            t=setBit(t,(byte) (i-1));
            t=setBit(t,(byte) (i-2));
            return t;

        }

        if (((s2*d1[1]/2)<d2[0]) && (d2[1]<=s2*d1[0]))
        {
            t=setBit(t,(byte) (i-1));
            t=setBit(t,(byte) (i-2));
            return t;
        }
        if (d1[1]<s2*d2[0])
        {
            for (l=j;l<=i-1;l++)
                t=setBit(t,l);
            return t;
        }
        if (d2[1]<s2*d1[0])
        {
            for (l=(byte) (j+1);l<=i;l++)
                t=setBit(t,l);
            return t;
        }
        if (d1[0]>(s2*d2[1]/2))
        {
            for (l=(byte) (j+1);l<=i;l++)
                t=setBit(t,l);
            return t;
        }
        if (d2[0]>(s2*d1[1]/2))
        {
            for (l=(byte) j;l<=i-1;l++)
                t=setBit(t,l);
            return t;
        }
        for (l=j;l<=i;l++)
                t=setBit(t,l);
        return t;
    }

    /* Used by dirComposition(byte,byte,double[],double[]).
    * Computes the composition direction of two angles that differ 90 degrees.
    */

    public static short angle2(byte i,byte j,double d1[],double d2[])
    {
        if (j>i) return angle2(j,i,d2,d1);
        short t=0;
        byte l;
        double s2=Math.sqrt(2);
        if ((d1[1]==0) && (d2[1]==0))
        {
            for (l=j;l<=i;l++)
                t=setBit(t,l);
            return t;
        }
        if (d2[1]==0)
        {
            t=setBit(t,i);
            return t;
        }
        if (d1[1]==0)
        {
            t=setBit(t,j);
            return t;
        }
        if ((d1[0]==d2[0]) && (d1[1]==d2[1]) && (d1[0]==d1[1]))
        {
            t=setBit(t,(byte) (i-1));
            return t;
        }
        if (d1[1]<d2[0])
        {
            t=setBit(t,(byte) (j+1));
            t=setBit(t,j);
            return t;
        }
        if (d1[0]>d2[1])
        {
            t=setBit(t,(byte) (i-1));
            t=setBit(t,i);
            return t;
        }
        for (l=j;l<=i;l++)
            t=setBit(t,l);
        return t;

    }

    /* Computes the topological composition of two relations
    *   Transforms byte --> char and calls topoComposition(char,char).
    */

    public static short topoComposition(byte i, byte j) // computes the composition of two relations
    {
        return topoComposition(topoMapping(i),topoMapping(j));
    }

    /* Computes the topological composition of the two topological relations
    *  supplied by c1, c2. Uses c1,c2 as indices to the table to retrive their
    *  compsite relation.
    */

    public static short topoComposition(char c1, char c2)
    {
        char relSet[]={
'D','M','E','I','B','C','V','O',
'D','M','I','B','O',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'D','M','E','B','V','O',' ',' ',
'M',' ',' ',' ',' ',' ',' ',' ',
'I','B','O',' ',' ',' ',' ',' ',
'M','I','B','O',' ',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D','M',' ',' ',' ',' ',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'M',' ',' ',' ',' ',' ',' ',' ',
'E',' ',' ',' ',' ',' ',' ',' ',
'I',' ',' ',' ',' ',' ',' ',' ',
'B',' ',' ',' ',' ',' ',' ',' ',
'C',' ',' ',' ',' ',' ',' ',' ',
'V',' ',' ',' ',' ',' ',' ',' ',
'O',' ',' ',' ',' ',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'I',' ',' ',' ',' ',' ',' ',' ',
'I',' ',' ',' ',' ',' ',' ',' ',
'I',' ',' ',' ',' ',' ',' ',' ',
'D','M','E','I','B','C','V','O',
'D','M','I','B','O',' ',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D',' ',' ',' ',' ',' ',' ',' ',
'D','M',' ',' ',' ',' ',' ',' ',
'B',' ',' ',' ',' ',' ',' ',' ',
'I',' ',' ',' ',' ',' ',' ',' ',
'I','B',' ',' ',' ',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'D','M','E','B','V','O',' ',' ',
'D','M','I','B','O',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'C','V','O',' ',' ',' ',' ',' ',
'C',' ',' ',' ',' ',' ',' ',' ',
'E','I','B','C','V','O',' ',' ',
'C','V','O',' ',' ',' ',' ',' ',
'C',' ',' ',' ',' ',' ',' ',' ',
'C',' ',' ',' ',' ',' ',' ',' ',
'C','V','O',' ',' ',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'M','C','V','O',' ',' ',' ',' ',
'V',' ',' ',' ',' ',' ',' ',' ',
'I','B','O',' ',' ',' ',' ',' ',
'E','B','V','O',' ',' ',' ',' ',
'C',' ',' ',' ',' ',' ',' ',' ',
'C','V',' ',' ',' ',' ',' ',' ',
'C','V','O',' ',' ',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'O',' ',' ',' ',' ',' ',' ',' ',
'I','B','O',' ',' ',' ',' ',' ',
'I','B','O',' ',' ',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'D','M','C','V','O',' ',' ',' ',
'D','M','E','I','B','C','V','O' };
        char c;
        byte i,j,l;
        i=topoMapping(c1);
        j=topoMapping(c2);
        short r=0;

        if ((i<8) && (j<8))
        {
            for (int n=0;n<8;n++)
            {
                c=relSet[(i*8+j)*8+n];
                l=topoMapping(c);
                if (l<8) r=setBit(r,l);
            }
            return r;
        }
        return r;
    }

    /* Returns the union of two shorts. (Two sets of relations)
    */

    public static short union(short r1, short r2)
    {
         short r=0;
         r=(short) (r1|r2);
         return r;
    }

    /*--------------------------------------------------------------
    */

    public static relationSet union(relationSet r1, relationSet r2)
    {
        relationSet temp=new relationSet();
        temp.A=union(r1.A,r2.A);
        temp.T=union(r1.T,r2.T);
        if ((r1.D[0]>-1) && (r2.D[0]>-1))
        {
            temp.D[0]=Math.min(r1.D[0],r2.D[0]);
            temp.D[1]=Math.max(r1.D[1],r2.D[1]);
            return temp;
        }
        if ((r1.D[0]==-1) && (r2.D[0]>=0))
        {
            temp.D[0]=r2.D[0];
            temp.D[1]=r2.D[1];
            return temp;
        }
        if ((r2.D[0]==-1) && (r1.D[0]>=0))
        {
            temp.D[0]=r1.D[0];
            temp.D[1]=r1.D[1];
        }
        return temp;
    }

    /* Computes the directional composition of 2 relationSets.
    */

    public static relationSet dirComposition(relationSet r1, relationSet r2)
    {
        boolean dist=false;
        byte i,j;
        relationSet temp=new relationSet();
        relationSet temp2=new relationSet();
        temp.A=0;
        temp.D[0]=-1;
        temp.D[1]=-1;
        double x,y;

        if (((r1.A)==255) || ((r2.A)==255))
        {
            temp.A=255;
            return temp;
        }
        if ((r1.D[0]>=0) && (r2.D[0]>=0)) dist=true;

        for (i=0;i<8;i++)
        {
            if (getBit(r1.A,i)>0)
            {
                for (j=0;j<8;j++)
                {
                    if (getBit(r2.A,j)>0)
                    {
                        if (dist)
                        {   // Direction Composition with distances.
                            temp2=dirComposition(i,j,r1.D,r2.D);
                            temp=union(temp,temp2);
                        }
                        // Direction Composition when no distances are defined.
                        else temp.A=union(temp.A,dirComposition(i,j));
                    }
                }
            }
        }
        return temp;
    }


    /* Computes the topological composition of 2 relationSets
    */

    public static short topoComposition(relationSet r1, relationSet r2)
    {
        short r=0;
        byte i,j;
        if (((r1.T)==255) || ((r2.T)==255))
        {
            r=255;
            return r;
        }

        for (i=0;i<8;i++)
        {
            if (getBit(r1.T,i)>0)
            {
                for (j=0;j<8;j++)
                {
                    if (getBit(r2.T,j)>0)
                        r=union(r,topoComposition(i,j));
                }
            }
        }
        return r;
    }


}