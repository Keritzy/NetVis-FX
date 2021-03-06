package netvis.view.util.jogl.gameengine;

import com.jogamp.common.nio.Buffers;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class Painter {
    static {
        // Prepare the vertex buffer for the hexagon
        FloatBuffer dbh = FloatBuffer.allocate(3*6);
        for (int i = 0; i < 6; i++) {
            dbh.put ((float) Math.cos(Math.PI / 6 + i * Math.PI / 3));
            dbh.put ((float) Math.sin(Math.PI / 6 + i * Math.PI / 3));
            dbh.put ((float) 0.0);
        }

        dbh.rewind();

        VertexBuffer hexagon = new VertexBuffer (dbh);

        // Prepare the vertex buffer for the textured hexagon
        FloatBuffer dbht = FloatBuffer.allocate(5*6);
        for (int i = 0; i < 6; i++) {
            dbht.put ((float) Math.cos(Math.PI / 6 + i * Math.PI / 3));
            dbht.put ((float) Math.sin(Math.PI / 6 + i * Math.PI / 3));
            dbht.put ((float) 0.0);

            dbht.put (0.5f + (float) (0.5 * Math.cos(Math.PI / 6 + i * Math.PI / 3)));
            dbht.put (0.5f + (float) (0.5 * Math.sin(Math.PI / 6 + i * Math.PI / 3)));
        }

        dbht.rewind();

        VertexBuffer hexagont = new VertexBuffer (dbht);

        // Prepare the vertex buffer for the hexagon
        FloatBuffer dbs = FloatBuffer.allocate(8*4);
        for (int i = 0; i < 4; i++) {
            // Position
            dbs.put ((float) (Math.sqrt(2) * (Math.cos(Math.PI / 4 + i * Math.PI / 2))));
            dbs.put ((float) (Math.sqrt(2) * (Math.sin(Math.PI / 4 + i * Math.PI / 2))));
            dbs.put ((float) 0.0);
            // Color
            dbs.put ((float) 0.5);
            dbs.put ((float) 0.5);
            dbs.put ((float) 0.5);
            //Texture coords
            dbs.put ((float) (0.5 + 0.5 * Math.sqrt(2) * (Math.cos(Math.PI / 4 + i * Math.PI / 2))));
            dbs.put ((float) (0.5 + 0.5 * Math.sqrt(2) * (Math.sin(Math.PI / 4 + i * Math.PI / 2))));
        }

        dbs.rewind();

        VertexBuffer square = new VertexBuffer (dbs);

        VertexBufferPool.putBuffer("hexagon", hexagon);
        VertexBufferPool.putBuffer("hexagontexture", hexagont);
        VertexBufferPool.putBuffer("square", square);
    }

    public static void generateGrid(String name, int dim) {
        FloatBuffer gb = FloatBuffer.allocate (2*6*3 * Units.dimToCap(dim));

        for (int i = 0; i < Units.dimToCap(dim); i++) {
            Position rs = Units.findSpotAround(i);
            Position co = Units.coordinateByRingAndShift(1, rs.x, rs.y);
            Position xy = Units.positionByCoordinate(1000, co);

            float dx = (float) xy.x / 1000;
            float dy = (float) xy.y / 1000;

            for (int k = 0; k < 6; k++) {
                gb.put (dx + (float) Math.cos(Math.PI / 6 + k * Math.PI / 3));
                gb.put (dy + (float) Math.sin(Math.PI / 6 + k * Math.PI / 3));
                gb.put (0.0f);

                gb.put (dx + (float) Math.cos(Math.PI / 6 + (k+1) * Math.PI / 3));
                gb.put (dy + (float) Math.sin(Math.PI / 6 + (k+1) * Math.PI / 3));
                gb.put (0.0f);
            }
        }

        gb.rewind();

        VertexBuffer grid = new VertexBuffer (gb);

        VertexBufferPool.putBuffer (name, grid);
    }

    public static void drawGrid(int base, int dim, String name, GL2 gl) {
        int bid = VertexBufferPool.get(name).bind(gl);

        gl.glBindBuffer (GL2.GL_ARRAY_BUFFER, bid);

        gl.glPushMatrix();
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

        gl.glScaled (base, base, 1.0);

        //Define the dataformat
        gl.glVertexPointer (3, GL2.GL_FLOAT, 3* Buffers.SIZEOF_FLOAT, 0);

        gl.glLineWidth(1.0f);
        gl.glColor3d(0.8, 0.8, 0.8);

        gl.glDrawArrays (GL2.GL_LINES, 0, 12 * Units.dimToCap(dim));

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glPopMatrix();
    }

    /*
	 * Rotation is in degrees
	 */
    public static void drawImage(Texture tex, double cx, double cy, double scale, double rot, double opacity, GL2 gl) {
        int w = tex.getW();
        int h = tex.getH();

        // Bind to the texture
        int id = tex.bind(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);

        // Set the opacity
        gl.glColor4d(1.0, 1.0, 1.0, opacity);

        // Draw the image
        drawSquare(w, h, cx, cy, scale, rot, gl);
    }

    public static void drawImageHex(Texture tex, int mode, double cx, double cy, double scale, double rot, double opacity, GL2 gl) {
        int w = tex.getW();
        @SuppressWarnings("unused")
        int h = tex.getH();

        // Bind to the texture
        int id = tex.bind(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, mode);

        // Draw the image
        Painter.drawHexagonTextured(GL2.GL_POLYGON, cx, cy, (int) Math.round(scale * w / 2.0), rot, gl);
    }

    public static void drawSquare(int w, int h, double cx, double cy, double scale, double rot, GL2 gl) {
        int bid = VertexBufferPool.get("square").bind(gl);

        //gl.glBindBuffer (GL2.GL_ARRAY_BUFFER, 0);
        //gl.glVertexPointer (3, GL2.GL_DOUBLE, 0, null);
        gl.glBindBuffer (GL2.GL_ARRAY_BUFFER, bid);

        gl.glEnable(GL2.GL_TEXTURE_2D);
        //gl.glEnable(GL2.GL_TEXTURE_2D_MULTISAMPLE);
        gl.glPushMatrix();

        gl.glEnableClientState (GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState (GL2.GL_COLOR_ARRAY);
        gl.glEnableClientState (GL2.GL_TEXTURE_COORD_ARRAY);

        gl.glScaled (scale*w/2.0, scale*h/2.0, 1.0);
        gl.glTranslated (cx, cy, 0.0);
        gl.glRotated(rot, 0.0, 0.0, 1.0);

        //gl.glVertexPointer (3, GL2.GL_DOUBLE, 6 * Buffers.SIZEOF_DOUBLE, 0);
        gl.glVertexPointer   (3, GL2.GL_FLOAT, 8*Buffers.SIZEOF_FLOAT, 0);
        gl.glColorPointer    (3, GL2.GL_FLOAT, 8*Buffers.SIZEOF_FLOAT, 3*Buffers.SIZEOF_FLOAT);
        gl.glTexCoordPointer (2, GL2.GL_FLOAT, 8*Buffers.SIZEOF_FLOAT, 6*Buffers.SIZEOF_FLOAT);

        //gl.glColor3d (0.0, 0.0, 0.0);
        gl.glDrawArrays(GL2.GL_POLYGON, 0, 4);

        gl.glDisableClientState (GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState( GL2.GL_COLOR_ARRAY );
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

        gl.glPopMatrix();
        //gl.glDisable(GL2.GL_TEXTURE_2D_MULTISAMPLE);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    public static void drawSquareImmediate(int w, int h, double cx, double cy, double scale, double rot, GL2 gl) {
        gl.glEnable(GL2.GL_TEXTURE_2D);
        //gl.glEnable(GL2.GL_TEXTURE_2D_MULTISAMPLE);
        gl.glPushMatrix();

        // Translate and rotate (rotation in degrees)
        gl.glTranslated(cx, cy, 0.0);
        gl.glRotated(rot, 0.0, 0.0, 1.0);

        gl.glBegin(GL2.GL_POLYGON);
        gl.glTexCoord2d(1, 0);
        gl.glVertex2d(-scale * w / 2.0, scale * h / 2.0);
        gl.glTexCoord2d(0, 0);
        gl.glVertex2d(+scale * w / 2.0, scale * h / 2.0);
        gl.glTexCoord2d(0, 1);
        gl.glVertex2d(+scale * w / 2.0, -scale * h / 2.0);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(-scale * w / 2.0, -scale * h / 2.0);
        gl.glEnd();

        gl.glPopMatrix();
        //gl.glDisable(GL2.GL_TEXTURE_2D_MULTISAMPLE);
        gl.glDisable(GL.GL_TEXTURE_2D);

		/*
		 * Draw the lines around gl.glPushMatrix();
		 *
		 * // Translate and rotate (rotation in degrees) gl.glTranslated(cx, cy,
		 * 0.0); gl.glRotated (rot, 0.0, 0.0, 1.0); gl.glColor3d(0.0, 0.0, 0.0);
		 *
		 * gl.glBegin (GL2.GL_LINE_LOOP); gl.glVertex2d (-scale*w/2.0,
		 * scale*h/2.0); gl.glVertex2d (+scale*w/2.0, scale*h/2.0);
		 * gl.glVertex2d (+scale*w/2.0, -scale*h/2.0); gl.glVertex2d
		 * (-scale*w/2.0, -scale*h/2.0); gl.glEnd (); gl.glPopMatrix();
		 */
    }

    public static void drawHexagon(int mode, double x, double y, int base, GL2 gl) {
        int bid = VertexBufferPool.get("hexagon").bind(gl);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bid);

        gl.glPushMatrix();
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

        gl.glTranslated(x, y, 0.0);
        gl.glScaled(base, base, 1.0);

        //Define the dataformat
        gl.glVertexPointer(3, GL2.GL_FLOAT, 3 * Buffers.SIZEOF_FLOAT, 0);

        gl.glDrawArrays(mode, 0, 6);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glPopMatrix();
    }

    public static void drawHexagonTextured(int mode, double x, double y, int base, double rot, GL2 gl) {
        int bid = VertexBufferPool.get("hexagontexture").bind(gl);

        gl.glBindBuffer (GL2.GL_ARRAY_BUFFER, bid);

        gl.glEnable(GL2.GL_TEXTURE_2D);
        //gl.glEnable(GL2.GL_TEXTURE_2D_MULTISAMPLE);

        gl.glPushMatrix();
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState (GL2.GL_TEXTURE_COORD_ARRAY);

        gl.glTranslated (x, y, 0.0);
        gl.glScaled(base, base, 1.0);
        gl.glRotated(rot, 0.0, 0.0, 1.0);

        //Define the dataformat
        gl.glVertexPointer(3, GL2.GL_FLOAT, 5 * Buffers.SIZEOF_FLOAT, 0);
        gl.glTexCoordPointer(2, GL2.GL_FLOAT, 5 * Buffers.SIZEOF_FLOAT, 3 * Buffers.SIZEOF_FLOAT);

        gl.glDrawArrays(mode, 0, 6);

        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glPopMatrix();

        gl.glDisable(GL2.GL_TEXTURE_2D);
        //gl.glDisable (GL2.GL_TEXTURE_2D_MULTISAMPLE);
    }

    public static void drawHexagonImmediate(int mode, double x, double y, int base, GL2 gl) {
        gl.glBegin(mode);
        for (int i = 0; i < 6; i++) {
            gl.glVertex2d(x + base * Math.cos(Math.PI / 6 + i * Math.PI / 3),
                    y + base * Math.sin(Math.PI / 6 + i * Math.PI / 3));
        }
        gl.glEnd();
    }

    public static void drawGridUnoptimized(int base, GL2 gl) {
        // Draw the usual hexagon
        for (int i = -20; i < 20; i++) {
            for (int j = -20; j < 20; j++) {
                gl.glLineWidth(1.0f);
                gl.glColor3d(0.8, 0.8, 0.8);
                double dx = base / 2 * Math.sqrt(3);
                if (j % 2 == 0)
                    dx = 0;
                drawHexagon(GL2.GL_LINE_LOOP, base * Math.sqrt(3) * i + dx, base * 1.5 * j, 400, gl);
            }
        }
    }

    public static void traceCurve(double[] arr, int size, GL2 gl) {
        DoubleBuffer ctrlPointBuffer = DoubleBuffer.allocate(3 * size);

        for (int i = 0; i < 3 * size; i++) {
            ctrlPointBuffer.put(arr[i]);
        }

        ctrlPointBuffer.rewind();

        for (int i = 0; i < size - 10; i += 9) {
            gl.glMap1d(GL2.GL_MAP1_VERTEX_3, 0.0, 1.0, 3, 10, arr, 3 * i);
            // gl.glMap2d (GL2.GL_MAP2_VERTEX_3, 0.0, 1.0, 3, size, 0.0, 1.0, 3,
            // 1, ctrlPointBuffer);
            gl.glEnable(GL2.GL_MAP1_VERTEX_3);
            gl.glLineWidth(2.0f);
            gl.glPointSize(3.0f);
            gl.glColor3d(0.0, 0.0, 0.0);
            gl.glMapGrid1d(3, 0.0, 1.0);
            gl.glEvalMesh1(GL2.GL_LINE, 0, 3);
            if (i / 2 % 2 == 0)
                gl.glColor3d(1.0, 0.0, 0.0);
            else
                gl.glColor3d(0.0, 1.0, 0.0);
            gl.glEvalMesh1(GL2.GL_POINT, 0, 3);
            gl.glDisable(GL2.GL_MAP1_VERTEX_3);
        }
    }

    public static void drawCircle(double x, double y, double rad, GL2 gl) {
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        int parts = 50;
        gl.glColor4d(0.5, 0.2, 0.0, 1.0);
        gl.glVertex2d(x, y);
        gl.glColor4d(0.7, 0.3, 0.1, 0.4);
        for (int i = 0; i <= parts; i++) {
            // Rotate
            double xx = x + rad * Math.cos((2 * Math.PI * i) / parts);
            double yy = y + rad * Math.sin((2 * Math.PI * i) / parts);
            gl.glVertex2d(xx, yy);
        }
        gl.glEnd();
    }

    public static void stressTest(int base, GL2 gl) {
        for (int i=0; i<10000; i++) {
            int x = base * (i % (int) Math.round(Math.sqrt(10000)));
            int y = 2 * base * (i / (int) Math.round(Math.sqrt(10000)));

            gl.glPushMatrix();
            gl.glTranslatef(x, y, 0.0f);

            gl.glColor3d(0.5, 0.5, 0.4);
            Painter.drawHexagon(GL2.GL_POLYGON, 0.0, 0.0, base, gl);
            gl.glColor3d(0.7, 0.7, 0.6);
            Painter.drawSquare(100, 100, 0.0, 0.0, 1.0, 0.0, gl);

            gl.glPopMatrix();
        }
    }

    public static void stressTestList(int base, GL2 gl) {
        int	index = gl.glGenLists(1);

        gl.glNewList(index, GL2.GL_COMPILE);
        gl.glColor3d(0.5, 0.5, 0.4);
        Painter.drawHexagon(GL2.GL_POLYGON, 0.0, 0.0, base, gl);
        gl.glColor3d(0.7, 0.7, 0.6);
        Painter.drawSquare(100, 100, 0.0, 0.0, 1.0, 0.0, gl);
        gl.glEndList();

        for (int i=0; i<10000; i++) {
            int x = base * (i % (int) Math.round(Math.sqrt(10000)));
            int y = 2 * base * (i / (int) Math.round(Math.sqrt(10000)));

            gl.glPushMatrix();
            gl.glTranslatef(x, y, 0.0f);
            //gl.glTranslated(x, y, 0.0);

            gl.glCallList(index);
            gl.glPopMatrix();
        }

        gl.glDeleteLists(index, 1);
    }
}
