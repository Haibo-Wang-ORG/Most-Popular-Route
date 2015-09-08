package rstar;

////////////////////////////////////////////////////////////////////////
// RTNode
////////////////////////////////////////////////////////////////////////

/**
* RTNode is the parent class of RTDirNode and RTDataNode, which implement
* the intermediate and leaf nodes of the R*-tree, respectively.
*/
public class RTNode
{
	public RTree my_tree;              // pointer to the container R-tree
    protected int capacity;               // max. # of entries
    protected int dimension;			  // dimension
    protected int num_entries;            // # of used entries
    protected boolean dirty;			  // TRUE, if node has to be written
    									  // to disk after a change (dirty bit)
    public int block;                     // corresponding disc block of the node
    public short level;                   // level of the node in the tree
    
    public int get_num()                  // returns # of used entries
    { return num_entries;}
    
    /**
    * RTNode constructor. RTNode objects are not directly instantiated,
    * thus this constructor is invoked only by the RTDirNode and RTDataNode
    * derived classes constructors.
    */
    public RTNode(RTree rt)
    {
        my_tree = rt;
        dimension = rt.dimension; 
        num_entries = 0;
    
        // noch kein Plattenblock vergeben
        block = -1;
    }
    
    /** the split algorithm as described in [Beck90]. The algorithm stores
    * the lower and the upper bounds of the mbr for each dimension to two
    * arrays; sml and smu. It then sorts these two arrays d times, according to
    * one dimension at a time, and finds the best axis according to which the
    * split will occur, by finding the best possible split distribution. The
    * criteria for the best split include the minimization of the margin, area,
    * and overlap between the sibling nodes to be created.
    * The function stores in distribution array the sorted indices of mbrs
    * and returns the index within dimension[] according to which the split
    * should take place.
    * the function returns in distribution[0] an ordering of the mbrs, and in
    * dist the index within this order, with respect to which the split will
    * take place
    * NOTE: the parameter distribution is a reference call to distribution[0][] which will be
    * the distribution array of the split and NOT an array of distributions
    */
    public int split(float mbr[][], int distribution[][])
    {
    //#ifdef SHOWMBR
    //    split_000++;
    //#endif
    
        boolean lu = false;       // indicates whether the split will be done
                                  // with respect to the lower bounds of the mbrs
        int i, j, k, l, s, n, m1;
        int dist = 0;             // the return int that specifies the split index within the distribution array
        int split_axis = 0;       // stores the best axis for split
        SortMbr sml[], smu[];     // store the sorted mbrs
        float minmarg;            // the minimum margin (used to compute best split axis)
        float marg, minover, mindead, dead, over, rxmbr[], rymbr[];
    
        // how much nodes are used?
        n = get_num();
    
        // nodes have to be filled at least 40%
        m1 = (int) ((float)n * 0.40);
        dist = m1; 
    
        // sort by lower value of their rectangles
        // index arrays initialisation
        sml = new SortMbr[n];    //stores the mbrs according to their lower values in a specific dimension(axis)
        smu = new SortMbr[n];    //stores the mbrs according to their upper values in a specific dimension(axis)
        rxmbr = new float[2*dimension];
        rymbr = new float[2*dimension];
    
        // choose split axis according to minimization of margin
        minmarg = Constants.MAXREAL;
        for (i = 0; i < dimension; i++)
        // for each axis
        {
            for (j = 0; j < n; j++)
            {
                // initialize SortMbr arrays
                sml[j] = new SortMbr();
                smu[j] = new SortMbr();
                sml[j].index = smu[j].index = j;
                sml[j].dimension = smu[j].dimension = i;
                sml[j].mbr = smu[j].mbr = mbr[j];
            }
    
            // Sort by lower and upper value perpendicular axis_i
            Constants.quickSort(sml, 0, sml.length - 1, Constants.SORT_LOWER_MBR);
            Constants.quickSort(smu, 0, smu.length - 1, Constants.SORT_UPPER_MBR);
            
            /*
            System.out.print("RTNode split: sml after qsort=");
            for (int m=0; m<n; m++)
                System.out.print(sml[m].index+" ");
            System.out.println("");
            
            System.out.print("RTNode split: smu after qsort=");
            for (int m=0; m<n; m++)
                System.out.print(smu[m].index+" ");
            System.out.println("");
            */
            
            marg = (float)0.0;
            // for all possible distributions R1,R2 of sml
            for (k = 0; k < n - 2*m1 + 1; k++)
            {
                // now calculate margin of R1
                // initialize mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =    Constants.MAXREAL;
                    rxmbr[s+1] = -Constants.MAXREAL;
                }
                // R1 = first m1+k mbrs
                for (l = 0; l < m1+k; l++)
                {
                    // calculate mbr of R1 
                    for (s = 0; s < 2*dimension; s += 2)
                    {
                        rxmbr[s] =   Constants.min(rxmbr[s],   sml[l].mbr[s]);
                        rxmbr[s+1] = Constants.max(rxmbr[s+1], sml[l].mbr[s+1]);
                    }
                }
                // add to marg the margin of the mbr of R1
                marg += Constants.margin(dimension, rxmbr); 
    
                // now calculate margin of R2
                // initialize mbr of R2 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =    Constants.MAXREAL;
                    rxmbr[s+1] = -Constants.MAXREAL;
                }
              
                    // R2 = last n-m1-k mbrs
                for ( ; l < n; l++)
                {
                    // calculate mbr of R1 
                    for (s = 0; s < 2*dimension; s += 2)
                        {
                            rxmbr[s] =   Constants.min(rxmbr[s],   sml[l].mbr[s]);
                            rxmbr[s+1] = Constants.max(rxmbr[s+1], sml[l].mbr[s+1]);
                        }
                }
                // add to marg the margin of the mbr of R2
                marg += Constants.margin(dimension, rxmbr); 
            }
    
            // for all possible distributions of smu
            for (k = 0; k < n - 2*m1 + 1; k++)
            {
                // now calculate margin of R1
                // initialize mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =    Constants.MAXREAL;
                    rxmbr[s+1] = -Constants.MAXREAL;
                }
                // R1 = first m1+k mbrs
                for (l = 0; l < m1+k; l++)
                {
                    // calculate mbr of R1
                    for (s = 0; s < 2*dimension; s += 2)
                    {
                        rxmbr[s] =   Constants.min(rxmbr[s],   smu[l].mbr[s]);
                        rxmbr[s+1] = Constants.max(rxmbr[s+1], smu[l].mbr[s+1]);
                    }
                }
                // add to marg the margin of the mbr of R1
                marg += Constants.margin(dimension, rxmbr); 
    
                // now calculate margin of R2
                // initialize mbr of R2 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =    Constants.MAXREAL;
                    rxmbr[s+1] = -Constants.MAXREAL;
                }
                // R2 = last n-m1-k mbrs
                for ( ; l < n; l++)
                {
                    // calculate mbr of R1
                    for (s = 0; s < 2*dimension; s += 2)
                    {
                        rxmbr[s] =   Constants.min(rxmbr[s],   smu[l].mbr[s]);
                        rxmbr[s+1] = Constants.max(rxmbr[s+1], smu[l].mbr[s+1]);
                    }
                }
                // add to marg the margin of the mbr of R1      
                marg += Constants.margin(dimension, rxmbr); 
            }
    
            //System.out.println("Margin for dimension " + i + ": " + marg);
            // now marg contains the sum of all margins of all distributions according
            // to the current dimension (axis)
            // is actual margin better than optimum?
            if (marg < minmarg)
            {
                // set split_axis to the best so far
                split_axis = i;
                // set minimum margin to the best so far
                minmarg = marg;
            }
    
        }
        
        //Now we have found the best axis to split, according to the minimization of the
        //sum of the margins of all possible distributions with respect to this axis.
    
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 
        //System.out.println("split axis = " + split_axis + "\n");
               
        // Now we choose the best distribution for split axis 
        // according to minimum overlap and minimum dead space
        for (j = 0; j < n; j++)
        {
            sml[j].index = smu[j].index = j;
            sml[j].dimension = smu[j].dimension = split_axis;
            sml[j].mbr = smu[j].mbr = mbr[j];
        }
               
        // Sort by lower and upper value perpendicular split axis
        Constants.quickSort(sml, 0, sml.length - 1, Constants.SORT_LOWER_MBR);
        Constants.quickSort(smu, 0, smu.length - 1, Constants.SORT_UPPER_MBR);

        minover = Constants.MAXREAL;
        mindead = Constants.MAXREAL;
        // for all possible distributions R1,R2 of sml and snu
        for (k = 0; k < n - 2*m1 + 1; k++)
        {
            // lower end sort
            // now calculate margin of R1
            // initialize mbr of R1 
            dead = (float)0.0;
            for (s = 0; s < 2*dimension; s += 2)
            {
                rxmbr[s] =    Constants.MAXREAL;
                rxmbr[s+1] = -Constants.MAXREAL;
            }
            // R1 = first m1+k mbrs
            for (l = 0; l < m1+k; l++)
            {
                // calculate mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =   Constants.min(rxmbr[s],   sml[l].mbr[s]);
                    rxmbr[s+1] = Constants.max(rxmbr[s+1], sml[l].mbr[s+1]);
                }
              dead -= Constants.area(dimension, sml[l].mbr);
            }
            dead += Constants.area(dimension, rxmbr);
        
            // now calculate margin of R2
            // initialize mbr of R2 
            for (s = 0; s < 2*dimension; s += 2)
            {
                rymbr[s] =    Constants.MAXREAL;
                rymbr[s+1] = -Constants.MAXREAL;
            }
            // R2 = last n-m1-k mbrs
            for ( ; l < n; l++)
            {
                // calculate mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rymbr[s] =   Constants.min(rymbr[s],   sml[l].mbr[s]);
                    rymbr[s+1] = Constants.max(rymbr[s+1], sml[l].mbr[s+1]);
                }
                dead -= Constants.area(dimension, sml[l].mbr);
            }
      
            dead += Constants.area(dimension, rymbr);
            //System.out.println("Dead area for sml distribution " + k + ": " + dead);
            over = Constants.overlap(dimension, rxmbr, rymbr);
            //System.out.println("overlap for sml distribution " + k + ": " + over);
        
            // is the overlap smaller than the best so far? 
            // if overlaps are the same, is the dead space smaller?
            if ((over < minover) || (over == minover) && dead < mindead)
            {
                // update best overlap and dead space
                minover = over;
                mindead = dead;
                dist = m1+k;  // update best distribution info
                lu = true;  // best distribution is with respect to the lower mbr bounds
            }
                
            // upper sort
            // now calculate margin of R1
            // initialize mbr of R1 
            dead = (float)0.0;
            for (s = 0; s < 2*dimension; s += 2)
            {
                rxmbr[s] =    Constants.MAXREAL;
                rxmbr[s+1] = -Constants.MAXREAL;
            }
            // R1 = first m1+k mbrs
            for (l = 0; l < m1+k; l++)
            {
                // calculate mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                    rxmbr[s] =   Constants.min(rxmbr[s],   smu[l].mbr[s]);
                    rxmbr[s+1] = Constants.max(rxmbr[s+1], smu[l].mbr[s+1]);
                }
              dead -= Constants.area(dimension, smu[l].mbr);
            }
            dead += Constants.area(dimension, rxmbr);
                    
            // now calculate margin of R2
            // initialize mbr of R2 
            for (s = 0; s < 2*dimension; s += 2)
            {
                rymbr[s] =    Constants.MAXREAL;
                rymbr[s+1] = -Constants.MAXREAL;
            }
            // R2 = last n-m1-k mbrs
            for ( ; l < n; l++)
            {
                // calculate mbr of R1 
                for (s = 0; s < 2*dimension; s += 2)
                {
                            rymbr[s] =   Constants.min(rymbr[s],   smu[l].mbr[s]);
                            rymbr[s+1] = Constants.max(rymbr[s+1], smu[l].mbr[s+1]);
                }
              dead -= Constants.area(dimension, smu[l].mbr);
            }
            dead += Constants.area(dimension, rxmbr);
            //System.out.println("Dead area for smu distribution " + k + ": " + dead);
            over = Constants.overlap(dimension, rxmbr, rymbr);
            //System.out.println("Overlap for sml distribution " + k + ": " + over);
        
            // is the overlap smaller than the best so far? 
            // if overlaps are the same, is the dead space smaller?
            if ((over < minover) || (over == minover) && dead < mindead)
            {
                // update best overlap and dead space
                minover = over;
                mindead = dead;
                dist = m1+k; // update best distribution info
                lu = false;  // best distribution is with respect to the upper mbr bounds
            }
        }
    
        // calculate best distribution
        // sml or smu hold the best sorting to be stored to distribution
        // and dist holds the best splitting index in this sorting
        distribution[0] = new int[n];    
        for (i = 0; i < n; i++)
        {
            if (lu)
                distribution[0][i] = sml[i].index;
            else
                distribution[0][i] = smu[i].index;
            //System.out.println("distribution " + i + ": " + distribution[0][i]);
        }
            
        // return the index in the distribution array that specifies the split
        return dist;
    }
}