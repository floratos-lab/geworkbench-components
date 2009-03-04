package org.geworkbench.components.genspace.rating;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import org.geworkbench.components.genspace.ServerConfig;
import org.geworkbench.components.genspace.bean.RatingBean;
import org.geworkbench.components.genspace.ui.LoginManager;

import org.jdesktop.swingworker.*;

public class StarRatingPanel extends JPanel implements MouseListener{

	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;

	private boolean clickable = true;
	private Star[] stars;
	private double value = 0;
	private int size = SMALL;
	private int id;
	private JLabel title;
	private JLabel ratingInfo;
	private Rater rater;

	private Font titleFont = new Font("Verdana", Font.BOLD, 9);
	private Font ratingFont = new Font("Verdana", Font.PLAIN, 9);
	private JPanel starPanel = new JPanel();
	private JPanel contentPanel = new JPanel();
	private ServerConfig server;
	private String getCommand;
	private String writeCommand;
	private JComponent parent;

	public StarRatingPanel(JComponent parent, ServerConfig server, String getCommand, String writeCommand){
		this(parent, "", -1, server, getCommand, writeCommand);
	}

	public StarRatingPanel(JComponent parent, String titleText, 
			int id, 
			ServerConfig server, 
			String getCommand, 
			String writeCommand){

		rater = new Rater(server, writeCommand, getCommand);

		contentPanel.setBorder(new MatteBorder(2,2,2,2, this.getBackground()));
		add(contentPanel);

		//basic setup
		contentPanel.setLayout(new BorderLayout());
		this.id = id;

		//add title
		title = new JLabel(titleText);
		title.setFont(titleFont);
		contentPanel.add(title, BorderLayout.NORTH);

		//add rating info
		ratingInfo = new JLabel("");
		ratingInfo.setFont(ratingFont);
		contentPanel.add(ratingInfo, BorderLayout.EAST);

		//add stars
		stars = new Star[5];
		for(int i = 0; i < 5; i++) stars[i] = new Star(this, i + 1);
		contentPanel.add(starPanel, BorderLayout.WEST);
		for(int i = 0; i < 5; i++) starPanel.add(stars[i]);
	}

	public void setTitle(String t) {
		this.title.setText(t);
	}

	public void loadRating(final int id){



		this.id = id;
		final String username = LoginManager.getLoggedInUser();

		//see if we can even execute the query
		if (id == -1){
			setVisible(false);
			return;
		}
		else setVisible(true);

		org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
			public Void doInBackground() {

				RatingBean rating = rater.getRating(id, username);

				if (rating != null){
					setVisible(true);
					if (rating.getUserRating() != 0) 
						setClickable(false);
					else setClickable(true);

					setRatingValue(rating.getOverallRating(), rating.getTotalRatings());
				}
				else setVisible(false);

				return null;
			}
		};
		worker.execute();
	}

	public void setRatingValue(double rating, long totalRatings){
		if (totalRatings != 0){
			setStarValue(rating);
			DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

			ratingInfo.setText("(" + twoDigit.format(rating) + 
					" by " + totalRatings + " users.)");
		}
		else {
			setStarValue(0);
			ratingInfo.setText("Not yet rated.");
		}
	}

	public void setStarValue(double value){
		this.value = value;

		for (int i = 1; i <= 5; i++){
			if (value >= i) stars[i - 1].setStar(Star.FULL);
			else if (value > i - 1) stars[i - 1].setStar(Star.HALF);
			else stars[i - 1].setStar(Star.EMPTY);
		}
	}
	
	public JPanel getThisPanel() {
		return this;
	}

	public void rate(final int rating){

		org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
			public Void doInBackground() {


				String username = LoginManager.getLoggedInUser();

				//perform rating here
				RatingBean newRating = rater.writeRating(id, username, rating);

				if (newRating == null){
					JOptionPane.showMessageDialog(null, "There was a problem in sending your rating.  Check your internet connection.");
				}
				else {
					setStarValue(newRating.getOverallRating());
					setRatingValue(newRating.getOverallRating(), newRating.getTotalRatings());
					//user can no longer rate now
					clickable = false;	
					setTitle("Thanks!");
					getThisPanel().repaint();
				}
				return null;
			}
		};
		worker.execute();
	}

	public boolean isClickable(){
		return clickable;
	}

	public void setClickable(boolean c){
		clickable = c;
	}

	public void mouseClicked(MouseEvent e) {
		if (clickable) rate(((Star)e.getComponent()).getValue());
	}

	public void mouseEntered(MouseEvent e) {
		if (!clickable) return;

		int starIndex = ((Star)e.getComponent()).getValue() - 1;
		for (int i = 0; i < 5; i++){
			if (i <= starIndex) stars[i].setStar(Star.FULL);
			else stars[i].setStar(Star.EMPTY);
		}
	}

	public void mouseExited(MouseEvent e) {
		if (!clickable) return;
		setStarValue(value);
	}

	//these aren't needed.
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

}
