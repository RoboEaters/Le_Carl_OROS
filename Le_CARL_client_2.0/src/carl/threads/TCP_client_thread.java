package carl.threads;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import carl.gui.Main_activity;

import android.hardware.Camera;
import android.hardware.Camera.Size;

import android.os.Build;
import android.util.Log;

public class TCP_client_thread extends Thread 
{
	String IP_add;
	int port_TCP;
	Socket the_socket;
	boolean STOP = false;
	Main_activity main_acti;
	private static final String TAG = "TCP_thread";
	String message_TCP;
	InetSocketAddress addr;
	BufferedWriter out;
	BufferedReader input;
	boolean CLOSED_SOCKET=false;

	public TCP_client_thread(Main_activity the_activity, String IP, int port)
	{
		IP_add = IP.toString();
		port_TCP = port;
		main_acti = the_activity;
		the_socket = new Socket();
		message_TCP = new String();

		message_TCP = "PHONE/" + Build.MODEL + " " + Build.MANUFACTURER + " " + Build.PRODUCT;

		try	// get supported sizes of camera...to send to the server
		{
			Camera mCamera = Camera.open();        
			Camera.Parameters parameters = mCamera.getParameters(); 
			List<Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
			Size a_size;

			for(int i=0;i<mSupportedPreviewSizes.size();i++)
			{
				a_size = mSupportedPreviewSizes.get(i);

				message_TCP += "/" + Integer.toString(a_size.width) + "x"+ Integer.toString(a_size.height); 
			}
			message_TCP += "/\n";

			if (mSupportedPreviewSizes != null) 
			{
				Log.i(TAG, "nb supported sizes: " + mSupportedPreviewSizes.size());
			}
			mCamera.release(); 
		}
		catch(Exception e){Log.e(TAG, "error camera");}
	}

	public void run()
	{
		addr = new InetSocketAddress(IP_add,port_TCP);
		try 
		{				
			the_socket.connect(addr, 1000);				//timeout 100 ms
			the_socket.setSoTimeout(500);
			out = new BufferedWriter(new OutputStreamWriter(the_socket.getOutputStream()));
			input = new BufferedReader(new InputStreamReader(the_socket.getInputStream()));

			out.write(message_TCP);
			out.flush();
			Log.i("tcp","send msg " + message_TCP);
		}
		catch(java.io.IOException e) 
		{
			STOP = true;
			Log.e("tcp","error connect: ", e);
		}

		while(STOP != true)
		{			
			String st=null;
			try
			{					
				st = input.readLine();
				//				Log.i("tcp","msg: "+st);
			}
			catch (java.net.SocketTimeoutException e) {}
			catch (IOException e) 
			{
				STOP = true;
				Log.e("tcp","error read: ", e);
			}

			if(st != null)
			{	        	
				final String[]sss= st.split("/");
				if(sss[0].matches("CAMERA_ON") == true)
				{	        		
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							int p = Integer.parseInt(sss[1]);
							int idx_cam = Integer.parseInt(sss[2]);
							Log.i("udp cam","running on port: "+ p + idx_cam);	        					
							main_acti.port_camera = p;
							main_acti.idx_size_cam = idx_cam;
							main_acti.start_video();                        
						}
					}); 
				}
				else if(sss[0].matches("CAMERA_OFF") == true)
				{
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							Log.i("tcp","stop cam ");	 
							main_acti.stop_video();                          
						}
					}); 
				}
				else if(sss[0].matches("IMG_RATE") == true)
				{
					int c = Integer.parseInt(sss[1]);					
					if(main_acti.udp_thread_cam != null)
						main_acti.udp_thread_cam.set_compression_rate(c);
				}
				else if(sss[0].matches("SENSORS_ON") == true)
				{
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							int p = Integer.parseInt(sss[1]);
							Log.i("tcp","start sensors ");	
							main_acti.port_sensors = p;
							main_acti.start_sensors(true);                          
						}
					}); 
				}
				else if(sss[0].matches("SENSORS_OFF") == true)
				{
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							Log.i("tcp","stop sensors ");	 
							main_acti.stop_sensors();                         
						}
					}); 
				}
				else if(sss[0].matches("IOIO_ON") == true)
				{
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							int p = Integer.parseInt(sss[1]);
							byte inv = Byte.parseByte(sss[2]);
							Log.i("tcp","start ioio , inv:" + inv);	
							main_acti.port_IOIO = p;
							main_acti.start_IOIO(true, inv, true);                          
						}
					}); 
				}
				else if(sss[0].matches("IOIO_OFF") == true)
				{
					main_acti.runOnUiThread(new Runnable() {
						@Override
						public void run() 
						{
							Log.i("tcp","stop ioio ");	 
							main_acti.stop_IOIO();                      
						}
					}); 
				}
			}

			try {Thread.sleep(50);	}											//was 10 !!!!!!!!!!!!!!!!!!! 
			catch (InterruptedException e) {Log.e("tcp","sleep error ", e);	}
		}

		close_socket();
	}

	public synchronized void stop_thread()
	{
		STOP = true;
		//		close_socket();
		Log.i("tcp","stopping...");
	}	

	private void close_socket()
	{
		//		if(CLOSED_SOCKET == false)
		//		{
		//			CLOSED_SOCKET = true;
		try 
		{	
			if(out != null)out.close();
			if(input != null)input.close();
			the_socket.close();				//Close connection
		} 
		catch (IOException e) {
			Log.e("tcp","error close: ", e);
		}

		main_acti.runOnUiThread(new Runnable() {
			@Override
			public void run() 
			{
				if(main_acti.button_connect.isChecked()) main_acti.button_connect.setChecked(false);   
				main_acti.stop_all();
			}
		}); 
		Log.i("tcp","tcp client stopped ");
	}
	//	}
}
