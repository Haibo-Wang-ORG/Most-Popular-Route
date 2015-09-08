package rstar;

/* Modified by Josephine Wong 23 Nov 1997
   Improve User Interface of the program
*/

public class Constants
{
	public static final boolean recordLeavesOnly = false;
	
/* These values are now set by the users - see UserInterface module.*/
	// for experimental rects
	public static final int MAXCOORD = 100;
	public static final int MAXWIDTH = 60;
	public static final int NUMRECTS = 200;
	public static final int DIMENSION = 2;
	public static final int BLOCKLENGTH = 1024;
	public static final int CACHESIZE = 128;

    // for queries
    static final int RANGEQUERY = 0;
    static final int POINTQUERY = 1;
    static final int CIRCLEQUERY = 2;
    static final int RINGQUERY = 3;    
    static final int CONSTQUERY = 4;
    
     // for buffering
    static final int SIZEOF_BOOLEAN = 1;
    static final int SIZEOF_SHORT = 2;
    static final int SIZEOF_CHAR = 1;
    static final int SIZEOF_BYTE = 1;
    static final int SIZEOF_FLOAT = 4;
    static final int SIZEOF_INT = 4;

    public final static int RTDataNode__dimension = 2;

    public final static float MAXREAL = (float)9.99e20;
    public final static int MAX_DIMENSION = 256;

    // for comparisons
    public final static float min(float a, float b) {return (a < b)? a : b;}
    public final static int min(int a, int b) {return (a < b)? a : b;}
    public final static float max(float a, float b) {return (a > b)? a : b;}
    public final static int max(int a, int b) {return (a > b)? a : b;}

    // for comparing mbrs
    public final static int OVERLAP=0;
    public final static int INSIDE=1;
    public final static int S_NONE=2;

    // for the insert algorithm
    public final static int SPLIT=0;
    public final static int REINSERT=1;
    public final static int NONE=2;

    // for header blocks
    public final static int BFHEAD_LENGTH = SIZEOF_INT*2;

    // sorting criteria
    public final static int SORT_LOWER_MBR = 0; //for mbrs
    public final static int SORT_UPPER_MBR = 1; //for mbrs
    public final static int SORT_CENTER_MBR = 2; //for mbrs
    public final static int SORT_MINDIST = 3; //for branchlists

    public final static int BLK_SIZE=4096;
    public final static int MAXLONGINT=32768;
    public final static int NUM_TRIES=10;

    // for errors
    public static void error (String msg, boolean fatal)
    {
        System.out.println(msg);
        if (fatal) System.exit(1);
    }

    // returns the d-dimension area of the mbr
    public static float area(int dimension, float mbr[])
    {
        int i;
        float sum;

        sum = (float)1.0;
        for (i = 0; i < dimension; i++)
                sum *= mbr[2*i+1] - mbr[2*i];

        return sum;
    }

    // returns the margin of the mbr. That is the sum of all projections
    // to the axes
    public static float margin(int dimension, float mbr[])
    {
        int i;
        int ml, mu, m_last;
        float sum;

        sum = (float)0.0;
        m_last = 2*dimension;
        ml = 0;
        mu = ml + 1;
        while (mu < m_last)
        {
                sum += mbr[mu] - mbr[ml];
                ml += 2;
                mu += 2;
        }

        return sum;
    }

    // ist ein Skalar in einem Intervall ?
    public static boolean inside(float p, float lb, float ub)
    {
        return (p >= lb && p <= ub);
    }

    // ist ein Vektor in einer Box ?
    public static boolean inside(float v[], float mbr[], int dimension)
    {
        int i;

        for (i = 0; i < dimension; i++)
                    if (!inside(v[i], mbr[2*i], mbr[2*i+1]))
                        return false;
        return true;
    }

    // calcutales the overlapping area of r1 and r2
    // calculate overlap in every dimension and multiplicate the values
    public static float overlap(int dimension, float r1[], float r2[])
    {
        float sum;
        int r1pos, r2pos, r1last;
        float r1_lb, r1_ub, r2_lb, r2_ub;

        sum = (float)1.0;
        r1pos = 0; r2pos = 0;
        r1last = 2 * dimension;

        while (r1pos < r1last)
        {
                    r1_lb = r1[r1pos++];
                    r1_ub = r1[r1pos++];
                    r2_lb = r2[r2pos++];
                    r2_ub = r2[r2pos++];

            // calculate overlap in this dimension

            if (inside(r1_ub, r2_lb, r2_ub))
            // upper bound of r1 is inside r2
                    {
                if (inside(r1_lb, r2_lb, r2_ub))
                // and lower bound of r1 is inside
                    sum *= (r1_ub - r1_lb);
                else
                    sum *= (r1_ub - r2_lb);
                    }
                    else
                    {
                if (inside(r1_lb, r2_lb, r2_ub))
                        // and lower bound of r1 is inside
                                    sum *= (r2_ub - r1_lb);
                        else
                        {
                                    if (inside(r2_lb, r1_lb, r1_ub) && inside(r2_ub, r1_lb, r1_ub))
                            // r1 contains r2
                                        sum *= (r2_ub - r2_lb);
                                    else
                                    // r1 and r2 do not overlap
                                        sum =    (float)0.0;
                        }
                    }
        }
        return sum;
    }

    // enlarge r in a way that it contains s
    public static void enlarge(int dimension, float mbr[], float r1[], float r2[])
    {
        int i;

        //mbr = new float[2*dimension];
        for (i = 0; i < 2*dimension; i += 2)
        {
                    mbr[i]   = min(r1[i],   r2[i]);
                    mbr[i+1] = max(r1[i+1], r2[i+1]);
        }

        /*System.out.println("Enlarge was called with parameters:");
        System.out.println("r1 = " + r1[0] + " " + r1[1] + " " + r1[2] + " " + r1[3]);
        System.out.println("r2 = " + r2[0] + " " + r2[1] + " " + r2[2] + " " + r2[3]);
        System.out.println("r1 = " + mbr[0] + " " + mbr[1] + " " + mbr[2] + " " + mbr[3]);
        */
            //#ifdef CHECK_MBR
            //   check_mbr(dimension,*mbr);
            //#endif
    }

    /**
    * returns true if the two mbrs intersect
    */
    public static boolean section(int dimension, float mbr1[], float mbr2[])
    {
        int i;

        for (i = 0; i < dimension; i++)
        {
            if (mbr1[2*i] > mbr2[2*i + 1] || mbr1[2*i + 1] < mbr2[2*i])
                return false;
        }
        return true;
    }

    /**
    * returns true if the specified mbr intersects the specified circle
    */
    public static boolean section_c(int dimension, float mbr1[], Object center, float radius)
    {
        float r2;

        r2 = radius * radius;

        // if MBR contains circle center (MINDIST) return true
        // if r2>MINDIST return true
        //if ((MINDIST(center,mbr1) != 0) ||
        //    (((r2 - MINDIST(center,mbr1)) < (float)1.0e-8)))
        if ((r2 - MINDIST(center,mbr1)) < (float)1.0e-8)
                return false;
        else
                return true;
    }
    
    //new function
    public static boolean section_c_2(int dimension, float mbr1[], Object center, float radius)
    {
        if (radius < MINDIST(center,mbr1))
                return false;
        else
                return true;
    }
    
    /**
    * returns true if the specified mbr intersects the specified circle
    */
    public static boolean section_ring(int dimension, float mbr1[], Object center, float radius1, float radius2)
    {
        float r_c1; //inner circle radius
        float r_c2; //outer circle radius
        
        if (radius1 < radius2)
        {      
            r_c1 = radius1 * radius1;
            r_c2 = radius2 * radius2;
        }
        else
        {
            r_c1 = radius2 * radius2;
            r_c2 = radius1 * radius1;
        }
   
        // if MBR contains circle center (MINDIST) return true
        // if r2>MINDIST return true
        //if ((MINDIST(center,mbr1) != 0) ||
        //    (((r2 - MINDIST(center,mbr1)) < (float)1.0e-8)))
        if (((r_c1 - MAXDIST(center,mbr1)) < (float)1.0e-8) &&
            ((MINDIST(center,mbr1) - r_c2) < (float)1.0e-8))
                return true;
        else
                return false;
    }

    /** This is a generic version of C.A.R Hoare's Quick Sort
    * algorithm.  This will handle arrays that are already
    * sorted, and arrays with duplicate keys.
    *
    * If you think of a one dimensional array as going from
    * the lowest index on the left to the highest index on the right
    * then the parameters to this function are lowest index or
    * left and highest index or right.  The first time you call
    * this function it will be with the parameters 0, a.length - 1.
    *
    * @param a       a Sortable array
    * @param lo0     left boundary of array partition
    * @param hi0     right boundary of array partition
    */
    public static void quickSort(Sortable a[], int lo0, int hi0, int sortCriterion)
    {
      int lo = lo0;
      int hi = hi0;
      Sortable mid;

      if (hi0 > lo0)
      {
              /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         mid = a[ ( lo0 + hi0 ) / 2 ];

         // loop through the array until indices cross
         while( lo <= hi )
         {
            /* find the first element that is greater than or equal to
             * the partition element starting from the left Index.
             */
             while( ( lo < hi0 ) && ( a[lo].lessThan(mid, sortCriterion) ))
                 ++lo;

            /* find an element that is smaller than or equal to
             * the partition element starting from the right Index.
             */
             while( ( hi > lo0 ) && ( a[hi].greaterThan(mid, sortCriterion)))
                 --hi;

            // if the indexes have not crossed, swap
            if( lo <= hi )
            {
               swap(a, lo, hi);
               ++lo;
               --hi;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if( lo0 < hi )
            quickSort( a, lo0, hi, sortCriterion );

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if( lo < hi0 )
            quickSort( a, lo, hi0, sortCriterion );
      }
    }

    //Swaps two entries in an array of objects to be sorted.
    //See Constants.quickSort()
    public static void swap(Sortable a[], int i, int j)
    {
      Sortable T;
      T = a[i];
      a[i] = a[j];
      a[j] = T;

    }

    /**
    * computes the square of the Euclidean distance between 2 points
    */
    public static float objectDIST(PPoint point1, PPoint point2)
    {

        //
        // Berechnet das Quadrat der euklid'schen Metrik.
        // (Der tatsaechliche Abstand ist uninteressant, weil
        // die anderen Metriken (MINDIST und MINMAXDIST fuer
        // die NearestNarborQuery nur relativ nie absolut
        // gebraucht werden; nur Aussagen "<" oder ">" sind
        // relevant.
        //

        float sum = (float)0;
        int i;

        for( i = 0; i < point1.dimension; i++)
            sum += java.lang.Math.pow(point1.data[i] - point2.data[i], 2);

        //return( sqrt(sum) );
        //return(sum);
        return ((float)java.lang.Math.sqrt(sum));
    }

    /**
    * computes the MINDIST between 1 pt and 1 MBR (see [Rous95])
    * the MINDIST ensures that the nearest neighbor from this pt to a
    * rect in this MBR is at at least this distance
    */
    public static float MINDIST(Object pt, float bounces[])
    {
        //
        // Berechne die kuerzeste Entfernung zwischen einem Punkt Point
        // und einem MBR bounces (Lotrecht!)
        //

        PPoint point = (PPoint)pt;
        
        float sum = (float)0.0;
        float r;
        int i;

        for(i = 0; i < point.dimension; i++)
        {
            if (point.data[i] < bounces[2*i])
                r = bounces[2*i];
            else
            {
                if (point.data[i] > bounces[2*i+1])
                            r = bounces[2*i+1];
                else
                            r = point.data[i];
            }

            sum += java.lang.Math.pow(point.data[i] - r , 2);
        }
        //return(sum);
        return((float)java.lang.Math.sqrt(sum));
    }
    
    /**
    * computes the MAXDIST between 1 pt and 1 MBR. It is defined as the
    * maximum distance of a MBR vertex against the specified point
    * Used as an upper bound of the furthest rectangle inside an MBR from a specific
    * point
    */
    public static float MAXDIST(Object pt, float bounces[])
    {
        PPoint point = (PPoint)pt;
        
        float sum = (float)0.0;
        float maxdiff;
        int i;

        for(i = 0; i < point.dimension; i++)
        {
            
            maxdiff = max(java.lang.Math.abs(point.data[i] - bounces[2*i]),
                        java.lang.Math.abs(point.data[i] - bounces[2*i+1]));
            sum += java.lang.Math.pow(maxdiff, 2);
        }
        //return(sum);
        return ((float)java.lang.Math.sqrt(sum));
    }

    /**
    * computes the MINMAXDIST between 1 pt and 1 MBR (see [Rous95])
    * the MINMAXDIST ensures that there is at least 1 object in the MBR
    * that is at most MINMAXDIST far away of the point
    */
    public static float MINMAXDIST(PPoint point, float bounces[])
    {

        // Berechne den kleinsten maximalen Abstand von einem Punkt Point
        // zu einem MBR bounces.
        // Wird benutzt zur Abschaetzung von Abstaenden bei NearestNarborQuery.
        // Kann als Supremum fuer die aktuell kuerzeste Distanz:
        // Alle Punkte mit einem Abstand > MINMAXDIST sind keine Kandidaten mehr
        // fuer den NearestNarbor
        // vgl. Literatur:
        // Nearest Narbor Query v. Roussopoulos, Kelley und Vincent,
        // University of Maryland

        float sum = 0;
        float minimum = (float)1.0e20;
        float S = 0;

        float rmk, rMi;
        int k,i,j;

        for( i = 0; i < point.dimension; i++)
        {
            rMi = (point.data[i] >= (bounces[2*i]+bounces[2*i+1])/2)
                        ? bounces[2*i] : bounces[2*i+1];
            S += java.lang.Math.pow( point.data[i] - rMi , 2 );
        }

        for( k = 0; k < point.dimension; k++)
        {
            rmk = ( point.data[k] <=  (bounces[2*k]+bounces[2*k+1]) / 2 ) ?
                bounces[2*k] : bounces[2*k+1];

            sum = (float)java.lang.Math.pow(point.data[k] - rmk , 2 );

            rMi = (point.data[k] >= (bounces[2*k]+bounces[2*k+1]) / 2 )
                ? bounces[2*k] : bounces[2*k+1];

            sum += S - java.lang.Math.pow(point.data[k] - rMi , 2);

            minimum = min(minimum,sum);
        }

        return(minimum);
        //return ((float)java.lang.Math.sqrt(minimum));
    }

    /*
    public static int sortmindist(Object element1, Object element2)
    {
        //
        // Vergleichsfunktion fuer die Sortierung der BranchList bei der
        // NearestNarborQuery (Sort, Branch and Prune)
        //

        BranchList e1,e2;

        e1 = (BranchList ) element1;
        e2 = (BranchList ) element2;

        if (e1.mindist < e2.mindist)
            return(-1);
        else if (e1.mindist > e2.mindist)
            return(1);
        else
            return(0);
    }*/

    public static int pruneBranchList(float nearest_distanz/*[]*/, Object activebranchList[], int n)
    {

        // Schneidet im Array BranchList alle Eintraege ab, deren Distanz groesser
        // ist als die aktuell naeheste Distanz
        //

        BranchList bl[];

        int i,j,k, aktlast;

        bl = (BranchList[]) activebranchList;

        // 1. Strategie:
        //
        // Ist MINDIST(P,M1) > MINMAXDIST(P,M2), so kann der
        // NearestNeighbor auf keinen Fall mehr in M1 liegen!
        //

        aktlast = n;

        for( i = 0; i < aktlast ; i++ )
        {
            if (bl[i].minmaxdist < bl[aktlast-1].mindist)
                for(j = 0; (j < aktlast) ; j++ )
                    if ((i!=j) && (bl[j].mindist>bl[i].minmaxdist))
                    {
                        aktlast = j;
                        break;
                    }
        }

        // 2. Strategie:
        //
        // nearest_distanz > MINMAXDIST(P,M)
        // -> nearest_distanz = MIMMAXDIST(P,M)
        //

        for( i = 0; i < aktlast; i++)
            if (nearest_distanz/*[0]*/ > bl[i].minmaxdist)
                nearest_distanz/*[0]*/ = bl[i].minmaxdist;


        // 3. Strategie:
        //
        // nearest_distanz < MINDIST(P,M)
        //
        // in M kann der Nearest-Narbor sicher nicht mehr liegen.
        //

        for( i = 0; (i < aktlast) && (nearest_distanz/*[0]*/ >= bl[i].mindist) ; i++);

        aktlast = i;

        // printf("n: %d aktlast: %d \n",n,aktlast);

        return (aktlast);
    }

    public static int testBranchList(Object abL[], Object sL[], int n, int last)
    {

        // Schneidet im Array BranchList alle Eintr"age ab, deren Distanz gr"o"ser
        // ist als die aktuell naeheste Distanz
        //

        BranchList activebranchList[], sectionList[];

        int i,number, aktlast;

        activebranchList = (BranchList[]) abL;
        sectionList      = (BranchList[]) sL;

        aktlast = last;

        for(i = last; i < n ; i++)
        {
            number = activebranchList[i].entry_number;
            if (sectionList[number].section)
            {
            // obwohl vom Abstand her dieser Eintrag gecanceld werden
            // m"usste, wird hier der Eintrag in die ABL wieder
                // aufgenommen, da evtl. ein Treffer der range-Query zu erwarten ist!
                //
                // An der letzten Stelle der Liste kommt der aktuelle Eintrag!
                aktlast++;
                activebranchList[aktlast].entry_number = activebranchList[i].entry_number;
                activebranchList[aktlast].mindist = activebranchList[i].mindist;
                activebranchList[aktlast].minmaxdist = activebranchList[i].minmaxdist;             }

        }

        return (aktlast);

    }

    public static void check_mbr(int dimension, float mbr[])
    {
    }
    
    public static rectangle toRectangle (float rect[])
    {
    	rectangle r = new rectangle(0);
    	
    	r.LX = (int) rect[0];
    	r.UX = (int) rect[1];
    	r.LY = (int) rect[2];
    	r.UY = (int) rect[3];
    	
    	return r;
    }
}