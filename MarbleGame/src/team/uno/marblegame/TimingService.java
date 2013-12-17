package team.uno.marblegame;

import java.util.Date;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class TimingService extends Service
{
	
	// This is the object that receives interactions from clients.
	/*private final IBinder mBinder = new LocalBinder();

	private  long dateOfLastSync;
	private  long timeNow;

	private final Handler handler = new Handler();
	private static Timer timer = new Timer();
	private Context ctx;*/


	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	/*public class LocalBinder extends Binder {

		TimingService getService() {
	        // Return this instance of LocalService so clients can call public methods
	        return TimingService.this;
	    }
	}

	@Override
	public void onCreate() {
	    ctx = this; 
	    startService();        
	}

	@Override
	public void onDestroy() {
	    Log.d("INTERNALCLOCK", "onDestroy");
	    Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();

	}

	private void startService()
	{           
	    timer.scheduleAtFixedRate(new mainTask(), 0, 1000);
	}

	private class mainTask extends TimerTask
	{ 
	    public void run() 
	    {
	        toastHandler.sendEmptyMessage(0);
	    }
	}    

	public void getTimeFromServer()  {
	    FetchServerTime fetchTime = new FetchServerTime();
	    fetchTime.execute();

	}

	@Override
	public IBinder onBind(Intent intent) {
	    Log.d("INTERNALCLOCK", "onBind");
	    getTimeFromServer();


	    return mBinder;
	}

	//method for clients 
	public long getTimeNow() {
	    Log.d("INTERNALCLOCK", "getTimeNow");
	  return timeNow;
	}

	public void startTimeCounter() {
	    Log.d("INTERNALCLOCK", "startTimeCounter");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Log.d("INTERNALCLOCK", "onStartCommand");

	    return START_STICKY;
	}

	private final Handler toastHandler = new Handler()
	{
	    @Override
	    public void handleMessage(Message msg)
	    {
	        timeNow += 1000;
	        Log.d("INTERNALCLOCK", "time is " + timeNow);
	        Toast.makeText(getApplicationContext(), "test", Toast.LENGTH_SHORT).show();
	    }
	}; 

	 class FetchServerTime extends AsyncTask<Integer, String, Long> {

	    private String mUrl;
	    private Date date;
	    private AlarmManager mAlarmManager;

	    public FetchServerTime() {
	        mUrl = "XXX";
	    }

	    @Override
	    protected Long doInBackground(Integer... params) {
	        (get server time code)
	    }

	    @Override
	    protected void onPostExecute(Long result) {
	        super.onPostExecute(result);
	        timeNow = result;
	        dateOfLastSync = result;
	    }




	}*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new TimingBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class TimingBinder extends Binder {
        TimingService getService() {
            return TimingService.this;
        }
    }

    @Override
    public void onCreate() {
       super.onCreate();
       Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // Tell the user we started.
        Toast.makeText(this, R.string.timing_service_started, Toast.LENGTH_SHORT).show();
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Toast.makeText(this, R.string.timer_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void startTimer()
    {
    	Toast.makeText(this, "Timer Started", Toast.LENGTH_SHORT).show();
    }
    
    public void stopTimer()
    {
    	Toast.makeText(this, "Timer Stopped", Toast.LENGTH_SHORT).show();
    }
    
    public void ResetTimer()
    {
    	Toast.makeText(this, "Timer Reset", Toast.LENGTH_SHORT).show();
    }
}
