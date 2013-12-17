package team.uno.marblegame;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AccelerometerPlay extends Activity {

	//@Override
	//public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.accelerometer_play, menu);
		//return true;
	//}
	
	private SimulationView m_SimulationView;
    private SensorManager m_SensorManager;
    //private PowerManager m_PowerManager;
    private WindowManager m_WindowManager;
    private Display m_Display;
    //private WakeLock m_WakeLock;
    private MazeGenerator m_Maze;
    private final int m_MazeHeight = 15;
    private final int m_MazeWidth = 20;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //doBindService();
        //Binds the app to the service.
        //Intent myIntent = new Intent(AccelerometerPlay.this, TimingService.class);
        //startService(myIntent);

        //setContentView(R.layout.activity_accelerometer_play);
        
        // instantiate our simulation view and set it as the activity's content
        //m_SimulationView = (SimulationView)findViewById(R.id.Simulation);
        
        m_Maze = new MazeGenerator(m_MazeHeight, m_MazeWidth);
        
        // Get an instance of the SensorManager
        m_SensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the PowerManager
        //m_PowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        m_Display = m_WindowManager.getDefaultDisplay();

        // Create a bright wake lock
        //m_WakeLock = m_PowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());
        m_SimulationView = new SimulationView(this);
        //m_SimulationView.m_SensorManager = (SensorManager) savedInstanceState.get("SensorManeger");
        //m_SimulationView.m_Display = (Display) savedInstanceState.get("Display");
        setContentView(m_SimulationView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        /*
         * when the activity is resumed, we acquire a wake-lock so that the
         * screen stays on, since the user will likely not be fiddling with the
         * screen or buttons.
         */
        //m_WakeLock.acquire();

        // Start the simulation
        m_SimulationView.startSimulation();
        //doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
         * When the activity is paused, we make sure to stop the simulation,
         * release our sensor resources and wake locks
         */

        // Stop the simulation
        m_SimulationView.stopSimulation();

        //Intent myIntent = new Intent(AccelerometerPlay.this, TimingService.class);
        //stopService(myIntent);
        //doUnbindService();
        // and release our wake-lock
        //m_WakeLock.release();
    }

   
    
    class SimulationView extends View implements SensorEventListener {
        // diameter of the balls in meters
        private static final float s_BallDiameter = 0.004f;
        private static final float s_BallDiameter2 = s_BallDiameter * s_BallDiameter;


        public Sensor m_Accelerometer;
        /*public SensorManager m_SensorManager;
        public Display m_Display;
        private WindowManager m_WindowManager;*/

        private float m_XDpi;
        private float m_YDpi;
        private float m_Height;
        private float m_Width;
        private float m_MetersToPixelsX;
        private float m_MetersToPixelsY;
        private Bitmap m_Bitmap;
        private Bitmap m_Wood;
        private float m_XOrigin;
        private float m_YOrigin;
        private float m_SensorX;
        private float m_SensorY;
        private long m_SensorTimeStamp;
        private long m_CpuTimeStamp;
        private float m_HorizontalBound;
        private float m_VerticalBound;
        private float m_HorizontalUpperBound;
        private float m_HorizontalLowerBound;
        private float m_VerticalUpperBound;
        private float m_VerticalLowerBound;
        private final ParticleSystem m_ParticleSystem = new ParticleSystem();
        
        private int m_BallxIndex;
        private int m_BallyIndex;
        private float m_CellWidth;
        private float m_CellHeight;

    	private boolean m_Running = false;
    	private Button m_btnNewGame;
    	private Button m_btnPlayPause;
    	
        public static final int STATE_START = -1;
        public static final int STATE_PLAY = 0;
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_RUNNING = 3;
        
        public int m_State;

        
        public void startSimulation() {
            /*
             * It is not necessary to get accelerometer events at a very high
             * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
             * automatic low-pass filter, which "extracts" the gravity component
             * of the acceleration. As an added benefit, we use less power and
             * CPU resources.
             */
            m_SensorManager.registerListener(this, m_Accelerometer, SensorManager.SENSOR_DELAY_UI);
            m_State = STATE_START;
        }

        public void stopSimulation() {
            m_SensorManager.unregisterListener(this);
            m_State = STATE_PAUSE;
        }

        public SimulationView(Context context) {
            super(context);
            m_Accelerometer = m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            
            //m_SensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

            // Get an instance of the WindowManager
            //m_WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            //m_Display = m_WindowManager.getDefaultDisplay();

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            m_XDpi = metrics.xdpi;
            m_YDpi = metrics.ydpi;
            m_MetersToPixelsX = m_XDpi / 0.0254f;
            m_MetersToPixelsY = m_YDpi / 0.0254f;
            m_Height = metrics.heightPixels;
            m_Width = metrics.widthPixels;

            // rescale the ball so it's about 0.5 cm on screen
            Bitmap ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = (int) (s_BallDiameter * m_MetersToPixelsX + 0.5f);
            final int dstHeight = (int) (s_BallDiameter * m_MetersToPixelsY + 0.5f);
            m_Bitmap = Bitmap.createScaledBitmap(ball, dstWidth, dstHeight, true);

            Options opts = new Options();
            opts.inDither = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap wood = BitmapFactory.decodeResource(getResources(), R.drawable.wood, opts);
            m_Wood = Bitmap.createScaledBitmap(wood, (int)m_Width, (int) m_Height, true);
            
            m_Running = false;
            
        }

        /*
         * Each of our particle holds its previous and current position, its
         * acceleration. for added realism each particle has its own friction
         * coefficient.
         */
        class Particle {
            public float m_PosX;
            public float m_PosY;
            public float m_AccelX;
            public float m_AccelY;
            public float m_LastPosX;
            public float m_LastPosY;
            public float m_OneMinusFriction;
            
            // friction of the virtual table and air
            private static final float s_Friction = 1.5f;


            Particle() {
                // make each particle a bit different by randomizing its
                // coefficient of friction
                final float r = ((float) Math.random() - 0.5f) * 0.2f;
                m_OneMinusFriction = 1.0f - s_Friction + r;
            }

            public void computePhysics(float sx, float sy, float dT, float dTC) {
                // Force of gravity applied to our virtual object
                final float m = 1000.0f; // mass of our virtual object
                final float gx = -sx * m;
                final float gy = -sy * m;

                /*
                 * ·F = mA <=> A = ·F / m We could simplify the code by
                 * completely eliminating "m" (the mass) from all the equations,
                 * but it would hide the concepts from this sample code.
                 */
                final float invm = 1.0f / m;
                final float ax = gx * invm;
                final float ay = gy * invm;

                /*
                 * Time-corrected Verlet integration The position Verlet
                 * integrator is defined as x(t+Æt) = x(t) + x(t) - x(t-Æt) +
                 * a(t)Ætö2 However, the above equation doesn't handle variable
                 * Æt very well, a time-corrected version is needed: x(t+Æt) =
                 * x(t) + (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2 We also add
                 * a simple friction term (f) to the equation: x(t+Æt) = x(t) +
                 * (1-f) * (x(t) - x(t-Æt)) * (Æt/Æt_prev) + a(t)Ætö2
                 */
                final float dTdT = dT * dT;
                final float x = m_PosX + m_OneMinusFriction * dTC * (m_PosX - m_LastPosX) + m_AccelX
                        * dTdT;
                final float y = m_PosY + m_OneMinusFriction * dTC * (m_PosY - m_LastPosY) + m_AccelY
                        * dTdT;
                m_LastPosX = m_PosX;
                m_LastPosY = m_PosY;
                m_PosX = x;
                m_PosY = y;
                m_AccelX = ax;
                m_AccelY = ay;
            }

            /*
             * Resolving constraints and collisions with the Verlet integrator
             * can be very simple, we simply need to move a colliding or
             * constrained particle in such way that the constraint is
             * satisfied.
             */
            public void resolveCollisionWithBounds() {
                final int[][] maze = m_Maze.Maze();
                int left = 0;
                int right = m_MazeWidth-1;
                int up = 0;
                int down = m_MazeHeight-1;
                if((maze[m_BallyIndex][m_BallxIndex] & 1) == 0)
                {
                    up = m_BallyIndex;    
                }
                if((maze[m_BallyIndex][m_BallxIndex] & 8) == 0)
                {
                    left = m_BallxIndex + 1;    
                }
                if(m_BallxIndex < right && (maze[m_BallyIndex][m_BallxIndex + 1] & 8) == 0)
                {
                    right = m_BallxIndex + 2;
                }
                if(m_BallyIndex < down && (maze[m_BallyIndex + 1][m_BallxIndex] & 1) == 0)
                {
                    down = m_BallyIndex + 1;
                }
                m_HorizontalUpperBound = -m_HorizontalBound + (m_CellWidth * (float)right);
                m_HorizontalLowerBound = -m_HorizontalBound + (m_CellWidth * (float)left);
                m_VerticalUpperBound = m_VerticalBound - (m_CellHeight * (float)up);
                m_VerticalLowerBound = m_VerticalBound - (m_CellHeight * (float)down);
                final float xmax = m_HorizontalUpperBound;
                final float xmin = m_HorizontalLowerBound;
                final float ymax = m_VerticalUpperBound;
                final float ymin = m_VerticalLowerBound;
                final float x = m_PosX;
                final float y = m_PosY;
                if (x > xmax) {
                    m_PosX = xmax;
                } else if (x < xmin) {
                    m_PosX = xmin;
                }
                if (y > ymax) {
                    m_PosY = ymax;
                } else if (y < ymin) {
                    m_PosY = ymin;
                }
            }
        }

        /*
         * A particle system is just a collection of particles
         */
        class ParticleSystem {
            static final int NUM_PARTICLES = 1;
            private Particle mBalls[] = new Particle[NUM_PARTICLES];     
            
            private long m_LastT;
            private float m_LastDeltaT;
        	// diameter of the balls in meters
            
            ParticleSystem() {
                /*
                 * Initially our particles have no speed or acceleration
                 */
                for (int i = 0; i < mBalls.length; i++) {
                    mBalls[i] = new Particle();
                }
            }

            /*
             * Update the position of each particle in the system using the
             * Verlet integrator.
             */
            private void updatePositions(float sx, float sy, long timestamp) {
                final long t = timestamp;
                if (m_LastT != 0) {
                    final float dT = (float) (t - m_LastT) * (1.0f / 1000000000.0f);
                    if (m_LastDeltaT != 0) {
                        final float dTC = dT / m_LastDeltaT;
                        final int count = mBalls.length;
                        for (int i = 0; i < count; i++) {
                            Particle ball = mBalls[i];
                            ball.computePhysics(sx, sy, dT, dTC);
                        }
                    }
                    m_LastDeltaT = dT;
                }
                m_LastT = t;
            }
            
            /*
             * Performs one iteration of the simulation. First updating the
             * position of all the particles and resolving the constraints and
             * collisions.
             */
            public void update(float sx, float sy, long now) {
                // update the system's positions
                updatePositions(sx, sy, now);

                /*
                 * Resolve collisions, each particle is tested against every
                 * other particle for collision. If a collision is detected the
                 * particle is moved away using a virtual spring of infinite
                 * stiffness.
                 */
                Particle curr = mBalls[0];
                
                m_CellHeight = (0.90f * ((m_Height / m_MetersToPixelsY) / (float) m_MazeHeight));
                m_CellWidth = (0.98f * ((m_Width / m_MetersToPixelsX) / (float) m_MazeWidth));
                m_BallxIndex = (int)((curr.m_PosX + m_HorizontalBound) / m_CellWidth);
                m_BallyIndex = (int)(-(curr.m_PosY - m_VerticalBound) / m_CellHeight);
                
                //int row = (m_MazeHeight / 2) - curr.m_PosY;
                //int col = (m_MazeWidth / 2) - curr.m_PosX;
                //updateBoundaries(row, col);
                /*
                 * Finally make sure the particle doesn't intersects
                 * with the walls.
                 */
                curr.resolveCollisionWithBounds();
                
            }

            public int getParticleCount() {
                return mBalls.length;
            }

            public float getPosX(int i) {
                return mBalls[i].m_PosX;
            }

            public float getPosY(int i) {
                return mBalls[i].m_PosY;
            }
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
            m_XOrigin = (w - m_Bitmap.getWidth()) * 0.5f;
            m_YOrigin = (h - m_Bitmap.getHeight()) * 0.5f;
            //m_HorizontalBound = (0.90f * w) / 2; 
            m_HorizontalBound = ((w / m_MetersToPixelsX - s_BallDiameter) * 0.5f);
            //m_VerticalBound = (0.98f * h) / 2; 
            m_VerticalBound = ((h / m_MetersToPixelsY - s_BallDiameter) * 0.5f);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;
            /*
             * record the accelerometer data, the event's timestamp as well as
             * the current time. The latter is needed so we can calculate the
             * "present" time during rendering. In this application, we need to
             * take into account how the screen is rotated with respect to the
             * sensors (which always return data in a coordinate space aligned
             * to with the screen in its native orientation).
             */

            switch (m_Display.getRotation()) {
                case Surface.ROTATION_0:
                    m_SensorX = event.values[0];
                    m_SensorY = event.values[1];
                    break;
                case Surface.ROTATION_90:
                    m_SensorX = -event.values[1];
                    m_SensorY = event.values[0];
                    break;
                case Surface.ROTATION_180:
                    m_SensorX = -event.values[0];
                    m_SensorY = -event.values[1];
                    break;
                case Surface.ROTATION_270:
                    m_SensorX = event.values[1];
                    m_SensorY = -event.values[0];
                    break;
            }

            m_SensorTimeStamp = event.timestamp;
            m_CpuTimeStamp = System.nanoTime();
        }

        
        @Override
        protected void onDraw(Canvas canvas) {
        	switch (m_State)
        	{
        		case STATE_START:
        			onDrawing(canvas);
        			break;
        		case STATE_PLAY:
        			onDrawing(canvas);
        			break;
        		case STATE_LOSE:
        			onDrawing(canvas);
        			break;
        		case STATE_PAUSE:
        			onDrawing(canvas);
        			break;
        		case STATE_RUNNING:
        			onDrawing(canvas);
        			break;
        	}
        	//and make sure to redraw asap
            invalidate();
        }
        
        
        protected void onDrawing(Canvas canvas) {
        	
            /*
             * draw the background
             */

            canvas.drawBitmap(m_Wood, 0, 0, null);
            /*
             * compute the new position of our object, based on accelerometer
             * data and present time.
             */  
            float height = 0.90f * (m_Height / (float) m_MazeHeight);
            float width = 0.98f * (m_Width / (float) m_MazeWidth);
            
            final int[][] maze = m_Maze.Maze();
            for(int i = 0; i < m_MazeHeight; i++)
            {
        		for (int j = 0; j < m_MazeWidth; j++) 
        		{
        			if((maze[i][j] & 1) == 0)
        			{        				
        		        canvas.drawLine((j * width), (i * height), ((j + 1) * width), (i * height), m_Maze.Wall());
        			}
    			}
        		
    			// draw the west edge
    			for (int j = 0; j < m_MazeWidth; j++)
    			{
    				if((maze[i][j] & 8) == 0)
        			{        				
        		        canvas.drawLine((j * width), (i * height), (j * width), ((i + 1) * height), m_Maze.Wall());
        			}
    			}
            }
            
            //Draws Boundaries N, S, E, W
            canvas.drawLine(0f, 0f, (width * m_MazeWidth), 0f, m_Maze.Wall());
            canvas.drawLine(0f, (height * m_MazeHeight), (width * m_MazeWidth), (height * m_MazeHeight), m_Maze.Wall());
            canvas.drawLine((width * m_MazeWidth), 0f, (width * m_MazeWidth), (height * m_MazeHeight), m_Maze.Wall());
            canvas.drawLine(0f, 0f, 0f, (height * m_MazeHeight), m_Maze.Wall());

            final ParticleSystem particleSystem = m_ParticleSystem;
            final long now = m_SensorTimeStamp + (System.nanoTime() - m_CpuTimeStamp);
            final float sx = m_SensorX;
            final float sy = m_SensorY;

            particleSystem.update(sx, sy, now);

            final float xc = m_XOrigin;
            final float yc = m_YOrigin;
            final float xs = m_MetersToPixelsX;
            final float ys = m_MetersToPixelsY;
            final Bitmap bitmap = m_Bitmap;
            final int count = particleSystem.getParticleCount();
            for (int i = 0; i < count; i++) {
                /*
                 * We transform the canvas so that the coordinate system matches
                 * the sensors coordinate system with the origin in the center
                 * of the screen and the unit is the meter.
                 */

                final float x = xc + particleSystem.getPosX(i) * xs;
                final float y = yc - particleSystem.getPosY(i) * ys;
                canvas.drawBitmap(bitmap, x, y, null);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        
        public void btnPlayPause_Click(View v)
        {
        	if(m_Running)
        	{
        		Pause();
        	}
        	else
        	{
        		Play();
        	}
        }
        
        public void btnNewGame_Click(View v)
        {
        	NewGame();
        }
        
        private void Play()
        {
        	
        }
        private void Pause()
        {
        	
        }
        private void NewGame()
        {
        	
        }
    }
}
