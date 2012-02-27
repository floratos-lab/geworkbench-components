package org.geworkbench.components.genspace.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowTool;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.components.genspace.ui.graph.myGraph;
import org.geworkbench.components.genspace.ui.graph.myStackLayout;
import org.geworkbench.engine.config.VisualPlugin;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;

/**
 * This is used to display received workflows.
 * 
 * @author jon
 * 
 */
public class WorkflowVisualizationPanel extends JPanel implements VisualPlugin {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3300246926475166675L;
	private WorkflowVisualizationPopup popup = new WorkflowVisualizationPopup();

	private myGraph graph;
	private mxGraphComponent graphComponent;
	private JScrollPane scroller = new JScrollPane();;
	private boolean redrawing = false;
	public WorkflowVisualizationPanel()
	{
		setOpaque(false);
		new Timer();
		add(scroller, BorderLayout.CENTER);
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {				
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if(!redrawing)
				{
					redrawing = true;
					if(graphComponent != null)
						graphComponent.setPreferredSize(getSize());
					SwingWorker<Void,Void> wrkr = new SwingWorker<Void, Void>()
							{
						int evt;
						protected void done() {
							GenSpace.getStatusBar().stop(evt);
							redrawing = false;
						};
						protected Void doInBackground(){
							evt = GenSpace.getStatusBar().start("Resizing graph");
							refreshLayout();
							return null;
						};
							};
							wrkr.execute();
				}
			}

			@Override
			public void componentMoved(ComponentEvent e) {				
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}
	public void clearData() {
		graph = null;
		this.removeAll();
		this.repaint();
	}
	public void render(WorkflowWrapper w)
	{
		render(w, null);
	}
	public void render(WorkflowWrapper w, Tool selected) {
		wkflwCache = new HashMap<Integer, WorkflowWrapper>();
		wkflwCache.put(w.getId(), w);
		initGraph();
		renderSingleWorkflow(w, selected, null);
		layoutAndShowGraph();	
	}
	private void refreshLayout()
	{
		if(graph != null)
		{
			Object parent = graph.getDefaultParent();
			myStackLayout layout = new myStackLayout(graph,false,20,10,10);
			layout.setResizeParent(true);
			layout.execute(pool);

			layout = new myStackLayout(graph,swimlanes.size() == 0,40,10,0);
			if(swimlanes.size() == 0)
			{
				layout.setWrap((int) this.getSize().getWidth() - 60);
			}
			layout.setResizeParent(true);
			layout.execute(parent);

			if(swimlanesBack.size() > 0)
				for(mxICell i : swimlanes.values())
				{
					WorkflowWrapper parentW = wkflwCache.get(swimlanesBack.get(i).getCachedParentId());
					int drawOffset = 0;
					if(parentW != null)
					{
						if(wkflTails.get(parentW) != null)
						{
							drawOffset = (int) (wkflTails.get(parentW).getGeometry().getX()+wkflTails.get(parentW).getGeometry().getWidth());
							Object[] es = graph.getEdgesBetween(wkflTails.get(parentW),i.getChildAt(0));
							if(es.length == 1)
							{
								mxICell edge = (mxICell) es[0];
								ArrayList<mxPoint> pts = new ArrayList<mxPoint>();
								pts.add(new mxPoint(wkflTails.get(parentW).getGeometry().getCenterX() +((mxICell) parent).getGeometry().getX(), (i.getChildAt(0)).getGeometry().getCenterY() + i.getGeometry().getY()));
								edge.getGeometry().setPoints(pts);
							}
						}
					}
					layout = new myStackLayout(graph,true,10,drawOffset,0);
					layout.setWrap((int) this.getSize().getWidth() + drawOffset - 60);
					layout.setResizeParent(true);
					layout.execute(i);
					layout.setWrap((int) i.getGeometry().getWidth());
					layout.setResizeParent(true);
					layout.execute(i);
					
				}

			this.removeAll();
			add(graphComponent, BorderLayout.CENTER);
			graphComponent.setVisible(true);
			graphComponent.setPreferredSize(this.getSize());
			revalidate();
			repaint();
		}
	}
	private void layoutAndShowGraph() {

		graph.getModel().endUpdate();

		graphComponent = new mxGraphComponent(graph);
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter()
		{

			public void mouseReleased(MouseEvent e)
			{
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());

				if (cell != null)
				{
					if(cell instanceof mxCell)
					{
						mxCell mx = (mxCell) cell;
						cell = mx.getValue();
					}
					if(cell.getClass().equals(WorkflowToolHolder.class))
					{
						WorkflowToolHolder selected = (WorkflowToolHolder) cell;
						popup.initialize(selected.getTool(), (Workflow) selected.getWorkflow());
						popup.show(WorkflowVisualizationPanel.this, (int) e.getX(),
								(int) e.getY());
					}
				}
			}
		});
		refreshLayout();
	}
	@Override
	public Component getComponent() {
		return this;
	}
	private Object pool;
	public void render(List<WorkflowWrapper> ret, Tool selected) {
		wkflwCache = new HashMap<Integer, WorkflowWrapper>();
		for(WorkflowWrapper w : ret)
		{
			wkflwCache.put(w.getId(), w);
		}
		//Sort by # of children
		Collections.sort(ret, new Comparator<WorkflowWrapper>() {

			@Override
			public int compare(WorkflowWrapper o1, WorkflowWrapper o2) {
				if(o1.getCachedChildrenCount() < o2.getCachedChildrenCount())
					return 1;
				else if(o1.getCachedChildrenCount() > o2.getCachedChildrenCount())
					return -1;
				else
					return 0;
			}
		});
		initGraph();
		pool = graph.insertVertex(graph.getDefaultParent(), null, "", 0, 0, 10, 10,"POOL");
		renderedWfs = new HashSet<Integer>();
		renderAsSubs(ret,selected,false,null);
		layoutAndShowGraph();

	}
	private HashSet<Integer> renderedWfs;
	private HashMap<Integer,WorkflowWrapper> wkflwCache;
	private HashMap<WorkflowWrapper, mxICell> wkflTails;
	private HashMap<WorkflowWrapper, mxICell> swimlanes;
	private HashMap<mxICell,WorkflowWrapper> swimlanesBack;
	private void renderAsSubs(List<WorkflowWrapper> ret, Tool selected,boolean collapseLevel, WorkflowWrapper parent) {
		if(ret.size() == 0)
			return;
		for(WorkflowWrapper w: ret)
		{
			if(renderedWfs.contains(w.getId()))
				continue;
			if(parent == null)
			{
				if(!ret.contains(wkflwCache.get(w.getCachedParentId())))
				{
					if(w.getTools().size() > 0)
					{
						Object lane = graph.insertVertex(pool, null, "", 0, 0, this.getWidth(), 10,"SWIMLANE");
						swimlanes.put(w, (mxICell) lane);
						renderedWfs.add(w.getId());
						renderSingleWorkflow(w,selected,lane);
						renderAsSubs(ret, selected, true, w);
					}
				}
			}
			else if(parent.getId() == w.getCachedParentId())
			{
				if(w.getTools().size() > 0)
				{
					Object lane = graph.insertVertex(pool, null, "", 0, 0, this.getWidth(), 10,"SWIMLANE");
					swimlanes.put(w, (mxICell) lane);
					renderedWfs.add(w.getId());
					renderSingleWorkflow(w,selected,lane,wkflwCache.get(w.getCachedParentId()).getTools().size(),wkflTails.get(wkflwCache.get(w.getCachedParentId())));
					renderAsSubs(ret, selected, true, w);
				}
			}
		}
	}

	private void renderSingleWorkflow(WorkflowWrapper w, Tool selected, Object parent,
			int toolOffset, mxICell drawFrom) {
		if(this.getGraphics() == null)
			return;
		Font f = new Font("Helvetica",Font.PLAIN,11);

		int drawOffset;
		if(drawFrom == null)
			drawOffset = 10;
		else
			drawOffset = (int) (drawFrom.getGeometry().getX() + drawFrom.getGeometry().getWidth());

		if(parent == null)
			parent = graph.getDefaultParent();

		Object lastCell = null;

		int i = 0;
		for(WorkflowTool to : w.getTools())
		{
			if(to == null || to.getTool() == null)
			{
				continue;
			}
			if(i < toolOffset)
			{
				i++;
				continue;
			}
			String styl;
			if(to.getTool().equals(selected))
				styl = "WORKFLOW;fillColor=#e8f2dd";
			else
				styl = "WORKFLOW";

			Rectangle2D r = f.getStringBounds(to.getTool().getName(), ((Graphics2D) this.getGraphics()).getFontRenderContext());
			Object v1 = graph.insertVertex(parent, null, new WorkflowToolHolder(to), 10, 10, r.getWidth()+10, r.getHeight()+10,styl);

			if(lastCell != null)
				graph.insertEdge(parent, null, "", lastCell, v1,"editable=0");
			else if(drawFrom != null)
			{
				myStackLayout layout = new myStackLayout(graph,false,20,10,10);
				layout.setResizeParent(true);
				layout.execute(pool);

				layout = new myStackLayout(graph,true,20,0,0);
				layout.setWrap((int) this.getSize().getWidth());
				layout.setResizeParent(true);
				layout.execute(parent);


				mxICell e = (mxICell) graph.insertEdge(graph.getDefaultParent(), null, "", drawFrom, v1,"CROSSOVER;editable=0");
				ArrayList<mxPoint> pts = new ArrayList<mxPoint>();
				pts.add(new mxPoint(drawFrom.getGeometry().getCenterX() +((mxICell) parent).getGeometry().getX(), ((mxICell) v1).getGeometry().getCenterY() + ((mxICell) parent).getGeometry().getY()));
				e.getGeometry().setPoints(pts);
			}
			lastCell = v1;
		}

		swimlanesBack.put((mxICell) parent,w);
		myStackLayout layout = new myStackLayout(graph,true,20,drawOffset,0);
		layout.setWrap((int) this.getSize().getWidth() + drawOffset);
		layout.setResizeParent(true);
		layout.execute(parent);
		if(swimlanes.get(w) != null)
		{
		layout.setWrap((int) swimlanes.get(w).getGeometry().getWidth());
		layout.setResizeParent(true);
		layout.execute(parent);
		}
		wkflTails.put(w, ((mxICell) lastCell));



	}
	private void renderSingleWorkflow(WorkflowWrapper w, Tool selected,Object parent) {
		renderSingleWorkflow(w, selected, parent,0,null);	
	}
	private void initGraph() {
		graph = new myGraph();



		graph.getModel().beginUpdate();


		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		style.put(mxConstants.STYLE_OPACITY, 50);
		style.put(mxConstants.STYLE_FONTCOLOR, "#774400");
		style.put(mxConstants.STYLE_FONTSIZE, 11);
		style.put(mxConstants.STYLE_FONTFAMILY, "Helvetica");		
		style.put(mxConstants.STYLE_EDITABLE, false);
		graph.getStylesheet().putCellStyle("WORKFLOW", style);

		Hashtable<String, Object> style2 = new Hashtable<String, Object>();
		style2.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_OVAL);
		style2.put(mxConstants.STYLE_ORTHOGONAL, true);
		style2.put(mxConstants.STYLE_BENDABLE, true);
		style2.put(mxConstants.STYLE_ELBOW, mxConstants.ELBOW_HORIZONTAL);
		graph.getStylesheet().putCellStyle("CROSSOVER", style2);


		Hashtable<String, Object> style3 = new Hashtable<String, Object>();
		style3.put(mxConstants.STYLE_FILLCOLOR, "#E1E2E6");
		style3.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		style3.put(mxConstants.STYLE_STROKEWIDTH, .5);
		style3.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_CENTER);
		//		style3.put(mxConstants.STYLE_OPACITY, "0");
		graph.getStylesheet().putCellStyle("SWIMLANE", style3);

		Hashtable<String, Object> style4 = new Hashtable<String, Object>();
		style4.put(mxConstants.STYLE_OPACITY, 0);
		graph.getStylesheet().putCellStyle("POOL", style4);


		graph.setConnectableEdges(false);
		graph.setAllowDanglingEdges(false);
		graph.setCellsEditable(false);
		graph.setCellsSelectable(false);
		graph.setCellsDisconnectable(false);
		graph.setCellsResizable(false);
		graph.setConnectableEdges(false);
		graph.setEnabled(false);

		wkflTails = new HashMap<WorkflowWrapper, mxICell>();
		swimlanes = new HashMap<WorkflowWrapper, mxICell>();
		swimlanesBack = new HashMap<mxICell, WorkflowWrapper>();
	}


}

class WorkflowToolHolder extends WorkflowTool
{
	WorkflowTool delegate;
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}
	public int getId() {
		return delegate.getId();
	}
	public int getOrder() {
		return delegate.getOrder();
	}
	public Tool getTool() {
		return delegate.getTool();
	}
	public Object getWorkflow() {
		return delegate.getWorkflow();
	}
	public int hashCode() {
		return delegate.hashCode();
	}
	public void setId(int value) {
		delegate.setId(value);
	}
	public void setOrder(int value) {
		delegate.setOrder(value);
	}
	public void setTool(Tool value) {
		delegate.setTool(value);
	}
	public void setWorkflow(Object value) {
		delegate.setWorkflow(value);
	}
	public String toString() {
		return delegate.getTool().getName();
	}
	public WorkflowToolHolder(WorkflowTool tool)
	{
		delegate = tool;
	}
}