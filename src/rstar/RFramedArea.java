package rstar;

import java.awt.*;

class RFramedArea extends Panel {
    RectArea area;
    public RFramedArea(Object controller) {
        super();

        //Set layout to one that makes its contents as big as possible.
        setLayout(new GridLayout(1,0));

        area = new RectArea(controller);
        add(area);
        validate();
    }

    public Insets Insets() {
        return new Insets(4,4,5,5);
    }

    public void paint(Graphics g) {
        Dimension d = getSize();
        Color bg = getBackground();
 
        g.setColor(bg);
        g.draw3DRect(0, 0, d.width - 1, d.height - 1, true);
        g.draw3DRect(3, 3, d.width - 7, d.height - 7, false);
    }
    
    /*
    public void drawRect(rectangle r)
    {
        area.drawRect(r);
    }*/
}