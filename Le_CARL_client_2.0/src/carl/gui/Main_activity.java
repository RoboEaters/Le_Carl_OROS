package carl.gui;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import carl.inputs.Camera_feedback;
import carl.inputs.Sensors_listener;
import carl.threads.Cam_udp_thread;
import carl.threads.IOIO_thread;
import carl.threads.IOIO_udp_thread;
import carl.threads.Sensors_udp_thread;
import carl.threads.TCP_client_thread;

// I've copied what was in IOIOActivity to this activity 
public class Main_activity extends Activity implements IOIOLooperProvider 		// implements IOIOLooperProvider: from IOIOActivity
{
	private final IOIOAndroidApplicationHelper helper_ = new IOIOAndroidApplicationHelper(this, this);			// from IOIOActivity

	final String tag = "Main";
	Main_activity the_activity;				//reference to this activity	

	/***************************************************************   Android Inputs   ***************************************************************/
	SensorManager sensorManager;	
	public Sensors_listener sensor_listener;
	Sensor compass, accelerometer;
	public Camera_feedback the_cam;
	SurfaceView the_view;

	/***************************************************************   Threads   ***************************************************************/
	public Cam_udp_thread udp_thread_cam;
	public Sensors_udp_thread udp_thread_sensors;
	public IOIO_udp_thread udp_thread_ioio;

	/***************************************************************   IOIO  ***************************************************************/
	public IOIO_thread the_IOIO;
	boolean STREAM_IOIO=false, RC_MODE=false;
	byte INVERTED=0;

	/***************************************************************   GUI stuff   ***************************************************************/
	My_spinner_Class spinner_IP, spinner_port;
	EditText ip_text, port_text;
	Button button_add_IP, button_delete_IP, button_add_port, button_delete_port;
	public ToggleButton button_connect;	

	PowerManager.WakeLock wake;

	/***************************************************************   Networking    ***************************************************************/
	TCP_client_thread tcp_client;
	String IP_server = null;
	public int port_TCP, port_camera, port_sensors, port_IOIO, port_NN;

	/***************************************************************   extra variables   ***************************************************************/
	public int idx_size_cam; //index used to set size of image from camera
	boolean SENSORS_STARTED, CAMERA_STARTED, IOIO_STARTED;
	
	/****************************************************************************** opencv 2.4.5*********************************************************************************/	
	BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(tag, "OpenCV loaded successfully");

	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_acti);

		the_activity = this;
		helper_.create();	// from IOIOActivity

		the_view = new SurfaceView(this);	
		SENSORS_STARTED = false;
		CAMERA_STARTED = false;
		IOIO_STARTED = false;
	
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		/****************************************************************************** opencv 2.4.5 *********************************************************************************/		
		Log.i(tag, "Trying to load OpenCV library");
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_5, this, mLoaderCallback))
		{
		  Log.e(tag, "Cannot connect to OpenCV Manager");
		}		

		/********************************************************************************************************************************************************************/
		/****************************************************************************** GUI *********************************************************************************/
		/********************************************************************************************************************************************************************/
		ip_text = (EditText) findViewById(R.id.txt_IP);
		port_text = (EditText) findViewById(R.id.txt_port);
		spinner_IP = (My_spinner_Class)findViewById(R.id.spinner_IP);
		spinner_port = (My_spinner_Class)findViewById(R.id.spinner_ports);
		button_add_IP = (Button) findViewById(R.id.btn_add_IP);
		button_delete_IP= (Button) findViewById(R.id.btn_delete_IP);
		button_add_port = (Button) findViewById(R.id.btn_add_port);
		button_delete_port = (Button) findViewById(R.id.btn_delete_port);
		button_connect = (ToggleButton) findViewById(R.id.btn_connect);
		set_buttons(false);
		button_connect.requestFocus();

		spinner_IP.set_file_name("IP_clients.txt");
		spinner_port.set_file_name("ports_clients.txt");

		spinner_IP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
			{
				IP_server = spinner_IP.getSelected();		
				Toast.makeText(the_activity, "IP_address: " + IP_server, Toast.LENGTH_SHORT).show();	
				set_buttons(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});	

		spinner_port.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
			{
				String port = spinner_port.getSelected();
				port_TCP = Integer.parseInt(port);
				Toast.makeText(the_activity, "Port: " + port, Toast.LENGTH_SHORT).show();	
				set_buttons(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});	

		button_add_IP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				IP_server = ip_text.getText().toString(); 
				ip_text.setText("");
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(ip_text.getWindowToken(), 0);
				spinner_IP.addItem(IP_server);
				ip_text.clearFocus();
				set_buttons(true);
			}
		});		

		button_add_port.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				String port = port_text.getText().toString(); 

				try
				{
					port_TCP = Integer.parseInt(port);
					spinner_port.addItem(port);
					set_buttons(true);
				}
				catch(java.lang.NumberFormatException e)
				{
					AlertDialog alertDialog;
					alertDialog = new AlertDialog.Builder(the_activity).create();
					alertDialog.setTitle("Error port");
					alertDialog.setMessage("enter a number  \n\n (press back to return)");
					alertDialog.show();			
				}

				port_text.setText("");
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(port_text.getWindowToken(), 0);
				port_text.clearFocus();
			}
		});		

		button_delete_IP.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				if(spinner_IP.remove_item() == false) set_buttons(false);
			}
		});		

		button_delete_port.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				if(spinner_port.remove_item() == false) set_buttons(false);
			}
		});		

		button_connect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) 
			{
				if (button_connect.isChecked()) Start_TCP_client();
				else  Stop_TCP_client();
			}
		});		
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wake = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		wake.acquire();		
	}

	private void set_buttons(boolean b)
	{
		button_delete_IP.setEnabled(b);
		button_delete_port.setEnabled(b);
		button_connect.setEnabled(b);
	}

	/********************************************************************************************************************************************************************/
	/************************************************************* TCP, VIDEO, SENSORS, IOIO *********************************************************************************/
	/********************************************************************************************************************************************************************/

	public void Start_TCP_client()
	{
		tcp_client = new TCP_client_thread(the_activity, IP_server, port_TCP);
		tcp_client.start();
	}

	public void Stop_TCP_client()		//stop everything if connection closed/lost
	{
		stop_all();
		if(tcp_client != null)tcp_client.stop_thread();
		tcp_client = null;
	}	

	public void stop_all()
	{
		stop_video();
		stop_sensors();
		stop_IOIO();
	}

	public void start_video()
	{
		if(CAMERA_STARTED == false)
		{
			the_cam = new Camera_feedback(the_view, idx_size_cam);	
			udp_thread_cam= new Cam_udp_thread(the_cam, IP_server, port_camera);
			udp_thread_cam.start();  
			CAMERA_STARTED = true;			
		}
	}

	public void stop_video()
	{
		if(CAMERA_STARTED == true)
		{
			udp_thread_cam.stop_thread();
			the_cam.stop_camera();	
			the_cam = null;
			CAMERA_STARTED = false;
		}
	}

	public void start_sensors(boolean stream)
	{	
		if(SENSORS_STARTED == false)
		{
			if(stream == false)
			{
				sensor_listener = new Sensors_listener(the_activity);	
			}
			else
			{
				sensor_listener = new Sensors_listener(the_activity);
				udp_thread_sensors = new Sensors_udp_thread(sensor_listener, IP_server, port_sensors);
				udp_thread_sensors.start();
			}

			compass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);	
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(sensor_listener, compass, SensorManager.SENSOR_DELAY_NORMAL);
			sensorManager.registerListener(sensor_listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			SENSORS_STARTED = true;
		}
	}

	public void stop_sensors()
	{
		if(SENSORS_STARTED == true)
		{
			if(udp_thread_sensors != null)udp_thread_sensors.stop_thread();
			sensorManager.unregisterListener(sensor_listener);
			SENSORS_STARTED = false;
		}
	}

	public void start_IOIO(boolean stream, byte inv, boolean RC)
	{
		if(IOIO_STARTED == false)
		{
			STREAM_IOIO = stream;
			RC_MODE = RC;
			INVERTED = inv;
			helper_.start();
			IOIO_STARTED = true;
		}
	}

	public void stop_IOIO()
	{
		if(IOIO_STARTED == true)
		{
			the_IOIO.get_ioio_vals().STOP();
			if(STREAM_IOIO == true)udp_thread_ioio.stop_thread();			
			helper_.stop();
			udp_thread_ioio=null;
			the_IOIO = null;
			IOIO_STARTED = false;
		}
	}

	/********************************************************************************************************************************************************************/
	/****************************************************** functions from IOIOActivity *********************************************************************************/
	/********************************************************************************************************************************************************************/

	protected IOIOLooper createIOIOLooper() 
	{
		//  !!!!!!!!!!!!!!!!!   create our own IOIO thread (Looper) with a reference to this activity
		the_IOIO = new IOIO_thread(this);

		if(udp_thread_ioio != null) udp_thread_ioio.stop_thread();
		udp_thread_ioio = null;

		if(STREAM_IOIO == true) 
		{
			udp_thread_ioio = new IOIO_udp_thread(the_IOIO, IP_server, port_IOIO, RC_MODE);
			udp_thread_ioio.start();
		} 		

		the_IOIO.set_inverted(INVERTED);

		return the_IOIO;
	}

	@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) 
	{
		return createIOIOLooper();
	}

	@Override
	protected void onDestroy() 
	{
		helper_.destroy();
		super.onDestroy();
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		//		helper_.start();
	}

	@Override
	protected void onStop() 
	{
		//		helper_.stop();
		
		wake.release();
		Stop_TCP_client();
		super.onStop();

		Log.i(tag, "stopping activity ");
		this.finish();
	}

	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) 
		{
			helper_.restart();
		}
	}

}