package rstar;
/* Written by Josephine Wong 25 November 1997
   Display dialog for user to enter new query
*/

import java.awt.*;

class QueryDialog extends Dialog {
    public QueryDialog (int querytype, Object controller) {
        super( ((TreeCreation)controller).f, "New Query", true);

        t = (TreeCreation)controller;
        this.querytype = querytype;
        rt = t.rt;
        
        //qf = ((TreeCreation)controller).qf;
        //mbrF = qf.getMbr();

        setFont(new Font("Times", Font.PLAIN, 16));

        Panel iPanel = new Panel();
        switch(querytype)
        {
            case Constants.RANGEQUERY:
                input = new TextField[4];
                iPanel.setLayout(new GridLayout(5, 1));
                iPanel.add(new Label("Enter coordinates of the range:"));
                for (int i=0; i<4; i++)
                    iPanel.add(input[i] = new TextField(Integer.toString(i*10), 5));
                break;
            case Constants.POINTQUERY:
                input = new TextField[2];
                iPanel.setLayout(new GridLayout(3, 1));
                iPanel.add(new Label("Enter coordinates of the point:"));
                for (int i=0; i<2; i++)
                    iPanel.add(input[i] = new TextField(Integer.toString(i*10), 5));
                break;
            case Constants.CIRCLEQUERY:
                input = new TextField[3];
                iPanel.setLayout(new GridLayout(5, 1));
                iPanel.add(new Label("Enter coordinates of the circle center:"));
                for (int i=0; i<2; i++)
                    iPanel.add(input[i] = new TextField(Integer.toString((i+20)*10), 5));
                iPanel.add(new Label("Enter value of the circle radius:"));
                iPanel.add(input[2] = new TextField(Integer.toString(10), 5));
                break;
            case Constants.RINGQUERY:
                input = new TextField[4];
                iPanel.setLayout(new GridLayout(6, 1));
                iPanel.add(new Label("Enter coordinates of the ring center:"));
                for (int i=0; i<2; i++)
                    iPanel.add(input[i] = new TextField(Integer.toString((i+20)*10), 5));
                iPanel.add(new Label("Enter value of the circle two radius's:"));
                for (int i=2; i<4; i++)
                    iPanel.add(input[i] = new TextField(Integer.toString(i*10), 5));
                break;
            case Constants.CONSTQUERY:
                input = new TextField[6];
                iPanel.setLayout(new GridLayout(26, 1));
                
                iPanel.add(new Label("object:"));
                //iPanel.add(new Label(""));
                //iPanel.add(new Label("X0="));
                iPanel.add(input[0] = new TextField("10", 5));
                //iPanel.add(new Label("X1="));
                iPanel.add(input[1] = new TextField("70", 5));
                //iPanel.add(new Label("Y0="));
                iPanel.add(input[2] = new TextField("20", 5));
                //iPanel.add(new Label("Y1="));
                iPanel.add(input[3] = new TextField("40", 5));

                iPanel.add(new Label("distance:"));
                //iPanel.add(new Label(""));
                //iPanel.add(new Label("0="));
                iPanel.add(input[4] = new TextField("0", 5));
                //iPanel.add(new Label("1="));
                iPanel.add(input[5] = new TextField("140", 5));
                
                constraints = new Checkbox[16];
                iPanel.add(new Label("direction:"));
                constraints[0] = new Checkbox("E");
                iPanel.add(constraints[0]);
		        constraints[1] = new Checkbox("NE");
		        iPanel.add(constraints[1]);
		        constraints[2] = new Checkbox("N");
		        iPanel.add(constraints[2]);
		        constraints[3] = new Checkbox("NW");
		        iPanel.add(constraints[3]);
		        constraints[4] = new Checkbox("W");
		        iPanel.add(constraints[4]);
		        constraints[5] = new Checkbox("SW");
		        iPanel.add(constraints[5]);
		        constraints[6] = new Checkbox("S");
		        iPanel.add(constraints[6]);
		        constraints[7] = new Checkbox("SE");
		        iPanel.add(constraints[7]);
		        iPanel.add(new Label("topology:"));
                constraints[8] = new Checkbox("D");
                iPanel.add(constraints[8]);
		        constraints[9] = new Checkbox("M");
		        iPanel.add(constraints[9]);
		        constraints[10] = new Checkbox("E");
		        iPanel.add(constraints[10]);
		        constraints[11] = new Checkbox("I");
		        iPanel.add(constraints[11]);
		        constraints[12] = new Checkbox("B");
		        iPanel.add(constraints[12]);
		        constraints[13] = new Checkbox("C");
		        iPanel.add(constraints[13]);
		        constraints[14] = new Checkbox("V");
		        iPanel.add(constraints[14]);
		        constraints[15] = new Checkbox("O");
		        iPanel.add(constraints[15]);
		        
                break;
        }
        
        add("Center", iPanel);

        Panel bPanel = new Panel();
        bPanel.add(new Button("Ok"));
        bPanel.add(new Button("Cancel"));
        add("South", bPanel);

        if (querytype == Constants.CONSTQUERY) resize(300, 700);
        	else resize(300, 200);
        move(300, 0);
    }

    public boolean action (Event e, Object o) {
        if (o.equals("Ok")) {
            processInput();
        }
        else if (o.equals("Cancel")) {
            this.dispose();
            //qf.show();
        }
        else
            super.action (e, o);
        return true;
    }

    public boolean handleEvent (Event e) {
        if (e.id == Event.WINDOW_DESTROY && e.target == this)
            dispose();
        else
            return super.handleEvent (e);
        return true;
    }

    private void processInput() {
        SortedLinList res;
        PPoint p;
        float[] mbr;
        
        switch (querytype)
        {
            case Constants.RANGEQUERY:
                mbr = new float[4];
                for (int i=0; i<4; i++)
                    try
                    {
                        mbr[i] = Float.valueOf(input[i].getText()).floatValue();
                    }
                    catch (NumberFormatException e)
                    {
                        requestFocus();
                    }

                res = new SortedLinList();
                rt.rangeQuery(mbr, res);
                System.out.println("query finished!");
                (new QueryFrame("Range - " + input[0].getText() + ", "+ input[1].getText() + ", "+ input[2].getText() + ", "+ input[3].getText() + " #PA=" + rt.page_access, res)).show();
                
                /*
                t.f.framedArea.area.queryres = new SortedLinList();
                for (Object obj = res.get_first(); obj != null; obj = res.get_next())
		        {
		        	t.f.framedArea.area.queryres.append(obj);
	            }*/

                t.f.framedArea.area.queryres = res;
                t.f.framedArea.area.drawRange(mbr);
                break;
            case Constants.POINTQUERY:
                p = new PPoint(2);
                
                for (int i=0; i<2; i++)
                    try
                    {
                        p.data[i] = Float.valueOf(input[i].getText()).floatValue();
                    }
                    catch (NumberFormatException e)
                    {
                        requestFocus();
                    }

                res = new SortedLinList();
                rt.point_query(p, res);
                t.f.framedArea.area.queryres = res;
                (new QueryFrame("Point - " + input[0].getText() + ", "+ input[1].getText()+ " #PA=" + rt.page_access, res)).show();
                break;
            case Constants.CIRCLEQUERY:
                p = new PPoint(2);
                
                for (int i=0; i<2; i++)
                    try
                    {
                        p.data[i] = Float.valueOf(input[i].getText()).floatValue();
                    }
                    catch (NumberFormatException e)
                    {
                        requestFocus();
                    }
                    
                float radius=0;
                try
                {
                    radius = Float.valueOf(input[2].getText()).floatValue();
                }
                catch (NumberFormatException e)
                {
                    requestFocus();
                }
                
                res = new SortedLinList();
                rt.rangeQuery(p, radius, res);
                t.f.framedArea.area.queryres = res;
                t.f.framedArea.area.drawRing(p, radius, 0);
                (new QueryFrame("Circle - " + input[0].getText() + ", "+ input[1].getText() + ", "+ input[2].getText()+ " #PA=" + rt.page_access, res)).show();
                break;
            case Constants.RINGQUERY:
                p = new PPoint(2);
                
                for (int i=0; i<2; i++)
                    try
                    {
                        p.data[i] = Float.valueOf(input[i].getText()).floatValue();
                    }
                    catch (NumberFormatException e)
                    {
                        requestFocus();
                    }
                    
                float radius1=0;
                try
                {
                    radius1 = Float.valueOf(input[2].getText()).floatValue();
                }
                catch (NumberFormatException e)
                {
                    requestFocus();
                }
                
                float radius2=0;
                try
                {
                    radius2 = Float.valueOf(input[3].getText()).floatValue();
                }
                catch (NumberFormatException e)
                {
                    requestFocus();
                }
                
                res = new SortedLinList();
                rt.ringQuery(p, radius1, radius2 , res);
                t.f.framedArea.area.drawRing(p, radius1, radius2);
                t.f.framedArea.area.queryres = res;
                (new QueryFrame("Ring - " + input[0].getText() + ", "+ input[1].getText() + ", "+ input[2].getText() + ", "+ input[3].getText()+ " #PA=" + rt.page_access, res)).show();
                break;
            case Constants.CONSTQUERY:
                mbr = new float[4];
                for (int i=0; i<4; i++)
                    try
                    {
                        mbr[i] = Float.valueOf(input[i].getText()).floatValue();
                    }
                    catch (NumberFormatException e)
                    {
                        requestFocus();
                    }

                rectangle rect = new rectangle(0);
                
                try
                {
                    rect.LX = (int)Float.valueOf(input[0].getText()).floatValue();
                    rect.UX = (int)Float.valueOf(input[1].getText()).floatValue();
                    rect.LY = (int)Float.valueOf(input[2].getText()).floatValue();
                    rect.UY = (int)Float.valueOf(input[3].getText()).floatValue();
                }
                catch (NumberFormatException e)
                {
                    requestFocus();
                }
                
                double dist[] = new double[2];
                try
                {
                    dist[0] = (double)Float.valueOf(input[4].getText()).floatValue();
                    dist[1] = (double)Float.valueOf(input[5].getText()).floatValue();
                }
                catch (NumberFormatException e)
                {
                    requestFocus();
                }
                
                short direction = 0;
                
                for(int i=0; i<8; i++)
                	if (constraints[i].getState())
                		direction = relationSet.setBit(direction, (byte)i);
                
                if (direction==0) direction=255; //consider default direction unconstrained
                
                short topology = 0;
                
                for(int i=8; i<16; i++)
                	if (constraints[i].getState())
                		topology=relationSet.setBit(topology, (byte)(i-8));
                		
                if (topology==0) topology=255; //consider default topology unconstrained
                              
                res = new SortedLinList();
                rt.constraints_query(rect, dist, direction, topology, res);
                t.f.framedArea.area.drawRange(mbr);
                t.f.framedArea.area.queryres = res;
                (new QueryFrame("Constraints query - " + input[0].getText() + ", "+ input[1].getText() + ", "+ input[2].getText() + ", "+ input[3].getText()+ " #PA=" + rt.page_access, res)).show();
                break;
        }
        this.dispose();
    }

    private TextField input[];
    private Checkbox constraints[];
    //private float mbrF[] = new float[4];
    //private QueryFrame qf;
    private TreeCreation t;
    private int querytype;
    private RTree rt;
}