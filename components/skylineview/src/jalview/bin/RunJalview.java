package jalview.bin;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import jalview.gui.*;

public class RunJalview
{
    public static void main(String[] args)
    {
	String file = "testpw.pir";
	String protocol = "File";
	String color = "CLUSTAL";

	try{
	    Cache.initLogger();
	}catch (Exception e) {
	    e.printStackTrace();
	    System.exit(0);
	}

	if (file == null)
	    {
		System.out.println("No files to open!");
		System.exit(1);
	    }
	if (file != null)
	    {
		System.out.println("Opening file: " + file);

		String format = new jalview.io.IdentifyFile().Identify(file, protocol);

		jalview.io.FileLoader fileLoader = new jalview.io.FileLoader();
		AlignFrame af = fileLoader.LoadFileWaitTillLoaded(file, protocol, format);

		jalview.schemes.ColourSchemeI cs =
		    jalview.schemes.ColourSchemeProperty.getColour(af.getViewport().getAlignment(), color);

		af.changeColour(cs);

		JPanel jp = new JPanel();
		jp.add(af);
		JFrame frame = new JFrame("Small");
		frame.setVisible(true);
		frame.setContentPane(jp);
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);

		af.toFront();
		af.setVisible(true);
		af.setClosable(true);
		af.setResizable(true);
		af.setMaximizable(true);
		af.setIconifiable(true);
		af.setFrameIcon(null);
		af.setPreferredSize(new java.awt.Dimension(600, 400));
	    }
    }

}
