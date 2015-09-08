package rstar;

import java.awt.*;

public class Test
{
	RTree rt;

    static final int NUMRECTS = 200;

    Test()
    {
        // initialize tree
        rt = new RTree(".\\rtree.dat", Constants.CACHESIZE);
    }

    public static void main(String argv[])
	{
		Test t = new Test();

        SortedLinList res = new SortedLinList();
        rectangle r = new rectangle(0);
        r.LX = 10;
        r.UX = 75;
        r.LY = 40;
        r.UY = 45;
        
        double dist[] = new double[2];
        dist[0] = (double)0;
        dist[1] = (double)50;
        t.rt.constraints_query(r, dist, /*relationSet.p2((byte)5)*/(short)255, /*(short)(relationSet.p2((byte)5)*/ relationSet.p2((byte)7), res);
        t.displayinwin(res.toString(),100,100);
        
        //RectFrame f = new RectFrame(t);
        //t.displayinwin(f.framedArea.area.outp,200,200);
        //f.pack();
        //f.show();
        //f.framedArea.area.repaint();
        //t.displayinwin(f.framedArea.area.outp,200,200);
        //System.out.println(f.framedArea.area.outp);
    }

    public void displayinwin(String s,int x,int y)
    {
        String qs="",a="";
        int fl=0;

        Frame fq=new Frame();
        fq.resize(300,250);
        TextArea ta=new TextArea(s);
        fq.add("Center",ta);
        fq.show();
        fq.move(x,y);
    }
}