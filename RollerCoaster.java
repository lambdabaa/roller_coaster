package roller_coaster;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import roller_coaster.Parser;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

public class RollerCoaster implements GLEventListener {
	private static final Logger LOGGER = Logger.getLogger(RollerCoaster.class.getName());
	private static final int WIDTH = 600;
	private static final int HEIGHT = 400;
	private static final int X0 = 100;
	private static final int Y0 = 50;
	private static final int REFRESH_RATE = 5;
	private static final int NEAR = 1;
	private static final int FAR = 10;
	private static int lightMode = 3;
	
	private FPSAnimator animator;
	private GLWindow window;
	//private GL2 gl;
	private GLU glu;
	private GLUT glut;
	private SpeedProvider speedProvider;
	
	private int pos = 0;
	private List<Line> lines = new LinkedList<Line>();
	
	private float aspect = 1;
	
	public RollerCoaster() {
        glu = new GLU();
        glut = new GLUT();
        speedProvider = new ConstantSpeedProvider().setSpeed(1);

        for (int i = 0; i < 50; i++) {
        	lines.add(new Line()
        		.setP1(new Point3()
        			.setX(i / (double) 10)
        			.setY(i / (double) 10)
        			.setZ(i / (double) 10))
        		.setP2(new Point3()
        			.setX((i + 1) / (double) 10)
        			.setY((i + 1) / (double) 10)
        			.setZ((i + 1) / (double) 10)));
        }
        
		window = GLWindow.create(new GLCapabilities(GLProfile.getDefault()));
        window.setSize(WIDTH, HEIGHT);
        window.setVisible(true);
        window.setTitle("RollerCoaster");
        window.setPosition(X0, Y0);
        window.addGLEventListener(this);
        window.addWindowListener(new WindowAdapter() {
            public void windowDestroyNotify(WindowEvent arg0) {
                LOGGER.info("Terminating...");
            	System.exit(0);
            }
        });
        
        animator = new FPSAnimator(window, REFRESH_RATE);
        animator.add(window);
        animator.start();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);     // Enables Depth Testing
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);    // draw front faces filled
        gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);    // draw back faces with lines
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		LOGGER.info("Getting rid of object references...");
		animator = null;
		window = null;
		speedProvider = null;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		update();
		render(drawable);
	}
	
	public void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);   
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        

        setLights(gl); // positions lights
        setCam(gl);  // position camera
        
		for (Line line : lines) {
			drawRails(gl);
			gl.glTranslatef(0.6f, 0.0f, 0.0f);
		}

	}
	
    public void drawRails(GL2 gl) {
       // gl.glRotated(zAngle, 0, 0, 1);
        
        for (int i = 1; i < Parser.faces.length; i++){
        	gl.glPushMatrix();
        	gl.glBegin(GL.GL_TRIANGLES);
        	gl.glColor3i(255, 166, 30);
        	gl.glNormal3f(Parser.normals[Parser.faces[i].a_n].nx, Parser.normals[Parser.faces[i].a_n].ny, Parser.normals[Parser.faces[i].a_n].nz);
        	gl.glVertex3f(Parser.vertices[Parser.faces[i].a].x,Parser.vertices[Parser.faces[i].a].y, Parser.vertices[Parser.faces[i].a].z ); 
        	gl.glVertex3f(Parser.vertices[Parser.faces[i].b].x,Parser.vertices[Parser.faces[i].b].y, Parser.vertices[Parser.faces[i].b].z ); 
        	gl.glVertex3f(Parser.vertices[Parser.faces[i].c].x,Parser.vertices[Parser.faces[i].c].y, Parser.vertices[Parser.faces[i].c].z ); 
        	gl.glEnd();
        	gl.glPopMatrix();
        	if (Parser.faces[i+1] == null) break;
        }
    }
	

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
        GL2 gl = drawable.getGL().getGL2();

        // avoid a divide by zero error!
        if (height <= 0) { 
        	height = 1;
        }

        gl.glViewport(0, 0, width, height);
        aspect = width / (float) height;
        setCam(gl);
	}
	
	private void setCam(GL2 gl) {
        // set projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        glu.gluPerspective(45.0f, aspect, NEAR, FAR);
        
        // set camera location and angle
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        glu.gluLookAt(2, 2, -pos/10.0, 0, 0, 0, 0, 1, 0);
    }
	
    public void setLights(GL2 gl) {
        float[] lightPos = {20, 10, 10, 1};
        float[] lightAmbient = {0.2f, 0.2f, 0.2f, 1};
        float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1};
        float[] lightSpecular = {0.8f, 0.8f, 0.8f, 1};

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);

        if (lightMode == 1) {
            gl.glDisable(GL2.GL_LIGHTING);
            gl.glDisable(GL2.GL_LIGHT0);
            gl.glDisable(GL2.GL_LIGHT1);
        } else if (lightMode == 2) {
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT0);
            gl.glDisable(GL2.GL_LIGHT1);
        } else { // mode 3
            gl.glEnable(GL2.GL_LIGHTING); 
            gl.glDisable(GL2.GL_LIGHT0);
            gl.glEnable(GL2.GL_LIGHT1);
        }
    }
	private void update() {
		pos += speedProvider.getSpeed(this);
		pos %= 50;
		
	}
	
	public static void main(String[] args) throws FileNotFoundException {
        new Parser();
		LOGGER.info("Starting scene rendering...");
		RollerCoaster rollerCoaster = new RollerCoaster();
	}

}
