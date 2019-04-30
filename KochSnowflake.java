import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

// Matthew King
// Fundamentals of Computer Graphics
// Completed 3 October 2018

public class Koch_Snowflake extends JFrame implements GLEventListener {	

	private static final long serialVersionUID = 1L;
	private GLCanvas myCanvas;
	private int rendering_program;
	
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	
	private GLSLUtils util = new GLSLUtils();
	private float[] vertex_positions = new float[2 * 2]; //Two points, two coordinates
	
	private float side_length; // Side length
	private int depth; // Recursion level
	
	public Koch_Snowflake(String[] args) {	
		setTitle("Project 1: Koch Snowflake");
		setSize(600, 600);
		
		//Making sure we get a GL4 context for the canvas 
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
		myCanvas = new GLCanvas(capabilities);
 		//end GL4 context
		
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
		
		// Assign args values to the variables
		side_length = Float.parseFloat(args[0]);
		depth = Integer.parseInt(args[1]);
		
		// These 2 variables are enough to calculate everything
		calculations(side_length, depth);
		}

	public void init(GLAutoDrawable drawable) {	
		GL4 gl = (GL4) drawable.getGL();
		rendering_program = createShaderProgram();

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		
		gl.glGenBuffers(vbo.length, vbo, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);

	}
	
	public void display(GLAutoDrawable drawable) {	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(rendering_program);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);//We are only passing two components
		gl.glEnableVertexAttribArray(0);

		// Call method that runs the project
		Snowflake(side_length, depth);
	}

	private void Snowflake(float side_length, int depth) {
		//Define the triangle
		float[] v1=new float[2];//Two coordinates
		float[] v2=new float[2];//Two coordinates
		float[] v3=new float[2];//Two coordinates
		//The first three vertices define the starting triangle
		//Equilateral triangle centered at the origin
		
		//Top vertex - x and y
		v1[0]=0; 
		v1[1]=side_length*(float)Math.sqrt(3)/3;
		
		//Bottom left
		v2[0]=-0.5f*side_length; 
		v2[1]=-(float)Math.sqrt(3)*side_length/6;
		
		//Bottom right
		v3[0]=0.5f*side_length; 
		v3[1]=-(float)Math.sqrt(3)*side_length/6;
		//Done defining triangle
	
		// Begin recursion
		createSegments(v2, v3, depth);
		createSegments(v3, v1, depth);
		createSegments(v1, v2, depth);
		
		// Mitsubishi logo if n = 1 *WARNING* n = 10 crashes computer
//		createSegments(v3, v2, depth);
//		createSegments(v1, v3, depth);
//		createSegments(v2, v1, depth);
	}
	
	//Processing triangles
	private void createSegments(float[] v1, float[] v2, int depth) {
		if (depth > 0) //Recurse
		{
			// Creation of the 5 points that the segment divides into
			float[] a = new float[2];
			float[] b = new float[2];
			float[] c = new float[2];
			float[] d = new float[2];
			float[] e = new float[2];
			
			// A point is the first point
			a[0] = v1[0];
			a[1] = v1[1];
			
			// Math for B point
			b[0] = v1[0] + (1.0f / 3.0f) * (v2[0] - v1[0]);
			b[1] = v1[1] + (1.0f / 3.0f) * (v2[1] - v1[1]);

			// Math for C point
			c[0] = ((1.0f/2.0f) * (v1[0] + v2[0])) + ((float)Math.sin(Math.toRadians(60)) * (v2[1] - v1[1]) / 3);
			c[1] = ((1.0f/2.0f) * (v1[1] + v2[1])) - ((float)Math.sin(Math.toRadians(60)) * (v2[0] - v1[0]) / 3); 
			
			// Math for D point
			d[0] = v2[0] - (1.0f/3.0f) * (v2[0] - v1[0]);
			d[1] = v2[1] - (1.0f/3.0f) * (v2[1] - v1[1]);
			
			// E point is the 2nd point
			e[0] = v2[0];
			e[1] = v2[1];
			
			// Continue recursion
			createSegments(a, b, depth - 1);
			createSegments(b, c, depth - 1);
			createSegments(c, d, depth - 1);
			createSegments(d, e, depth - 1);
		}
		else {
			// Draw
			drawSegments(v1, v2); //Draw
		}
		
	}

	private void drawSegments(float [] v1, float[] v2) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		//Store points in backing store
		vertex_positions[0]=v1[0];
		vertex_positions[1]=v1[1];
		vertex_positions[2]=v2[0];
		vertex_positions[3]=v2[1];

		// Buffer
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//Draw now
		gl.glDrawArrays(GL_LINES, 0, 2);

	}
	
	// Calculations
	public void calculations(float side_length, int depth) {
    	double numSides = 3 * Math.pow(4, depth);
    	double sideLength = side_length / Math.pow(3, depth);
    	double perimeter = numSides * sideLength;
    	double area = (2 * Math.pow(side_length, 2) * Math.sqrt(3)) / 5;
    	
    	System.out.println("Depth = " + depth);
    	System.out.println("Sides = " + numSides);
    	System.out.println("Side Length = " + sideLength);
    	System.out.println("Total Perimeter = " + perimeter);
    	System.out.println("Total area = " + area);
    }
	
	private int createShaderProgram() {	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		//String vshaderSource[] = util.readShaderSource("src\\opengl101\\Koch_Snowflake Shaders/vert.shader.txt");
		//String fshaderSource[] = util.readShaderSource("src\\opengl101\\Koch_Snowflake Shaders/frag.shader.txt");

		// Built in shaders so that source txts don't need to be worried about
		String vshaderSource[] =
			{ "#version 450	\n",
			  "layout (location=0) in vec2 position;",
			  "void main(void) \n",
			  "{ gl_Position = vec4(position, 0, 1); } \n" 
			};
		
		String fshaderSource[] =
			{ "#version 450	\n",
			  "out vec4 color;",
			  "void main(void) \n", 
			  "{ color = vec4(1.0, 1.0, 1.0, 1.0);} \n"
			}; 

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}

	public static void main(String[] args) {
		// Constructor passing the command line args
		new Koch_Snowflake(args); 
		}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}
