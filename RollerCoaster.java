package roller_coaster;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

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
	private static final int REFRESH_RATE = 60;
	private static final int NEAR = 1;
	private static final int FAR = 10;
	
	private FPSAnimator animator;
	private GLWindow window;
	private GL2 gl;
	private GLU glu;
	private SpeedProvider speedProvider;
	
	private int pos = 0;
	private List<Line> lines = new LinkedList<Line>();
	
	private float aspect = 1;
	
	public RollerCoaster() {
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
        
        glu = new GLU();
        
        animator = new FPSAnimator(window, REFRESH_RATE);
        animator.add(window);
        animator.start();
        
        speedProvider = new ConstantSpeedProvider().setSpeed(1);
        
        for (int i = 0; i < 50; i++) {
        	lines.add(new Line()
        		.setP1(new Point3().setX(i / 10).setY(i / 10).setZ(i / 10))
        		.setP2(new Point3().setX((i + 1) / 10).setY((i + 1) / 10).setZ((i + 1) / 10)));
        }
	}

	@Override
	public void init(GLAutoDrawable drawable) {
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
        glu.gluLookAt(0, 0, -5, 0, 0, 0, 0, 1, 0);
    }
	
	private void update() {
		pos += speedProvider.getSpeed(this);
		pos %= 50;
	}
	
	private void render(GLAutoDrawable drawable) {
		LOGGER.info("Rendering...");
		
		for (Line line : lines) {
			Point3 p1 = line.getP1();
			Point3 p2 = line.getP2();
			gl.glPushMatrix();
			gl.glColor3i(0, 255, 0);
			gl.glBegin(GL2.GL_LINES);
			gl.glVertex3d(p1.getX(), p1.getY(), p1.getZ());
			gl.glVertex3d(p2.getX(), p2.getY(), p2.getZ());
			gl.glEnd();
			gl.glPopMatrix();
		}
		
		Point3 p1 = lines.get(pos).getP1();
		Point3 p2 = lines.get(pos).getP2();
		
		gl.glColor3i(0, 0, 255);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3d(p1.getX(), p1.getY(), p1.getZ());
		gl.glVertex3d(p2.getX(), p2.getY(), p2.getZ());
		gl.glEnd();
	}
	
	public static void main(String[] args) {
		LOGGER.info("Starting scene rendering...");
		RollerCoaster rollerCoaster = new RollerCoaster();
	}

}
