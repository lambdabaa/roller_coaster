//A simulation of first person view on a roller coaster
//Authors: Daisy Zhuo, Gareth Aye
//Credit: Christopher Twigg (Carnegie Mellon University) project instruction on:
//http://graphics.cs.cmu.edu/nsp/course/15-462/Fall07/462/assts/Assn2.html

package roller_coaster;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import roller_coaster.Parser;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;


public class RollerCoaster implements GLEventListener {
	private static final Logger LOGGER = Logger.getLogger(RollerCoaster.class.getName());
	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;
	private static final int X0 = 100;
	private static final int Y0 = 50;
	private static final int REFRESH_RATE = 30;
	private static final int NEAR = 1;
	private static final int FAR = 35;
	
	private double dtheta = 0.008;
    private boolean pause = false;
    private boolean firstPerson = false;
    static Parser parser;
    static Parser parser2;
    
	private FPSAnimator animator;
	private GLWindow window;
	private GLU glu;
	private GLUT glut;
	private SpeedProvider speedProvider;
	private int speed;
    TextRenderer renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 16));
    
	private Texture groundTexture;
	private Texture railTexture;
	private Texture cartTexture;
	private Texture[] skyTexture = new Texture[5];
	
	private int pos = 0;
	private int num_rails = 1000;
	private Point3[] centerPos = new Point3[num_rails];
	
	private float aspect = 1;
	
	public RollerCoaster() {
        glu = new GLU();
        glut = new GLUT();
        speedProvider = new ConstantSpeedProvider().setSpeed(1);
        speed = speedProvider.getSpeed(this);

        for (int i = 0; i < num_rails; i++) {
        	
        	//centerPos[i] = new Point3(0.3*10.0*Math.sin(dtheta*i), 0, 0.3*10.0*Math.cos(dtheta*i));
        	centerPos[i] = new Point3(0.3*(i)/20.0*Math.sin(dtheta*i), 3*Math.sin(i/20.0)+3, 0.3*(i)/20.0*Math.cos(dtheta*i));
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
        
        window.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke){
                switch(ke.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_Q:
                    System.exit(0);
                case KeyEvent.VK_F:
                    firstPerson = !firstPerson; break;
                case KeyEvent.VK_UP:
                    speed += 1; break;
                case KeyEvent.VK_DOWN:
                    speed = Math.max(0, speed-1); break;
                case KeyEvent.VK_SPACE:
                    pause = !pause; break;
                }
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
        //gl.glEnable(GL2.GL_DEPTH_TEST);     // Enables Depth Testing
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);    // draw front faces filled
        gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINES);    // draw back faces with lines
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);	// Really Nice Perspective Calculations
		gl.glShadeModel(GL2.GL_SMOOTH); 
		gl.glColor4f(1.0f,1.0f,1.0f,0.5f);                  // Full Brightness, 50% Alpha ( NEW )
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);       // Blending Function For Translucency Based On Source Alpha Value ( NEW )
        
        groundTexture = loadTexture("./roller_coaster/data/ground.jpg");
        skyTexture[0] = loadTexture("./roller_coaster/data/otop7.jpg");
        skyTexture[1] = loadTexture("./roller_coaster/data/oleft7.jpg");
        skyTexture[2] = loadTexture("./roller_coaster/data/oback7.jpg");
        skyTexture[3] = loadTexture("./roller_coaster/data/oright7.jpg");
        skyTexture[4] = loadTexture("./roller_coaster/data/ofront7.jpg");
        railTexture = loadTexture("./roller_coaster/data/wood2.jpg");
        cartTexture = loadTexture("./roller_coaster/data/light_wood.jpg");
        if(groundTexture == null)
        	System.exit(0);
        groundTexture.enable(gl);
        cartTexture.enable(gl);
        for (int i = 0; i < 5; i++){
        	skyTexture[i].enable(gl);
        }

        renderText(drawable);
        //gl.glFlush();
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
		if (!pause) update();
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

    	gl.glDisable(GL2.GL_DEPTH_TEST);
        drawSkybox(gl);
    	gl.glEnable(GL2.GL_DEPTH_TEST);
        
    	drawGround(gl);
        
		for (int i = 1; i < num_rails; i++) {
			gl.glPushMatrix();
			gl.glTranslated(centerPos[i].getX(), centerPos[i].getY(), centerPos[i].getZ());
			drawRail(gl);
			gl.glPopMatrix();
		}
		drawCart(gl);
	}
	
    public void drawRail(GL2 gl) {

        railTexture.bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        for (int i = 1; i < parser.faces.length; i++){
        	gl.glPushMatrix();
        	gl.glBegin(GL2.GL_QUADS);
        	gl.glColor3i(255, 255, 255);
        	gl.glNormal3f(parser.normals[parser.faces[i].a_n].nx, parser.normals[parser.faces[i].a_n].ny, parser.normals[parser.faces[i].a_n].nz);
       	
        	gl.glTexCoord2f(0.0f, 0.0f); 
        	gl.glVertex3f(parser.vertices[parser.faces[i].a].x,parser.vertices[parser.faces[i].a].y, parser.vertices[parser.faces[i].a].z ); 
            gl.glTexCoord2f(1.0f, 0.0f); 
            gl.glVertex3f(parser.vertices[parser.faces[i].b].x,parser.vertices[parser.faces[i].b].y, parser.vertices[parser.faces[i].b].z ); 
            gl.glTexCoord2f(1.0f, 1.0f); 
            gl.glVertex3f(parser.vertices[parser.faces[i].c].x,parser.vertices[parser.faces[i].c].y, parser.vertices[parser.faces[i].c].z ); 
            gl.glTexCoord2f(0.0f, 1.0f); 
            gl.glVertex3f(parser.vertices[parser.faces[i].d].x,parser.vertices[parser.faces[i].d].y, parser.vertices[parser.faces[i].d].z ); 
            
        	//gl.glRotated(90, 0, 0, 0);
            //gl.glScaled(0.5d, 0.5d, 0.5d);
        	gl.glEnd();
        	gl.glPopMatrix();
        	if (parser.faces[i+1] == null) break;
        }

    }
    
    private void drawCart(GL2 gl) {
    	cartTexture.bind(gl);
    	gl.glPushMatrix();
    	gl.glTranslated(centerPos[(pos+5)%num_rails].getX(), centerPos[(pos+5)%num_rails].getY()+0.05f, centerPos[(pos+5)%num_rails].getZ());
    	gl.glRotated(180, 0, 1, 0);
    	Vector3 direction = new Vector3();
    	//System.out.println(centerPos[(pos+5)%num_rails+" "+direction.getZ()+" "+degree);
    	direction.sub(centerPos[(pos+1)%num_rails], centerPos[pos]);
    	direction.setY(0);
    	//direction.setZ(-direction.getZ());
    	direction.normalize();
    	double degree = direction.dot(new Vector3(-1, 0, 0)) * 180.0/Math.PI; 
    	System.out.println(direction.getX()+" "+direction.getZ()+" "+degree);
    	gl.glRotated(degree, 0, 1, 0);
    	gl.glScaled(8.0d, 8.0d, 8.0d);
    	//glut.glutSolidTeapot(0.05); 
    	for (int i = 1; i < parser2.faces.length; i++){
        	gl.glPushMatrix();
        	gl.glBegin(GL2.GL_QUADS);
        	gl.glColor3i(255, 255, 255);
    	    gl.glNormal3f(parser2.normals[parser2.faces[i].a_n].nx, parser2.normals[parser2.faces[i].a_n].ny, parser2.normals[parser2.faces[i].a_n].nz);
        	gl.glTexCoord2f(0.0f, 0.0f); 
        	gl.glVertex3f(parser2.vertices[parser2.faces[i].a].x,parser2.vertices[parser2.faces[i].a].y, parser2.vertices[parser2.faces[i].a].z ); 
	    gl.glNormal3f(parser2.normals[parser2.faces[i].b_n].nx, parser2.normals[parser2.faces[i].b_n].ny, parser2.normals[parser2.faces[i].b_n].nz);
            gl.glTexCoord2f(1.0f, 0.0f); 
            gl.glVertex3f(parser2.vertices[parser2.faces[i].b].x,parser2.vertices[parser2.faces[i].b].y, parser2.vertices[parser2.faces[i].b].z ); 
	    gl.glNormal3f(parser2.normals[parser2.faces[i].c_n].nx, parser2.normals[parser2.faces[i].c_n].ny, parser2.normals[parser2.faces[i].c_n].nz);
            gl.glTexCoord2f(1.0f, 1.0f); 
            gl.glVertex3f(parser2.vertices[parser2.faces[i].c].x,parser2.vertices[parser2.faces[i].c].y, parser2.vertices[parser2.faces[i].c].z ); 
	    gl.glNormal3f(parser2.normals[parser2.faces[i].d_n].nx, parser2.normals[parser2.faces[i].d_n].ny, parser2.normals[parser2.faces[i].d_n].nz);
            gl.glTexCoord2f(0.0f, 1.0f); 
            gl.glVertex3f(parser2.vertices[parser2.faces[i].d].x,parser2.vertices[parser2.faces[i].d].y, parser2.vertices[parser2.faces[i].d].z ); 
            
        	gl.glEnd();
        	gl.glPopMatrix();
        	if (parser2.faces[i+1] == null) break;
        }
    	gl.glPopMatrix();
    }
	
    private void drawGround(GL2 gl) { 
        groundTexture.bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20.0f, 0.0f,  -20.0f);  // Bottom Left Of The Texture and Quad
            gl.glTexCoord2f(10.0f, 0.0f); gl.glVertex3f(-20.0f, 0.0f,  -20.0f);  // Bottom Right Of The Texture and Quad
            gl.glTexCoord2f(10.0f, 10.0f); gl.glVertex3f(-20.0f,  0.0f,  20.0f);  // Top Right Of The Texture and Quad
            gl.glTexCoord2f(0.0f, 10.0f); gl.glVertex3f(20.0f,  0.0f,  20.0f);  // Top Left Of The Texture and Quad
        gl.glEnd();
    }
    
    private void drawSkybox(GL2 gl) {
    	gl.glPushMatrix();
    	gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    	//draw top
    	skyTexture[0].bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE); 
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Bottom Left Of The Texture and Quad
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 20f,  -20f);  // Bottom Right Of The Texture and Quad
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Right Of The Texture and Quad
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f, 20f);  // Top Left Of The Texture and Quad
	    gl.glEnd();
		
		//draw right
		skyTexture[3].bind(gl);
	    gl.glBegin(GL2.GL_QUADS);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 0f,  20f);  // Bottom Left Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-20f, 0f,  -20f);  // Bottom Right Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Top Right Of The Texture and Quad
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f,  20f);  // Top Left Of The Texture and Quad
	    gl.glEnd();
		
		//draw back
		skyTexture[2].bind(gl);
	    gl.glBegin(GL2.GL_QUADS);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20f, 0f,  20f);  // Bottom Left Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-20f, 0f,  20f);  // Bottom Right Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-20f, 20f,  20f);  // Top Right Of The Texture and Quad
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Left Of The Texture and Quad
	    gl.glEnd();
	    
		//draw left
	    skyTexture[1].bind(gl);
	    gl.glBegin(GL2.GL_QUADS);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(20f, 0f,  -20f);  // Bottom Left Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 0f,  20f);  // Bottom Right Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  20f);  // Top Right Of The Texture and Quad
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(20f, 20f,  -20f);  // Top Left Of The Texture and Quad
	    gl.glEnd();
	    
		//draw front quad
	    skyTexture[4].bind(gl);
	    gl.glBegin(GL2.GL_QUADS);
	        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-20f, 0f,  -20f);  // Bottom Left Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(20f, 0f,  -20f);  // Bottom Right Of The Texture and Quad
	        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(20f, 20f,  -20f);  // Top Right Of The Texture and Quad
	        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-20f, 20f,  -20f);  // Top Left Of The Texture and Quad
	    gl.glEnd();
		gl.glPopMatrix();
    }
    
    private Texture loadTexture(String string) {
        Texture texture = null;
        File imgFile = null;
        
        try {
            URL url = this.getClass().getClassLoader().getResource(string); 
            imgFile = new File(url.getFile());
            texture = TextureIO.newTexture(imgFile, true);
        } 
        catch (IOException e) {
            System.out.println("fail openning file... " + imgFile.getName());
            System.out.println(e);
        }
        
        return texture;
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
        if (!firstPerson) glu.gluLookAt(0,15,0, centerPos[(pos)%num_rails].getX(), centerPos[(pos)%num_rails].getY(), centerPos[(pos)%num_rails].getZ(), 0, 1, 0);
        else {
	        glu.gluLookAt(centerPos[pos].getX(), centerPos[pos].getY()+1, centerPos[pos].getZ(), 
	        		centerPos[(pos+20)%num_rails].getX(), centerPos[(pos+15)%num_rails].getY(), centerPos[(pos+10)%num_rails].getZ(), 
	        		centerPos[(pos)%num_rails].getX()/10.0, 1, 0);
        }
    }
	
    public void setLights(GL2 gl) {
        float[] lightPos = {-2, 15, -2, 1};
        float[] lightAmbient = { 0.5f, 0.5f, 0.5f, 1};
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1};
        float[] lightSpecular = {0.8f, 0.8f, 0.8f, 1};

        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);

        float[] lightPos2 = {5, 10, 5, 1};
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, lightPos2, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, lightSpecular, 0);

        float[] lightPos3 = {0, 5, 0, 1};
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, lightPos3, 0);
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, lightSpecular, 0);

        gl.glEnable(GL2.GL_LIGHTING); 
        gl.glDisable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHT2);
        gl.glEnable(GL2.GL_LIGHT3);
    }
	
    private void update() {
		pos += speed;
		pos %= num_rails;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
        parser = new Parser("./roller_coaster/untitled2.obj");
        parser2 = new Parser("./roller_coaster/dinghy.obj");
		LOGGER.info("Starting scene rendering...");
		new RollerCoaster();
	}
	
    public void renderText(GLAutoDrawable drawable) {
        int w = drawable.getWidth(), h = drawable.getHeight();
        renderer.setColor(0, 0, 0, 0.8f);
        renderer.beginRendering(w, h);
        String st = "<Up/Down Arrows: Increase/Decrease Speed";
        renderer.draw(st, 20, h-24);
        st = "Space: pause/resume; F: first-person/third-persn view";
        renderer.draw(st, 20, h-48);
        st += (firstPerson ? "first person" : "third person") + " view";
        renderer.draw(st, 5, 5);
        renderer.endRendering();
    }
}
