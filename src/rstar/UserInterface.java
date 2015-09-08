package rstar;
/* R* Tree Implementation - COMP630C Project
   Implementing User Interface of the program
   Written by Josephine Wong 23 November 1997
*/

import java.awt.*;
import java.awt.event.*;

public class UserInterface extends Frame implements ActionListener {

    public UserInterface () {
        setTitle("R* Tree Creation");

        Panel textPanel = new Panel();
        setFont(new Font("Times", Font.BOLD, 16));
        textPanel.add(new Label("Please enter details of the R* Tree:"));
        add("North", textPanel);

        Panel inputPanel = new Panel();
        setFont(new Font("Times", Font.PLAIN, 16));
        inputPanel.setLayout(new GridLayout(7, 2));
        inputPanel.add(new Label("Tree filename:"));
        inputPanel.add(filename = new TextField("myTree", 10));
        inputPanel.add(new Label("Maximum coordinates:"));
        inputPanel.add(maxCoord = new TextField("300", 10));
        inputPanel.add(new Label("Maximum width:"));
        inputPanel.add(maxWidth = new TextField("60", 10));
        inputPanel.add(new Label("Number of Rectangles:"));
        inputPanel.add(numRects = new TextField("200", 10));
        inputPanel.add(new Label("Dimension:"));
        inputPanel.add(dimension = new TextField("2", 10));
        inputPanel.add(new Label("BlockLength:"));
        inputPanel.add(blockLength = new TextField("256", 10));
        inputPanel.add(new Label("Cache Size:"));
        inputPanel.add(cacheSize = new TextField("128", 10));
        add("Center", inputPanel);

        Panel buttonPanel = new Panel();
        Button createButton = new Button("Create");
        createButton.addActionListener(this);
        buttonPanel.add(createButton);
        Button loadButton = new Button("Load");
        loadButton.addActionListener(this);
        buttonPanel.add(loadButton);
        Button exitButton = new Button("Exit");
        exitButton.addActionListener(this);
        buttonPanel.add(exitButton);
        add("South", buttonPanel);

        setSize(400, 300);
        setLocation(100, 100);
    }

    public static int getMAXCOORD () {
        return MAXCOORD;
    }

    public static int getMAXWIDTH () {
        return MAXWIDTH;
    }

    public static int getNUMRECTS () {
        return NUMRECTS;
    }

    public static int getDIMENSION () {
        return DIMENSION;
    }

    public static int getBLOCKLENGTH () {
        return BLOCKLENGTH;
    }

    public static int getCACHESIZE () {
        return CACHESIZE;
    }

    public boolean handleEvent (Event e) {
        if (e.id == Event.WINDOW_DESTROY && e.target == this)
            System.exit(0);
        return super.handleEvent(e);
    }

    public void actionPerformed (ActionEvent e) {
        if (e.getActionCommand().equals("Create")) {
            if (!filename.getText().equals(""))
            {
                processInput();
                hide();
                TreeCreation tc = new TreeCreation(filename.getText()+".rtr",NUMRECTS, DIMENSION, BLOCKLENGTH, CACHESIZE);
            }
        }
        else if (e.getActionCommand().equals("Load")) {
            FileDialog fd = new FileDialog(this, "Choose an rtree");
            fd.show();
            String fname = fd.getFile();
            if ((fname != null) && (!fname.equals("")))
            {
                hide();
                TreeCreation tc = new TreeCreation(fname,CACHESIZE);
            }
        }
        else if (e.getActionCommand().equals("Exit"))
            System.exit(0);
    }
            
    private void processInput () {
        try {
            MAXCOORD = Integer.parseInt(maxCoord.getText());
            MAXWIDTH = Integer.parseInt(maxWidth.getText());
            NUMRECTS = Integer.parseInt(numRects.getText());
            DIMENSION = Integer.parseInt(dimension.getText());
            BLOCKLENGTH = Integer.parseInt(blockLength.getText());
            CACHESIZE = Integer.parseInt(cacheSize.getText());
        }
        catch (NumberFormatException e) {
            requestFocus ();
        }
    }

    private static int MAXCOORD = 300;
    private static int MAXWIDTH = 60;
    private static int NUMRECTS = 200;
    private static int DIMENSION = 2;
    private static int BLOCKLENGTH = 256;
    private static int CACHESIZE = 128;
    private Object controller;
    private TextField filename,maxCoord, maxWidth, numRects, dimension, blockLength, cacheSize;

    public static void main (String [] args) {
        UserInterface ui = new UserInterface();
        ui.show();
    }

}
