package rstar;
/* Written by Josephine Wong 25 November 1997
   Handle the Query Result Window
*/

import java.awt.*;

public class QueryFrame extends Frame {
    QueryFrame (String title, SortedLinList res) {
        super("Query: " + title);

        //this.rt = rt;

        //mbr = new float[4];
        //mbr[0] = 100.0f;
        //mbr[1] = 260.0f;
        //mbr[2] = 100.0f;
        //mbr[3] = 260.0f;

        //res = new SortedLinList();
        //rt.rangeQuery(mbr, res);

        resize(300,250);
        move(350,0);
        ta = new TextArea(res.toString());
        add("Center",ta);
    }
    
    public boolean handleEvent (Event e) {
        if (e.id == Event.WINDOW_DESTROY && e.target == this)
            dispose();
        else
            return super.handleEvent (e);
        return true;
    }

    /*
    public void updateQuery(float[] mbr1) {
        hide();
        for (int i = 0; i <= 3; i++)
            mbr[i] = mbr1[i];
        res.erase();
        res = new SortedLinList();
        rt.rangeQuery(mbr, res);
        ta.replaceText(res.toString(), 0, end);
        show();
    }*/

    /*
    public float[] getMbr() {
        return mbr;
    }*/

    //private float[] mbr;
    //private SortedLinList res;
    //private RTree rt;
    private TextArea ta;
    //private final static int end = 1000;

}