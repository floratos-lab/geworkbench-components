/*
 * Parts of this code was adapted from Content3D.java written by caliente
 * of The Institute for Genomic Research (TIGR)
 *
 */
package org.geworkbench.components.gpmodule.pca.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Panel;

import javax.media.j3d.*;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.swing.*;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.config.rules.GeawConfigObject;

/**
 * @author: Marc-Danie Nazaire
 */
public class PCAContent3D extends Panel
{
     private static Log log = LogFactory.getLog(PCAContent3D.class);
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
    private Map clusterAppearanceMap = new HashMap();
    private Map clusterColors;
    private Map pointTextMap = new HashMap();
    private String selectedPoint;

    // controls which points are visible and not visible in plot
    private Switch sphereSwitch;

    ArrayList spheresList = new ArrayList();

    /**
     * Constructs a PCAContent3D object
     */
    public PCAContent3D()
    {
        try
        {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
            setLayout(new BorderLayout());
            GraphicsConfiguration config = getPreferredConfig(GeawConfigObject.getGuiWindow().getLocation());
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
            universe.getViewer().getView().setFieldOfView(1.1);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "An error occured while creating 3D projection plot");
            log.error(e);
        }
    }

    public void setData(List xyzPoints)
    {
        this.xyzPoints = xyzPoints;
        initScales(xyzPoints);
    }

    public Canvas3D getCanvas()
    {
        return onScreenCanvas; 
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


    /**
        * Sets scales according to U-matrix values.
        * @param xyzPoint a list of XYZData objects (3D coordinates)
        */
    private void initScales(List xyzPoint)
    {
       float maxX = 0f;
       float maxY = 0f;
       float maxZ = 0f;
       final int rows = xyzPoint.size();
       for (int i = rows; --i >= 0;) {
           maxX = Math.max(maxX, Math.abs(((XYZData) xyzPoints.get(i)).getX()));
           maxY = Math.max(maxY, Math.abs(((XYZData) xyzPoints.get(i)).getY()));
           maxZ = Math.max(maxZ, Math.abs(((XYZData) xyzPoints.get(i)).getZ()));
       }

       maxX = (float) (Math.ceil(((double) maxX / 5))) * 5 + 5;
       maxY = (float) (Math.ceil(((double) maxY / 5))) * 5 + 5;
       maxZ = (float) (Math.ceil(((double) maxZ / 5))) * 5 + 5;
       setScale(maxX, maxY, maxZ);
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
        ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, (BufferedImage)java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(onScreenCanvas.getWidth(), onScreenCanvas.getHeight()));
        offScreenCanvas.setOffScreenLocation(onScreenCanvas.getLocationOnScreen());
        offScreenCanvas.setOffScreenBuffer(buffer);
        offScreenCanvas.renderOffScreenBuffer();
        offScreenCanvas.waitForOffScreenRendering();
        BufferedImage offImage = offScreenCanvas.getOffScreenBuffer().getImage();
        BufferedImage image = new BufferedImage(offImage.getWidth(), offImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        image.setData(offImage.getData());
        return image;
    }

    /**
     * Resets spin coordinaties.
     */
    public void reset()
    {
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

    public void updatePoints()
    {
        BitSet bitSet = sphereSwitch.getChildMask();
        bitSet.set(0, spheresList.size(), false);

        for(int i=0; i < xyzPoints.size(); i++)
        {
            PCAContent3D.XYZData data = (PCAContent3D.XYZData)xyzPoints.get(i);

            if(data.getCluster() == null)
                continue;

            if(clusterAppearanceMap.get(data.getCluster()) == null)
            {
                Color c = (Color)clusterColors.get(data.getCluster());

	            Appearance cAppearance = createSphereAppearance(new Color3f(c));
			    clusterAppearanceMap.put(data.getCluster(), cAppearance);
	        }

            int j=0;
            Sphere sp = (Sphere)spheresList.get(j);

            while(j < spheresList.size() && (sp != null && !sp.getUserData().equals(data.getLabel())))
            {
                j++;
                sp = (Sphere)spheresList.get(j);            
            }

            if(sp == null)
                continue;
            
            sp.setAppearance((Appearance)clusterAppearanceMap.get(data.getCluster()));
            bitSet.set(j, true);
        }

        sphereSwitch.setChildMask(bitSet);
    }

    /**
     * Creates a branch group with specified canvas and transformation object.
     */
    private BranchGroup createSceneGraph(Canvas3D canvas, Transform3D spinTransform)
    {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        this.spinGroup = createCoordinateSystem();
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
    private TransformGroup createCoordinateSystem()
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
        material.setCapability(Material.ALLOW_COMPONENT_READ);
        //material.setLightingEnable(true);

        Appearance appearance = new Appearance();
        appearance.setMaterial(material);
        appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
        return appearance;
    }


    public void setClusterColors(HashMap clusterColors)
    {
        this.clusterColors = clusterColors;
    }

    /**
     * Creates a spheres transform group.
     */
    private Switch createSpheres()
    {
        spheresList = new ArrayList();
        sphereSwitch = new Switch(Switch.CHILD_MASK);
        sphereSwitch.setCapability(Switch.ALLOW_CHILDREN_READ);
        sphereSwitch.setCapability(Switch.ALLOW_CHILDREN_WRITE);
        sphereSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        sphereSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        BitSet bitSet = new BitSet();

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

        for (int i=0; i< xyzPoints.size(); i++)
        {
            bitSet.set(i, true);
            XYZData data = (XYZData)xyzPoints.get(i);
            x = data.getX();
            y = data.getY();
            z = data.getZ();

            if(data.getCluster() != null)
            {
            	if(clusterAppearanceMap.get(data.getCluster()) == null)
            	{
            		Color c = (Color)clusterColors.get(data.getCluster());

					Appearance cAppearance = createSphereAppearance(new Color3f(c));
					clusterAppearanceMap.put(data.getCluster(), cAppearance);
				}
			}

            transform  = new Transform3D();
            vector3d = new Vector3d(x*factorX, y*factorY, z*factorZ);
            transform.set(vector3d);

            sphere = new TransformGroup(transform);

            Sphere shape = null;

            if(data.getCluster() == null)
            {
                shape = new Sphere(getPointSize()/20f);
                bitSet.set(i, false);
            }
            else if(data.getCluster() != null && !data.getLabel().equals(selectedPoint))
                shape = new Sphere(getPointSize()/20f, (Appearance) clusterAppearanceMap.get(data.getCluster()));
            else
                shape = new Sphere(getPointSize()/20f, sAppearance);

            shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
	        shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
            shape.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            shape.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_READ);
            shape.setUserData(data.getLabel());
            shape.getShape().setUserData(data.getLabel());

            spheresList.add(shape);
            sphere.addChild(shape);
            sphereSwitch.addChild(sphere);
        }

        sphereSwitch.setChildMask(bitSet);
        return sphereSwitch;
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

 		Font3D font = new Font3D(new Font("TestFont", Font.BOLD, (Math.round(getPointSize()))), new FontExtrusion());
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
            text3d = new Text3D(font, text, new Point3f(x*factorX*10f+getPointSize(), (y*factorY*10f - (float)ascent/2f), z*factorZ*10f));
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

            pointTextMap.put(data.getLabel(), text3d);
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
      		updateScene(mevent.getPoint().x, mevent.getPoint().y);
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
            if(buttonPress && lastSelectedShape != null)
				lastSelectedShape.setAppearance(lastSelectedShapeAppearance);

            if(lastShowedText != null)
                  lastShowedText.setString("");

            pickCanvas.setShapeLocation(mevent);
            pickCanvas.setTolerance((float)4.0);

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

			if(m_Shape3D.getUserData() != null && m_Shape3D.getCapability(Shape3D.ALLOW_APPEARANCE_WRITE))
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
                        m_Shape3D.getAppearance().setUserData(lastSelectedShapeAppearance);                        
                    }
                    else
                        m_Shape3D.setAppearance(lastSelectedShapeAppearance);

                    selectedPoint = (String)m_Shape3D.getUserData();
                }
                else
                {
                    Text3D text = (Text3D) pointTextMap.get(m_Shape3D.getUserData());
                    text.setString((String)text.getUserData());
                    lastShowedText = text;
                }
            }
        }
    }
 }

    /**
     * Handler that displays a dialog and exits when there's an uncaught exception
     * in a J3D thread.
     */
    static class CustomExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        public void uncaughtException(Thread t, Throwable e)
        {
            log.error(e);
            if (t.getName().startsWith("J3D-")) {
                JOptionPane.showMessageDialog(null,
                        "A fatal exception occurred while running Java 3D",
                        "Java 3D Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

	private static GraphicsDevice[] screens_;
	private static GraphicsDevice defaultScreen_;
	private static Rectangle[] screenRects_;
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		screens_ = ge.getScreenDevices();
		int ns = screens_.length;
		screenRects_ = new Rectangle[ns];
		for (int j = 0; j < ns; j++) {
			GraphicsDevice screen = screens_[j];
			Rectangle r = screen.getDefaultConfiguration().getBounds();
			screenRects_[j] = r;
		}

		defaultScreen_ = ge.getDefaultScreenDevice();
	}

	private static GraphicsConfiguration getPreferredConfig(Point p) {
		GraphicsDevice screen = getScreen(p);
		if (screen == null)
			screen = defaultScreen_;
		return screen.getBestConfiguration(new GraphicsConfigTemplate3D());
	}

	/**
	 * return the screen containing the point, null if none do
	 */
	private static GraphicsDevice getScreen(Point p) {
		if (p == null)
			return null;

		GraphicsDevice screen = null;
		int i, n = screenRects_.length;
		for (i = 0; i < n; i++) {
			if (screenRects_[i].contains(p)) {
				screen = screens_[i];
				break;
			}
		}
		return screen;
    }
}