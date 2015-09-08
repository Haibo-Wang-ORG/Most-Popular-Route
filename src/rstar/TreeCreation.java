package rstar;
/* Modified from Test.java created by Nikos
*/

import java.awt.*;

public class TreeCreation {
    TreeCreation (String filename, int numRects, int dimension, int blockLength, int cacheSize) {
        this.numRects = numRects;
        this.dimension = dimension;
        this.blockLength = blockLength;
        this.cacheSize = cacheSize;
        
        // initialize tree
        rt = new RTree(filename, blockLength, cacheSize, dimension);

        // insert random data into the tree
        Data d;
        for (int i=0; i<numRects; i++)
        {
            // create a new Data with dim=dimension
            d = new Data(dimension, i);
            // create a new rectangle
            rectangle r = new rectangle(i);
            // copy the rectangle's coords into d's data
            d.data = new float[dimension*2];
            d.data[0] = (float)r.LX;
            d.data[1] = (float)r.UX;
            d.data[2] = (float)r.LY;
            d.data[3] = (float)r.UY;
            //d.print();
            rt.insert(d);
        }
        
        // Create the Query Result Window
        //qf = new QueryFrame(rt);
        //qf.show();
        //qf.move(400, 0);

        // Create the Rectangle Display Window
        f = new RectFrame(this);
        f.pack();
        f.show();

    }
    
    TreeCreation (String filename, int cacheSize) {
        //this.numRects = numRects;
        //this.dimension = dimension;
        //this.blockLength = blockLength;
        this.cacheSize = cacheSize;
        
        // initialize tree
        rt = new RTree(filename, cacheSize);

        // Create the Rectangle Display Window
        f = new RectFrame(this);
        f.pack();
        f.show();

    }
    
    public void exit(int exitcode)
    {
        if ((rt != null) && (exitcode == 0))
            rt.delete();
        System.exit(0);
    }

    public RTree rt;
    public RectFrame f;
    //public QueryFrame qf;
    public int displaylevel = 199;
    private int numRects, dimension, blockLength, cacheSize;
}