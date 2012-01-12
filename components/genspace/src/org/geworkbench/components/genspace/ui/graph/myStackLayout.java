package org.geworkbench.components.genspace.ui.graph;

import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

public class myStackLayout extends mxStackLayout {
	public void setWrap(int w) {
		this.wrap = w;
	}
	public void setResizeParent(boolean v)
	{
		this.resizeParent = v;
	}
	/**
	 * Constructs a new stack layout layout for the specified graph,
	 * spacing, orientation and offset.
	 */
	public myStackLayout(mxGraph graph)
	{
		this(graph, true);
	}

	/**
	 * Constructs a new stack layout layout for the specified graph,
	 * spacing, orientation and offset.
	 */
	public myStackLayout(mxGraph graph, boolean horizontal)
	{
		this(graph, horizontal, graph.getGridSize());
	}

	/**
	 * Constructs a new stack layout layout for the specified graph,
	 * spacing, orientation and offset.
	 */
	public myStackLayout(mxGraph graph, boolean horizontal, int spacing)
	{
		this(graph, horizontal, spacing, spacing, spacing);
	}

	/**
	 * Constructs a new stack layout layout for the specified graph,
	 * spacing, orientation and offset.
	 */
	public myStackLayout(mxGraph graph, boolean horizontal, int spacing,
			int x0, int y0)
	{
		super(graph);
		this.horizontal = horizontal;
		this.spacing = spacing;
		this.x0 = x0;
		this.y0 = y0;
	}
	double maxX = -1;
	
	public void execute(Object parent)
	{
		if (parent != null)
		{
			double x0 = this.x0 + 1;
			double y0 = this.y0;
			boolean horizontal = isHorizontal();

			mxIGraphModel model = graph.getModel();
			mxGeometry pgeo = model.getGeometry(parent);

			// Handles special case where the parent is either a layer with no
			// geometry or the current root of the view in which case the size
			// of the graph's container will be used.
			if (pgeo == null && model.getParent(parent) == model.getRoot()
					|| parent == graph.getView().getCurrentRoot())
			{
				mxRectangle tmp = getContainerSize();
				pgeo = new mxGeometry(0, 0, tmp.getWidth(), tmp.getHeight());
			}

			double fillValue = 0;

			if (pgeo != null)
			{
				fillValue = (horizontal) ? pgeo.getHeight() : pgeo.getWidth();
			}

			fillValue -= 2 * spacing;

			// Handles swimlane start size
			mxRectangle size = new mxRectangle(0, 0, 0, 0);

			fillValue -= (horizontal) ? size.getHeight() : size.getWidth();
			x0 = this.x0 + size.getWidth();
			y0 = this.y0 + size.getHeight() + spacing;

			model.beginUpdate();
			try
			{
				double tmp = 0;
				mxGeometry last = null;
				int childCount = model.getChildCount(parent);

				for (int i = 0; i < childCount; i++)
				{
					Object child = model.getChildAt(parent, i);

					if (!isVertexIgnored(child) && isVertexMovable(child))
					{
						mxGeometry geo = model.getGeometry(child);

						if (geo != null)
						{
							geo = (mxGeometry) geo.clone();

							if (wrap != 0 && last != null)
							{

								if ((horizontal && last.getX()
										+ last.getWidth() + geo.getWidth() + 2
										* spacing > wrap)
										|| (!horizontal && last.getY()
												+ last.getHeight()
												+ geo.getHeight() + 2 * spacing > wrap))
								{
									last = null;
									if (horizontal)
									{
										y0 += tmp + spacing;
									}
									else
									{
										x0 += tmp + spacing;
									}

									tmp = 0;
								}
							}

							tmp = Math.max(tmp, (horizontal) ? geo
									.getHeight() : geo.getWidth());

							if (last != null)
							{
								if (horizontal)
								{
									geo.setX(last.getX() + last.getWidth()
											+ spacing);
								}
								else
								{
									geo.setY(last.getY() + last.getHeight()
											+ spacing);
								}
							}
							else
							{
								if (horizontal)
								{
									geo.setX(x0);
								}
								else
								{
									geo.setY(y0);
								}
							}

							if (horizontal)
							{
								geo.setY(y0);
							}
							else
							{
								geo.setX(x0);
							}

							if (fill && fillValue > 0)
							{
								if (horizontal)
								{
									geo.setHeight(fillValue);
								}
								else
								{
									geo.setWidth(fillValue);
								}
							}
							if(geo.getX() > maxX)
								maxX = geo.getX() + geo.getWidth();
							model.setGeometry(child, geo);
							last = geo;
						}
					}
				}

				if (resizeParent && pgeo != null && last != null
						&& !graph.isCellCollapsed(parent))
				{
					pgeo = (mxGeometry) pgeo.clone();

					pgeo.setWidth(maxX + spacing);
					pgeo
					.setHeight(last.getY() + last.getHeight() + 20
							);

					model.setGeometry(parent, pgeo);
				}
			}
			finally
			{
				model.endUpdate();
			}
		}
	}

}
