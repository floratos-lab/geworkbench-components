package org.geworkbench.components.genspace.rating;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.MatteBorder;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.ToolRating;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowRating;
import org.geworkbench.components.genspace.server.wrapper.ToolWrapper;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;

public class StarRatingPanel extends JPanel implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7212044466813119614L;
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;

	private boolean clickable = true;
	private Star[] stars;
	private double value = 0;
//	private int size = SMALL;
	private Workflow workflow;
	private Tool tool;
	private JLabel title;
	private JLabel ratingInfo;

	private Font titleFont = new Font("Verdana", Font.BOLD, 9);
	private Font ratingFont = new Font("Verdana", Font.PLAIN, 9);
	private JPanel starPanel = new JPanel();
	private JPanel contentPanel = new JPanel();

//	private JComponent parent;

	public StarRatingPanel() {
		this("", null);
	}

	public StarRatingPanel(String titleText, Tool tool) {


		contentPanel
				.setBorder(new MatteBorder(2, 2, 2, 2, this.getBackground()));
		add(contentPanel);

		// basic setup
		contentPanel.setLayout(new BorderLayout());
		this.tool = tool;

		// add title
		title = new JLabel(titleText);
		title.setFont(titleFont);
		contentPanel.add(title, BorderLayout.NORTH);

		// add rating info
		ratingInfo = new JLabel("");
		ratingInfo.setFont(ratingFont);
		contentPanel.add(ratingInfo, BorderLayout.EAST);

		// add stars
		stars = new Star[5];
		for (int i = 0; i < 5; i++)
			stars[i] = new Star(this, i + 1);
		contentPanel.add(starPanel, BorderLayout.WEST);
		for (int i = 0; i < 5; i++)
			starPanel.add(stars[i]);
	}

	public void setTitle(String t) {
		this.title.setText(t);
	}
	public void loadRating(Workflow wf) {
		this.workflow = wf;
		// see if we can even execute the query
		if (workflow == null || workflow.getId() < 1) {
			setVisible(false);
			return;
		} else
			setVisible(true);

		SwingWorker<WorkflowRating, Void> worker = new SwingWorker<WorkflowRating, Void>() {
			int evt;
			@Override
			public WorkflowRating doInBackground() {
				evt = GenSpace.getStatusBar().start("Loading rating");
				try {
					return GenSpaceServerFactory.getPrivUsageFacade().getMyWorkflowRating(workflow.getId());
				} catch (Exception e) {
					GenSpace.getStatusBar().stop(evt);
					return null;
				}
			}
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);
				WorkflowRating rating = null;
				try {
					rating = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				if (rating == null)
					setClickable(true);
				else
					setClickable(true);

				super.done();
			}
		};
		worker.execute();
		
		SwingWorker<Workflow, Void> worker2 = new SwingWorker<Workflow, Void>() {
			@Override
			public Workflow doInBackground() {
				try {
					return GenSpaceServerFactory.getPrivUsageFacade().getWorkflow(workflow.getId());
				} catch (Exception e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return null;
				}
			}
			@Override
			protected void done() {

				Workflow rating = null;
				try {
					rating = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					return;
				}
				if(rating != null)
				{
					WorkflowWrapper rat = new WorkflowWrapper(rating);
					setRatingValue(rat.getOverallRating(),
						rat.getNumRating());
				}
				super.done();
			}
		};
		worker2.execute();
	}
	public void loadRating(final Tool tn) {

		this.tool = tn;
		// see if we can even execute the query
//		if (tn == null || tn.getId() < 1) {
//			System.out.println("Setting not visible");
//			setVisible(false);
//			return;
//		} else
//			setVisible(true);

		SwingWorker<ToolRating, Void> worker = new SwingWorker<ToolRating, Void>() {
			int evt;
			@Override
			public ToolRating doInBackground() {
				evt = GenSpace.getStatusBar().start("Loading rating");

				try {
					return GenSpaceServerFactory.getPrivUsageFacade().getMyToolRating(tool.getId());
				} catch (Exception e) {
					GenSpace.getStatusBar().stop(evt);
					return null;
				}
			}
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);

				ToolRating rating = null;
				try {
					rating = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				if (rating == null)
					setClickable(true);
				else
					setClickable(true);

				super.done();
			}
		};
		worker.execute();
		

		SwingWorker<Tool, Void> worker2 = new SwingWorker<Tool, Void>() {
			int evt;
			@Override
			public Tool doInBackground() {
				evt = GenSpace.getStatusBar().start("Loading rating");

				try {
					return GenSpaceServerFactory.getPrivUsageFacade().getTool(tool.getId());
				} catch (Exception e) {
					GenSpace.getStatusBar().stop(evt);
					return null;
				}
			}
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);

				Tool rating = null;
				try {
					rating = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				if(rating != null)
				{
					ToolWrapper wrap = new ToolWrapper(rating);
					setRatingValue(wrap.getOverallRating(),
						wrap.getNumRating());
				}
				super.done();
			}
		};
		worker2.execute();
	}

	public void setRatingValue(double rating, long totalRatings) {
		if (totalRatings != 0) {
			setStarValue(rating);
			DecimalFormat twoDigit = new DecimalFormat("#,##0.00");

			ratingInfo.setText("(" + twoDigit.format(rating) + " by "
					+ totalRatings + " users.)");
		} else {
			setStarValue(0);
			ratingInfo.setText("Not yet rated.");
		}
	}

	public void setStarValue(double value) {
		this.value = value;

		for (int i = 1; i <= 5; i++) {
			if (value >= i)
				stars[i - 1].setStar(Star.FULL);
			else if (value > i - 1)
				stars[i - 1].setStar(Star.HALF);
			else
				stars[i - 1].setStar(Star.EMPTY);
		}
	}

	public JPanel getThisPanel() {
		return this;
	}
	public void rateWorkflow(final int rating) {

		SwingWorker<Workflow, Void> worker = new SwingWorker<Workflow, Void>() {
			int evt;
			@Override
			public Workflow doInBackground() {
				evt = GenSpace.getStatusBar().start("Saving rating");
				try {
					return GenSpaceServerFactory.getPrivUsageFacade().saveWorkflowRating(workflow.getId(),rating);
				} catch (Exception e) {
					GenSpace.getStatusBar().stop(evt);
					return null;
				}
			}
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);
				Workflow result = null;
				try {
					result = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				if (result == null) {
					JOptionPane
							.showMessageDialog(null,
									"There was a problem in sending your rating.  Check your internet connection.");
				} else {
					workflow.setSumRating(result.getSumRating());
					workflow.setNumRating(result.getNumRating());
					setStarValue(rating);
					WorkflowWrapper wrap = new WorkflowWrapper(result);
					setRatingValue(wrap.getOverallRating(),
							wrap.getNumRating());

					// user can no longer rate now
//					clickable = false;
					setTitle("Thanks!");
					getThisPanel().repaint();
				}
				super.done();
			}
		};
		worker.execute();
	}
	public void rateTool(final int rating) {

		SwingWorker<Tool, Void> worker = new SwingWorker<Tool, Void>() {
			int evt;
			@Override
			public Tool doInBackground() {
				evt = GenSpace.getStatusBar().start("Saving rating");

				try {
					return GenSpaceServerFactory.getPrivUsageFacade().saveToolRating(tool.getId(), rating);
				} catch (Exception e) {
					GenSpace.getStatusBar().stop(evt);
					return null;
				}
			}
			@Override
			protected void done() {
				GenSpace.getStatusBar().stop(evt);
				Tool result = null;
				try {
					result = get();
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				if (result == null) {
					JOptionPane
							.showMessageDialog(null,
									"There was a problem in sending your rating.  Check your internet connection.");
				} else {
					RuntimeEnvironmentSettings.tools.put(result.getId(), result);
					setStarValue(rating);
					ToolWrapper wrap = new ToolWrapper(result);
					setRatingValue(wrap.getOverallRating(),
							wrap.getNumRating());
					// user can no longer rate now
//					clickable = false;
					setTitle("Thanks!");
					getThisPanel().repaint();
				}
				super.done();
			}
		};
		worker.execute();
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean c) {
		clickable = c;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (clickable)
			if(workflow != null)
				rateWorkflow(((Star) e.getComponent()).getValue());
			else
				rateTool(((Star) e.getComponent()).getValue());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!clickable)
			return;

		int starIndex = ((Star) e.getComponent()).getValue() - 1;
		for (int i = 0; i < 5; i++) {
			if (i <= starIndex)
				stars[i].setStar(Star.FULL);
			else
				stars[i].setStar(Star.EMPTY);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!clickable)
			return;
		setStarValue(value);
	}

	// these aren't needed.
	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	

}
