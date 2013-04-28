package carl.threads;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;
import carl.inputs.Camera_feedback;

public class Cam_udp_thread extends Thread 
{
	private static final String TAG = "Cam_udp";
	boolean STOP = false;
	Camera_feedback the_cam;
	Bitmap the_image;
	byte[] data;
	boolean NEW_IMAGE=true;

	/***************************************************************  Networking   ***************************************************************/
	InetAddress serverAddr;
	DatagramSocket socket;		
	String ip_address;
	int port_cam;	
	public static int HEADER_SIZE = 5;
	public static int DATAGRAM_MAX_SIZE = 1450 - HEADER_SIZE;	

	/***************************************************************  image processing ***************************************************************/
	ByteArrayOutputStream byteStream,bos;
	int[] mRGBData;
	byte frame_nb = 0;
	int size_packet_sent = 0;	
	int width_ima, height_ima,size_p=0,packetCount,nb_packets,size;
	byte[] picData,data2;
	int compression_rate;
	Mat m,dest;							//openCV image

	public Cam_udp_thread(Camera_feedback cam, String ip, int p)
	{
		the_cam = cam;
		ip_address = ip;
		port_cam = p;
		byteStream = new ByteArrayOutputStream();
		compression_rate = 75;					// default jpeg compression rate
	}

	public synchronized void stop_thread()
	{
		STOP = true;
	}

	public synchronized void set_compression_rate(int nb)
	{
		compression_rate = nb;
	}

	@Override
	public final void run() 
	{
		try
		{
			serverAddr = InetAddress.getByName(ip_address);
			socket = new DatagramSocket();
		}
		catch (Exception exception) 
		{
			Log.e(TAG, "Error: ", exception);
		}

		while(STOP == false)
		{
			if(NEW_IMAGE == true)
			{
				the_image = the_cam.mBitmap;
				if(the_image != null)
				{
					NEW_IMAGE = false;
					m = new Mat(the_image.getHeight() + the_image.getHeight() / 2, the_image.getWidth(), CvType.CV_8UC1);
					dest = new Mat();					
				}	
			}

			if(m != null)
			{		
				data = the_cam.get_data();
				m.put(0, 0, data);
				Imgproc.cvtColor(m, dest, Imgproc.COLOR_YUV420sp2RGB,4);
				Utils.matToBitmap(dest, the_image);
				stream_UDP(the_image);
			}

			try {
				Thread.sleep(10);										// was 10 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			} catch (InterruptedException e) {Log.e(TAG, "Error: ", e);}
		}
		socket.close();
	}

	private void stream_UDP(Bitmap image)
	{
		if(image != null)
		{    	 
			byteStream.reset();
			image.compress(Bitmap.CompressFormat.JPEG, compression_rate, byteStream);	// !!!!!!!  change compression rate to change packets size

			picData = byteStream.toByteArray();

			//Number of packets used for this bitmap UNCOMPRESSED
			nb_packets = (int)Math.ceil(picData.length / (float)DATAGRAM_MAX_SIZE);
			size = DATAGRAM_MAX_SIZE;

			/* Loop through slices of the bitmap*/
			for(packetCount = 0; packetCount < nb_packets; packetCount++)
			{
				//If we are on the last packet... or we only need one packet
				if(packetCount == nb_packets-1)
				{
					//Set the size of this packet to be whatever we have not used up in the previous packets
					size = picData.length - packetCount * DATAGRAM_MAX_SIZE;
				}

				/* Set additional header */
				data2 = new byte[HEADER_SIZE + size];
				data2[0] = (byte)frame_nb;
				data2[1] = (byte)nb_packets;
				data2[2] = (byte)packetCount;
				data2[3] = (byte)(size >> 8);
				data2[4] = (byte)size;

				/* Copy current slice to byte array */
				System.arraycopy(picData, packetCount * DATAGRAM_MAX_SIZE, data2, HEADER_SIZE, size);		

				try 
				{			
					size_p = data2.length;
					DatagramPacket packet = new DatagramPacket(data2, size_p, serverAddr, port_cam);
					socket.send(packet);
				}catch (Exception e) {	Log.e(TAG, "Error: ", e);}	
			}	
			frame_nb++;

			if(frame_nb == 127)frame_nb=0;
		}
	}   
}
