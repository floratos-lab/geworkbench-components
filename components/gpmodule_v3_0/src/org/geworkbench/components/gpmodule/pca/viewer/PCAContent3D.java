/*
 * Parts of this code was adapted from Content3D.java written by caliente
 * of The Institute for Genomic Research (TIGR)
 *
 */
package org.geworkbench.components.gpmodule.pca.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;

import javax.media.j3d.*;

import javax.swing.JPanel;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.behaviors.PickRotateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickTranslateBehavior;
import com.sun.j3d.utils.picking.behaviors.PickZoomBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.picking.behaviors.*;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;


import java.util.*;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAContent3D extends JPanel
{
    private SimpleUniverse universe;
    private Canvas3D onScreenCanvas;
    private Canvas3D offScreenCanvas;
    private BranchGroup scene;
    private TransformGroup spinGroup;
    private float scaleAxisX = 3f;
    private float scaleAxisY = 3f;
    private float scaleAxisZ = 3f;

    private float pointSize = 1.0f;
    private Color3f blackColor = new Color3f(0f, 0f, 0f);
    private Color3f whiteColor = new Color3f(1f, 1f, 1f);
    private List xyzPoints;
    private String xAxisLabel = "X";
    private String yAxisLabel = "Y";
    private String zAxisLabel = "Z";
    private Map clusterColorMap = new HashMap();
    private Map pointLabelMap = new HashMap();
    private String selectedPoint;


    /**
     * Constructs a PCAContent3D object
     */
    public PCAContent3D(List xyzPoints)
    {
    	this.xyzPoints = xyzPoints;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(10, 10));
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        this.onScreenCanvas = new Canvas3D(config);
        this.universe = new SimpleUniverse(onScreenCanvas);
        universe.getViewingPlatform().setNominalViewingTransform();

        offScreenCanvas = new Canvas3D(config, true);
        Screen3D sOn = onScreenCanvas.getScreen3D();
        Screen3D sOff = offScreenCanvas.getScreen3D();
        sOff.setSize(sOn.getSize());
        sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth());
        sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight());
        // attach the offscreen canvas to the view
        universe.getViewer().getView().addCanvas3D(offScreenCanvas);


        add(onScreenCanvas, BorderLayout.CENTER);
    }

    public String getSelectedPoint()
    {
        return selectedPoint;
    }
    
    public void setXAxisLabel(String label)
    {
    	this.xAxisLabel = label;
    }

    public void setYAxisLabel(String label)
    {
    	this.yAxisLabel = label;
    }

    public void setZAxisLabel(String label)
    {
    	this.zAxisLabel = label;
    }
    /**
     * Returns a scale of the x axis.
     */
    public float getScaleAxisX()
    {
        return scaleAxisX;
    }

    /**
     * Returns a scale of the y axis.
     */
    public float getScaleAxisY()
    {
        return scaleAxisY;
    }

    /**
     * Returns a scale of the z axis.
     */
    public float getScaleAxisZ()
    {
        return scaleAxisZ;
    }

    /**
     * Sets scales.
     */
    public void setScale(float dimX, float dimY, float dimZ)
    {
        scaleAxisX = dimX;
        scaleAxisY = dimY;
        scaleAxisZ = dimZ;
    }

    public float getMaxValue()
    {
        return Math.max(scaleAxisX, Math.max(scaleAxisY, scaleAxisZ));
    }


    /**
     * Returns the content image.
     */
    public BufferedImage createImage()
    {
//        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, (BufferedImage)this.createImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight()));
        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight()));
        offScreenCanvas.setOffScreenLocation(onScreenCanvas.getLocationOnScreen());
        offScreenCanvas.setOffScreenBuffer(buffer);
        offScreenCanvas.renderOffScreenBuffer();
        offScreenCanvas.waitForOffScreenRendering();
        BufferedImage offImage = offScreenCanvas.getOffScreenBuffer().getImage();
        BufferedImage image = new BufferedImage(offImage.getWidth(), offImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        //BufferedImage image = (BufferedImage)this.createImage(offImage.getWidth(), offImage.getHeight());
        //BufferedImage image = (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(offImage.getWidth(), offImage.getHeight());
        image.setData(offImage.getData());
        return image;
    }

    /**
     * Resets spin coordinaties.
     */
    public void reset() {
        spinGroup.setTransform(new Transform3D());
    }

    /**
     * Updates the universe scene.
     */
    public void updateScene() {
        Transform3D spinTransform = new Transform3D();
        if (scene != null) {
            spinGroup.getTransform(spinTransform);
            scene.detach();
        }
        this.scene = createSceneGraph(onScreenCanvas, spinTransform);
        universe.addBranchGraph(scene);
    }

    /**
     * Creates a branch group with specified canvas.
     */
    private BranchGroup createSceneGraph(Canvas3D canvas)
    {
        return createSceneGraph(canvas, null);
    }

    /**
     * Creates a branch group with specified canvas and transformation object.
     */
    private BranchGroup createSceneGraph(Canvas3D canvas, Transform3D spinTransform)
    {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        this.spinGroup = createCoordinateSystem(bounds);
        if (spinTransform != null) {
            spinGroup.setTransform(spinTransform);
        }
        TransformGroup objScale = createScaleTransformGroup(bounds);
        objScale.addChild(spinGroup);
        objRoot.addChild(objScale);

        PickRotateBehavior rotateBehavior = new PickRotateBehavior(objRoot, canvas, bounds);
        objRoot.addChild(rotateBehavior);
        PickZoomBehavior zoomBehavior = new PickZoomBehavior(objRoot, canvas, bounds);
        objRoot.addChild(zoomBehavior);
        PickTranslateBehavior translateBehavior = new PickTranslateBehavior(objRoot, canvas, bounds);
        objRoot.addChild(translateBehavior);

        PCABehavior pcaBehavior = new PCABehavior(objRoot, canvas, bounds);
        objRoot.addChild(pcaBehavior);

        objRoot.compile();
        return objRoot;
    }

    /**
     * Creates a scale transform group.
     */
    private TransformGroup createScaleTransformGroup(BoundingSphere bounds)
    {
        Transform3D t = new Transform3D();
        t.setScale(0.22);
        TransformGroup scale = new TransformGroup(t);

        Color3f bgColor = blackColor;
        Background bg = new Background(bgColor);
        bg.setApplicationBounds(bounds);
        scale.addChild(bg);

        scale.addChild(createAmbientLight(bounds));
        scale.addChild(createLight(bounds, new Vector3d(0.0, 0.0, 3.0)));
        scale.addChild(createLight(bounds, new Vector3d(0.0, 0.0, 10.0)));
        return scale;
    }

    /**
     * Creates a coordinate system transform group.
     */
    private TransformGroup createCoordinateSystem(BoundingSphere bounds)
    {
        TransformGroup group = new TransformGroup();
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        group.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        group.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
        // add axises
        group.addChild(createXAxis());
        group.addChild(createYAxis());
        group.addChild(createZAxis());

        group.addChild(createSpheres());
        group.addChild(createText());

        return group;
    }


    /**
     * Creates a light transform group.
     */
    private TransformGroup createLight(BoundingSphere bounds, Vector3d vector)
    {
        Transform3D t = new Transform3D();
        t.set(vector);
        TransformGroup lightGroup = new TransformGroup(t);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        lightGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        lightGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        ColoringAttributes attr = new ColoringAttributes();
        Color3f color = new Color3f(1.0f, 1.0f, 1.0f);
        attr.setColor(color);
        Appearance appearance = new Appearance();
        appearance.setColoringAttributes(attr);
        lightGroup.addChild(new Sphere(0.01f, Sphere.GENERATE_NORMALS, 15, appearance));
        Light light = new PointLight(color, new Point3f(0.0f, 0.0f, 0.0f), new Point3f(1.0f, 0.0f, 0.0f));
        light.setInfluencingBounds(bounds);
        lightGroup.addChild(light);
        return lightGroup;
    }


    /**
     * Creates an ambient light.
     */
    private AmbientLight createAmbientLight(BoundingSphere bounds)
    {
        AmbientLight light = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
        light.setInfluencingBounds(bounds);
        return light;
    }

    /**
     * Creates a cone shape.
     */
    private Cone createCone()
    {
        return new Cone(0.05f, 0.2f);
    }

    /**
     * Creates a cylinder shape with specified color.
     */
    private Cylinder createCylinder(Color3f color)
    {
        Material material = new Material(color, blackColor, color, whiteColor, 100f);

        Appearance appearance = new Appearance();
        appearance.setLineAttributes(new LineAttributes(10, LineAttributes.PATTERN_SOLID, true));
        appearance.setMaterial(material);
        return new Cylinder(0.025f, 6f, appearance);
        //return new Cylinder(0.025f, getMaxValue(), appearance);
    }

    /**
     * Returns a specified point size.
     */
    private float getPointSize()
    {
        return pointSize;
    }

    /**
     * Sets a point size.
     */
    public void setPointSize(float size) {

        pointSize = size;
    }

    /**
     * Creates a point appearance with specified color.
     */
    private Appearance createSphereAppearance(Color3f color)
    {
        Material material = new Material(color, this.blackColor, color, this.whiteColor, 100.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        return appearance;
    }

    /**
     * Creates a spheres transform group.
     */
    private TransformGroup createSpheres() {
        TransformGroup spheres = new TransformGroup();

        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;

        // selected material
        Color3f sColor = new Color3f(Color.RED);
        Appearance sAppearance = createSphereAppearance(sColor);

        Transform3D transform;
        Vector3d vector3d;
        TransformGroup sphere;
        double x, y, z;

        Random rand = new Random(12345);
        for (int i=0; i< xyzPoints.size(); i++)
        {
        	XYZData data = (XYZData)xyzPoints.get(i);
            x = data.getX();
            y = data.getY();
            z = data.getZ();

            if(data.getCluster() != null)
            {
            	if(clusterColorMap.get(data.getCluster()) == null)
            	{
            		Color c = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					while(c == Color.RED || c == Color.GREEN)
						c = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

					Appearance cAppearance = createSphereAppearance(new Color3f(c));
					clusterColorMap.put(data.getCluster(), cAppearance);
				}
			}

            transform  = new Transform3D();
            vector3d = new Vector3d(x*factorX, y*factorY, z*factorZ);
            transform.set(vector3d);

            sphere = new TransformGroup(transform);

            Sphere shape = null;
            if(data.getCluster() != null)
                shape = new Sphere(getPointSize()/20f, (Appearance)clusterColorMap.get(data.getCluster()));       
            else
                shape = new Sphere(getPointSize()/20f, sAppearance);

            shape.getCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            shape.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            shape.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_READ);
            shape.setUserData(data.getLabel());
            shape.getShape().setUserData(data.getLabel());

            sphere.addChild(shape);

            spheres.addChild(sphere);
        }

        return spheres;
    }



    /**
     * Creates a text transform group.
     */
    private TransformGroup createText()
    {
        TransformGroup textGroup = new TransformGroup();

        float factorX = 3f/scaleAxisX;
        float factorY = 3f/scaleAxisY;
        float factorZ = 3f/scaleAxisZ;

 		Font3D font = new Font3D(new Font("TestFont", Font.BOLD, (int)(Math.round(getPointSize()))), new FontExtrusion());
        Font origFont = font.getFont();

        FontMetrics fMet = onScreenCanvas.getFontMetrics(origFont);
        int ascent = fMet.getAscent();

        Color3f color3f;

        color3f = new Color3f(1.0f, 1.0f, 1.0f);

        Material material;

        material = new Material(color3f, whiteColor, color3f, whiteColor, 100f);

        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);

        Transform3D fontTransform = new Transform3D();
        fontTransform.setScale(0.1);

        TransformGroup tempGroup;
        Text3D text3d;
        Shape3D shape3d;
        String text;
        float x, y, z;
         for (int i=0; i< xyzPoints.size(); i++)
        {
        	XYZData data = (XYZData)xyzPoints.get(i);
            x = data.getX();
            y = data.getY();
            z = data.getZ();
            tempGroup = new TransformGroup(fontTransform);
            text = "";
            text3d = new Text3D(font, text, new Point3f(x*factorX*10f+getPointSize(), (float)(y*factorY*10f - (float)ascent/2f), z*factorZ*10f));
            text3d.setCapability(Text3D.ALLOW_STRING_WRITE);
            text3d.setCapability(Text3D.ALLOW_STRING_READ);
            text3d.setUserData(data.getLabel() + " : " + "[" + x + ", " + y + ", " + z + "]");
            shape3d = new Shape3D();
            shape3d.setGeometry(text3d);
            shape3d.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
            shape3d.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
            shape3d.setAppearance(appearance);
            tempGroup.addChild(shape3d);
            textGroup.addChild(tempGroup);

            pointLabelMap.put(data.getLabel(), text3d);
        }
        return textGroup;
    }

    /**
     * Creates 3D shape for specified string.
     */
    private Shape3D createTextShape3D(String text)
    {
        Font3D axisFont = new Font3D(new Font("TestFont", Font.BOLD, 1), new FontExtrusion());
        Text3D text3D = new Text3D(axisFont, text);
        Shape3D shape = new Shape3D();
        shape.setGeometry(text3D);
        Color3f color3f;
        Material axisFontMaterial;

        color3f = new Color3f(0.5f, 0.5f, 0.5f);
        axisFontMaterial = new Material(color3f, blackColor, color3f, whiteColor, 100f);


        axisFontMaterial.setLightingEnable(true);
        Appearance axisFontAppearance = new Appearance();
        axisFontAppearance.setMaterial(axisFontMaterial);
        shape.setAppearance(axisFontAppearance);
        return shape;
    }

    /**
     * Creates x-axis transform group.
     */
    private TransformGroup createXAxis()
    {
        Transform3D axisTrans = new Transform3D();
        axisTrans.rotZ(-Math.PI/2.0d);
        Transform3D fontTrans = new Transform3D();
        fontTrans.rotZ(Math.PI/2.0d);

         return createAxis(xAxisLabel, new Color3f(0.5f, 0.5f, 0.5f), axisTrans, fontTrans);
     }

    /**
     * Creates y-axis transform group.
     */
    private TransformGroup createYAxis()
    {
    	return createAxis(yAxisLabel, new Color3f(0.3f, 0.3f, 1f), null, null);
    }

    /**
     * Creates z-axis transform group.
     */
    private TransformGroup createZAxis()
    {
        Transform3D axisTrans = new Transform3D();
        axisTrans.rotX(-Math.PI/2.0d);
        Transform3D zTrans = new Transform3D();
        zTrans.rotY(Math.PI/2.0d);
        axisTrans.mul(zTrans);
        Transform3D fontTrans = new Transform3D();
        fontTrans.rotZ(Math.PI/2.0d);

        return createAxis(zAxisLabel, new Color3f(1f, 0.3f, 1f), axisTrans, fontTrans);
     }

    /**
     * Creates an axis transform group.
     */
    private TransformGroup createAxis(String name, Color3f color, Transform3D axisTrans, Transform3D fontTrans) {
        TransformGroup axis = new TransformGroup();
        if (axisTrans != null) {
            axis.setTransform(axisTrans);
        }
        axis.addChild(createCylinder(color));
        // Axis Positive End
        Transform3D posTransform = new Transform3D();
        posTransform.set(new Vector3d(0.0, 3.1, 0.0));
        TransformGroup posEnd = new TransformGroup(posTransform);
        posEnd.addChild(createCone());
        // Axis Negative End
        Transform3D negTransform = new Transform3D();
        negTransform.set(new Vector3d(0.0, -3.1, 0.0));
        Transform3D rotate180X = new Transform3D();
        rotate180X.rotX(Math.PI);
        negTransform.mul(rotate180X);
        TransformGroup negEnd = new TransformGroup(negTransform);
        negEnd.addChild(createCone());
        // Font
        Transform3D fontTransform = new Transform3D();
        fontTransform.set(0.22, new Vector3d(0.25, 2.75, -0.0125));
        TransformGroup fontGroup = new TransformGroup(fontTransform);

        TransformGroup rotFontGroup = new TransformGroup();
        if (fontTrans != null)
        {
            rotFontGroup.setTransform(fontTrans);
        }

        Shape3D shape3D = createTextShape3D(name);
        rotFontGroup.addChild(shape3D);

        fontGroup.addChild(rotFontGroup);

        axis.addChild(fontGroup);
        axis.addChild(posEnd);
        axis.addChild(negEnd);
        return axis;
    }

    static public class XYZData
    {
    	private String label= null;
    	private String cluster = null;
    	private float x;
    	private float y;
    	private float z;

    	public XYZData(float x, float y, float z, String label)
    	{
    		this.x = x;
    		this.y = y;
    		this.z = z;
    		this.label = label;
    	}

    	public float getX()
    	{
    		return x;
    	}

    	public float getY()
    	{
    		return y;
    	}

    	public float getZ()
    	{
    		return z;
    	}

    	public String getLabel()
    	{
    		return label;
    	}

    	public void setCluster(String cluster)
    	{
    		this.cluster = cluster;
    	}

    	public String getCluster()
    	{
    		return cluster;
    	}
    }

    private class PCABehavior extends PickMouseBehavior
	{
  		private int pickMode = PickCanvas.GEOMETRY_INTERSECT_INFO;
  		private Sphere lastSelectedShape = null;
  		private Appearance lastSelectedShapeAppearance = null;
        private Text3D lastShowedText = null;

          /**
   	 	 * Creates a pick/translate behavior that waits for user mouse events for
   	 	 * the scene graph. This method has its pickMode set to BOUNDS picking.
   	 	 * @param root   Root of your scene graph.
   	 	 * @param canvas Java 3D drawing canvas.
   	 	 * @param bounds Bounds of your scene.
   		 **/

  	    public PCABehavior(BranchGroup root, Canvas3D canvas, Bounds bounds)
  	    {
    	    super(canvas, root, bounds);
    	    this.setSchedulingBounds(bounds);
         }

  	/**
   	 * Sets the pickMode component of this PickTranslateBehavior to the value of
   	 * the passed pickMode.
   	 * @param pickMode the pickMode to be copied.
   	 **/

  	public void setPickMode(int pickMode)
  	{
    	this.pickMode = pickMode;
  	}

 	/**
   	 * Return the pickMode component of this PickTranslaeBehavior.
   	 **/

  	public int getPickMode()
  	{
    	return pickMode;
  	}

	public void processStimulus (Enumeration criteria)
	{
    	super.processStimulus(criteria);

    	if(!buttonPress && mevent != null)
      		updateScene(mevent.getPoint().x, mevent.getPoint().x);
  	}

  	/**
   	 * Update the scene to manipulate any nodes. This is not meant to be
   	 * called by users. Behavior automatically calls this. You can call
   	 * this only if you know what you are doing.
   	 *
   	 * @param xpos Current mouse X pos.
   	 * @param ypos Current mouse Y pos.
   	**/
   	public void updateScene(int xpos, int ypos)
   	{
    	if (!mevent.isAltDown())
    	{           
            if(lastSelectedShape != null)
				lastSelectedShape.setAppearance(lastSelectedShapeAppearance);

			pickCanvas.setShapeLocation(mevent);
            //pickCanvas.setTolerance((float)6.0);

            PickResult[] results = pickCanvas.pickAllSorted();

            if(results == null)
				return;

            Node result = null;
            for(int i =0; i < results.length; i++)
            {
                if(results[i].getNode(PickResult.PRIMITIVE) instanceof Sphere)
                {
                    result = results[i].getNode(PickResult.PRIMITIVE);
                    break;
                }
            }

            if(result == null)
				return;

   			 Sphere m_Shape3D = (Sphere)result;

			if(m_Shape3D.getUserData() != null && m_Shape3D.getCapability(Shape3D.ALLOW_APPEARANCE_WRITE) == true)
			{
				if(buttonPress)
                {
                    Appearance app = new Appearance();

				    Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
				    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

				    Color3f objColor = new Color3f();
				    objColor.set(Color.GREEN);
				    app.setMaterial(new Material(objColor, black, objColor,
					     white, 80.0f));

                    if(lastSelectedShape != m_Shape3D)
                    {
                        lastSelectedShape = m_Shape3D;
                        lastSelectedShapeAppearance = m_Shape3D.getAppearance();

                        m_Shape3D.setAppearance(app);
                    }
                    else
                        m_Shape3D.setAppearance(lastSelectedShapeAppearance);

                    selectedPoint = (String)m_Shape3D.getUserData();
                }
                else
                {
                    if(lastShowedText != null)

                    lastShowedText.setString("");

                    Text3D text = (Text3D)pointLabelMap.get(m_Shape3D.getUserData());
                    text.setString((String)text.getUserData());
                    lastShowedText = text;
                }
            }
        }

        /*else
      	{
            if(lastShowedText != null) 
                lastShowedText.setString("");

            pickCanvas.setShapeLocation(mevent);
            pickCanvas.setTolerance((float)20.0);

            PickResult[] results = pickCanvas.pickAllSorted();
            if(results == null)
				return;

            Text3D result = null;
            for(int i =0; i < results.length; i++)
            {
                if( ((Shape3D)results[i].getObject()).getGeometry() instanceof Text3D)
                {
                    result = (Text3D)((Shape3D)results[i].getObject()).getGeometry();
                    break;
                }
            }

            if(result == null || result.getCapability(Text3D.ALLOW_STRING_WRITE) == false)
		        return;

            result.setString((String)result.getUserData());
            lastShowedText = result;
        }  */
    }
 }
}