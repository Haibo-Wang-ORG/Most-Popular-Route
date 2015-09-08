package rstar;
/* Modified by Josephine Wong 24 November 1997
   Add Menu Bar and allow changing of display levels.
*/

import java.awt.*;

public class RectFrame extends Frame
{
    RFramedArea framedArea;
    Label label;
    Object controller;
    private TreeCreation t;

    RectFrame(Object controller)
	{
        t = (TreeCreation) controller;

        setTitle("The R* Tree Created");

        // Add the menu bar and menus
        MenuBar mBar = new MenuBar();
        Menu pMenu = new Menu("Program");
        pMenu.add(new MenuItem("Exit"));
        mBar.add(pMenu);
        Menu dMenu = new Menu("Display");
        dMenu.add(new MenuItem("Query results"));
        dMenu.add(new MenuItem("All"));
        dMenu.add(new MenuItem("Level 0"));
        dMenu.add(new MenuItem("Level 1"));
        dMenu.add(new MenuItem("Level 2"));
        dMenu.add(new MenuItem("Level 3"));
        dMenu.add(new MenuItem("Level 4"));
        dMenu.add(new MenuItem("Level 5"));
        mBar.add(dMenu);
        Menu qMenu = new Menu("Query");
        qMenu.add(new MenuItem("Range query"));
        qMenu.add(new MenuItem("Point query"));
        qMenu.add(new MenuItem("Circle query"));
        qMenu.add(new MenuItem("Ring query"));
        qMenu.add(new MenuItem("Constraints query"));
        mBar.add(qMenu);
        setMenuBar(mBar);

        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridBag);

        this.controller = controller;
        framedArea = new RFramedArea(controller);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER; //end row
        gridBag.setConstraints(framedArea, c);
        add(framedArea);
/*
        label = new Label("Select Level of display");
        Choice level = new Choice();
        level.addItem("Level 0");
        level.addItem("Level 1");
        level.addItem("Level 2");
        level.addItem("Level 3");
        level.addItem("Level 4");
        level.addItem("Level 5");
*/
        label = new Label("Current display level: " + t.displaylevel);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        gridBag.setConstraints(label, c);
        add(label);

//        gridBag.setConstraints(level, c);
//        add(level);

        validate();
    }

    public boolean handleEvent (Event e) {
        if (e.id == Event.WINDOW_DESTROY && e.target == this)
            t.exit(0);
        return super.handleEvent(e);
    }
    
    public boolean action (Event e, Object o) {
    // Handle user selection of menu items.
        if (e.target instanceof MenuItem) {
            if (o.equals("Exit"))
                t.exit(0);
            if (o.equals("Query results"))
                redrawRect(200);
            if (o.equals("All"))
                redrawRect(199);
            else if (o.equals("Level 0"))
                redrawRect(0);
            else if (o.equals("Level 1"))
                redrawRect(1);
            else if (o.equals("Level 2"))
                redrawRect(2);
            else if (o.equals("Level 3"))
                redrawRect(3);
            else if (o.equals("Level 4"))
                redrawRect(4);
            else if (o.equals("Level 5"))
                redrawRect(5);
            else if (o.equals("Range query"))
                showQueryDlg(Constants.RANGEQUERY);
            else if (o.equals("Point query"))
                showQueryDlg(Constants.POINTQUERY);
            else if (o.equals("Circle query"))
                showQueryDlg(Constants.CIRCLEQUERY);
            else if (o.equals("Ring query"))
                showQueryDlg(Constants.RINGQUERY);
            else if (o.equals("Constraints query"))
                showQueryDlg(Constants.CONSTQUERY);
        }
        else
            return super.action(e, o);
        return true;
    }

    private void redrawRect (int level) {
    // redraw the canvas if display level is changed.
        t.displaylevel = level;
        framedArea.area.repaint();
        label.setText("Current display level: " + t.displaylevel);
    }

    private void showQueryDlg(int querytype) {
    // show query dialog for new query
        QueryDialog qDlg = new QueryDialog(querytype, controller);
        qDlg.show();
    }

    /*
    public void drawRect(rectangle r)
    {
        framedArea.drawRect(r);
    }*/
}
        
        
        
        
		
		