//package com.computation.experimental;
//
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;
//import com.sun.j3d.utils.applet.MainFrame;
//import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
//import com.sun.j3d.utils.geometry.GeometryInfo;
//import com.sun.j3d.utils.geometry.NormalGenerator;
//import com.sun.j3d.utils.geometry.Triangulator;
//import com.sun.j3d.utils.universe.SimpleUniverse;
//
//import javax.media.j3d.*;
//import javax.swing.*;
//import javax.vecmath.*;
//import java.applet.Applet;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.io.BufferedOutputStream;
//import java.io.FileOutputStream;
//import java.text.NumberFormat;
//
//public class PointCloud3D extends Applet {
//
//    SimpleUniverse u;
//
//    boolean isApplication;
//
//    Canvas3D canvas;
//
//    View view;
//
//    /* image capture */
//    OffScreenCanvas3D offScreenCanvas;
//
//    float offScreenScale = 1.0f;
//
//    String snapImageString = "Snap Image";
//
//    // GUI elements
//    JTabbedPane tabbedPane;
//
//    // Temporaries that are reused
//    Transform3D tmpTrans = new Transform3D();
//
//    Vector3f tmpVector = new Vector3f();
//
//    AxisAngle4f tmpAxisAngle = new AxisAngle4f();
//
//    // colors for use in the cones
//    Color3f red = new Color3f(1.0f, 0.0f, 0.0f);
//
//    Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
//
//    Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
//
//    // geometric constants
//    Point3f origin = new Point3f();
//
//    Vector3f yAxis = new Vector3f(0.0f, 1.0f, 0.0f);
//
//    // NumberFormat to print out floats with only two digits
//    NumberFormat nf;
//
//    public PointCloud3D() {
//        this(false);
//    }
//
//    public PointCloud3D(boolean isApplication) {
//        this.isApplication = isApplication;
//    }
//
//    // The following allows LineTypes to be run as an application
//    // as well as an applet
//    //
//    public static void main(String[] args) {
//        new MainFrame(new PointCloud3D(true), 600, 600);
//    }
//
//    // Returns the TransformGroup we will be editing to change the tranform
//    // on the lines
//    Group createLineTypes() {
//
//        Group lineGroup = new Group();
//
//        Appearance app = new Appearance();
//        ColoringAttributes ca = new ColoringAttributes(black,
//                ColoringAttributes.SHADE_FLAT);
//        app.setColoringAttributes(ca);
//
//        // Plain line
//        Point3f[] plaPts = new Point3f[2];
//        plaPts[0] = new Point3f(-0.9f, -0.7f, 0.0f);
//        plaPts[1] = new Point3f(-0.5f, 0.7f, 0.0f);
//        LineArray pla = new LineArray(2, LineArray.COORDINATES);
//        pla.setCoordinates(0, plaPts);
//        Shape3D plShape = new Shape3D(pla, app);
//        lineGroup.addChild(plShape);
//
//        // Set up the points
//        Point3f[] plaPts2 = new Point3f[4];
//        Color3f[] colPts = new Color3f[4]; //parallel to coordinates, colors.
//        int count = 0;
//
//        for (int i = 0; i < 2; i++) {
//            for (int j = 0; j < 2; j++) {
//                System.out.println(count);
//                plaPts2[count] = new Point3f(i / 10.0f, j / 10.0f, 0);
//                colPts[count] = new Color3f(i / 3.0f, j / 3.0f, (float) ((i + j) / 3.0));//my arbitrary color set :)
//                count++;
//            }
//        }
//
//        PointArray pla2 = new PointArray(4, GeometryArray.COORDINATES | GeometryArray.COLOR_3);
//        pla2.setColors(0, colPts); //this is the color-array setting
//        pla2.setCoordinates(0, plaPts2);
//        PointAttributes pointAttrs = new PointAttributes();
//        pointAttrs.setPointSize(10.0f);//10 pixel-wide point
//        pointAttrs.setPointAntialiasingEnable(true);//now points are sphere-like(not a cube)
//        app.setPointAttributes(pointAttrs);
//        Shape3D plShape2 = new Shape3D(pla2, app);
//        lineGroup.addChild(plShape2);
//
//        //Generate a surface from 10 vertices and
//        //create a hole in the surface by removing
//        //a hole defined using 5 vertices. Note that the hole
//        //must be entirely within the outer polygon.
//        double[] m_VertexArray = {1, 1, 0, //0
//                0, 3, 0, //1
//                1, 5, 0, //2
//                2, 4, 0, //3
//                4, 5, 0, //4
//                3, 3, 0, //5
//                4, 2, 0, //6
//                4, 0, 0, //7
//                3, 0, 0, //8
//                2, 1, 0, //9
//        //these are vertices for the hole
//                1, 3, 0, //10
//                2, 3, 0, //11
//                3, 2, 0, //12
//                3, 1, 0, //13
//                2, 2, 0};//14
//        //triangulate the polygon
//        GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
//
//        gi.setCoordinates(m_VertexArray);
//
//        //the first 10 points make up the outer edge of the polygon,
//        //the next five make up the hole
//        int[] stripCountArray = {10, 5};
//        int[] countourCountArray = {stripCountArray.length};
//
//        gi.setContourCounts(countourCountArray);
//        gi.setStripCounts(stripCountArray);
//
//        Triangulator triangulator = new Triangulator();
//        triangulator.triangulate(gi);
//
//        //also generate normal vectors so that the surface can be light
//        NormalGenerator normalGenerator = new NormalGenerator();
//        normalGenerator.generateNormals(gi);
//
//        //render as a wireframe
//        PolygonAttributes polyAttrbutes = new PolygonAttributes();
//        polyAttrbutes.setPolygonMode(PolygonAttributes.POLYGON_LINE);
//        polyAttrbutes.setCullFace(PolygonAttributes.CULL_NONE);
//        app.setPolygonAttributes(polyAttrbutes);
//
//        //add both a wireframe and a solid version
//        //of the triangulated surface
//        Shape3D shape1 = new Shape3D(gi.getGeometryArray(), app);
//        Shape3D shape2 = new Shape3D(gi.getGeometryArray());
//
//        lineGroup.addChild(shape1);
//        lineGroup.addChild(shape2);
//
//        return lineGroup;
//
//    }
//
//    BranchGroup createSceneGraph() {
//        // Create the root of the branch graph
//        BranchGroup objRoot = new BranchGroup();
//
//        // Create a TransformGroup to scale the scene down by 3.5x
//        // TODO: move view platform instead of scene using orbit behavior
//        TransformGroup objScale = new TransformGroup();
//        Transform3D scaleTrans = new Transform3D();
//        //scaleTrans.set(1 / 3.5f); // scale down by 3.5x
//        objScale.setTransform(scaleTrans);
//        objRoot.addChild(objScale);
//
//        // Create a TransformGroup and initialize it to the
//        // identity. Enable the TRANSFORM_WRITE capability so that
//        // the mouse behaviors code can modify it at runtime. Add it to the
//        // root of the subgraph.
//        TransformGroup objTrans = new TransformGroup();
//        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
//        objScale.addChild(objTrans);
//
//        // Add the primitives to the scene
//        objTrans.addChild(createLineTypes());
//
//        BoundingSphere bounds = new BoundingSphere(new Point3d(), 100.0);
//        Background bg = new Background(new Color3f(1.0f, 1.0f, 1.0f));
//        bg.setApplicationBounds(bounds);
//        objTrans.addChild(bg);
//
//        // set up the mouse rotation behavior
//        MouseRotate mr = new MouseRotate();
//        mr.setTransformGroup(objTrans);
//        mr.setSchedulingBounds(bounds);
//        mr.setFactor(0.007);
//        objTrans.addChild(mr);
//
//        // Set up the ambient light
//        Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
//        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
//        ambientLightNode.setInfluencingBounds(bounds);
//        objRoot.addChild(ambientLightNode);
//
//        // Set up the directional lights
//        Color3f light1Color = new Color3f(1.0f, 1.0f, 1.0f);
//        Vector3f light1Direction = new Vector3f(0.0f, -0.2f, -1.0f);
//
//        DirectionalLight light1 = new DirectionalLight(light1Color,
//                light1Direction);
//        light1.setInfluencingBounds(bounds);
//        objRoot.addChild(light1);
//
//        return objRoot;
//    }
//
//    public void init() {
//
//        // set up a NumFormat object to print out float with only 3 fraction
//        // digits
//        nf = NumberFormat.getInstance();
//        nf.setMaximumFractionDigits(3);
//
//        setLayout(new BorderLayout());
//        GraphicsConfiguration config = SimpleUniverse
//                .getPreferredConfiguration();
//
//        canvas = new Canvas3D(config);
//
//        add("Center", canvas);
//
//        // Create a simple scene and attach it to the virtual universe
//        BranchGroup scene = createSceneGraph();
//        u = new SimpleUniverse(canvas);
//
//        if (isApplication) {
//            offScreenCanvas = new OffScreenCanvas3D(config, true);
//            // set the size of the off-screen canvas based on a scale
//            // of the on-screen size
//            Screen3D sOn = canvas.getScreen3D();
//            Screen3D sOff = offScreenCanvas.getScreen3D();
//            Dimension dim = sOn.getSize();
//            dim.width *= 1.0f;
//            dim.height *= 1.0f;
//            sOff.setSize(dim);
//            sOff.setPhysicalScreenWidth(sOn.getPhysicalScreenWidth()
//                    * offScreenScale);
//            sOff.setPhysicalScreenHeight(sOn.getPhysicalScreenHeight()
//                    * offScreenScale);
//
//            // attach the offscreen canvas to the view
//            u.getViewer().getView().addCanvas3D(offScreenCanvas);
//        }
//
//        // This will move the ViewPlatform back a bit so the
//        // objects in the scene can be viewed.
//        u.getViewingPlatform().setNominalViewingTransform();
//        u.addBranchGraph(scene);
//
//        view = u.getViewer().getView();
//
//        add("South", guiPanel());
//    }
//
//    // create a panel with a tabbed pane holding each of the edit panels
//    JPanel guiPanel() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new GridLayout(0, 1));
//
//        if (isApplication) {
//            JButton snapButton = new JButton(snapImageString);
//            snapButton.setActionCommand(snapImageString);
//            snapButton.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    doSnapshot();
//                }
//            });
//            panel.add(snapButton);
//        }
//
//        return panel;
//    }
//
//    void doSnapshot() {
//        Point loc = canvas.getLocationOnScreen();
//        offScreenCanvas.setOffScreenLocation(loc);
//        Dimension dim = canvas.getSize();
//        dim.width *= offScreenScale;
//        dim.height *= offScreenScale;
//        nf.setMinimumIntegerDigits(3);
//        offScreenCanvas.snapImageFile("lineTypes", dim.width, dim.height);
//        nf.setMinimumIntegerDigits(0);
//    }
//
//    public void destroy() {
//        u.removeAllLocales();
//    }
//}
//
//class OffScreenCanvas3D extends Canvas3D {
//
//    OffScreenCanvas3D(GraphicsConfiguration graphicsConfiguration,
//                      boolean offScreen) {
//
//        super(graphicsConfiguration, offScreen);
//    }
//
//    private BufferedImage doRender(int width, int height) {
//
//        BufferedImage bImage = new BufferedImage(width, height,
//                BufferedImage.TYPE_INT_RGB);
//
//        ImageComponent2D buffer = new ImageComponent2D(
//                ImageComponent.FORMAT_RGB, bImage);
//        //buffer.setYUp(true);
//
//        setOffScreenBuffer(buffer);
//        renderOffScreenBuffer();
//        waitForOffScreenRendering();
//        bImage = getOffScreenBuffer().getImage();
//        return bImage;
//    }
//
//    void snapImageFile(String filename, int width, int height) {
//        BufferedImage bImage = doRender(width, height);
//
//    /*
//     * JAI: RenderedImage fImage = JAI.create("format", bImage,
//     * DataBuffer.TYPE_BYTE); JAI.create("filestore", fImage, filename +
//     * ".tif", "tiff", null);
//     */
//
//    /* No JAI: */
//        try {
//            FileOutputStream fos = new FileOutputStream(filename + ".jpg");
//            BufferedOutputStream bos = new BufferedOutputStream(fos);
//
//            JPEGImageEncoder jie = JPEGCodec.createJPEGEncoder(bos);
//            JPEGEncodeParam param = jie.getDefaultJPEGEncodeParam(bImage);
//            param.setQuality(1.0f, true);
//            jie.setJPEGEncodeParam(param);
//            jie.encode(bImage);
//
//            bos.flush();
//            fos.close();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//}