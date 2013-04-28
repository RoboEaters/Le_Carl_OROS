/*******************************************************************************************************
Copyright (c) 2011 Regents of the University of California.
All rights reserved.

This software was developed at the University of California, Irvine.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. All advertising materials mentioning features or use of this
   software must display the following acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

4. The name of the University may not be used to endorse or promote
   products derived from this software without specific prior written
   permission.

5. Redistributions of any form whatsoever must retain the following
   acknowledgment:
   "This product includes software developed at the University of
   California, Irvine by Nicolas Oros, Ph.D.
   (http://www.cogsci.uci.edu/~noros/)."

THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
IN NO EVENT SHALL THE UNIVERSITY OR THE PROGRAM CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/
package carl.threads;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import carl.inputs.Sensors_listener;
import android.util.Log;

public class Sensors_udp_thread extends Thread 
{
	final String tag = "NN_udp";
	
	Sensors_listener the_sensors;
	
	/***************************************************************  Networking   ***************************************************************/
	InetAddress serverAddr;
	DatagramSocket socket;	
	int size_p;
	boolean STOP = false;
	int port_UDP;
	String ip_address;
	
	/***************************************************************  values   ***************************************************************/
	float[] acceleration;
	float[] orientation;
	float ix_O, iy_O, iz_O, ix_A, iy_A, iz_A;
	

	public Sensors_udp_thread(Sensors_listener sen, String ip, int port)
	{
		the_sensors = sen;
	 	ip_address = ip;
    	port_UDP = port;    	
	}

	@Override
	public final void run() 
	{
    	try
    	{
    		serverAddr = InetAddress.getByName(ip_address);
    		socket = new DatagramSocket();
    	}
    	catch (Exception exception){ Log.e(tag, "Error constructor: ", exception);}
    	
		while(STOP == false)
		{	
			if(the_sensors != null)
			{
				orientation = the_sensors.get_orientation();
				acceleration = the_sensors.get_acceleration();	
			
				if(orientation != null)
				{
					ix_O = (orientation[0]);
					iy_O = (orientation[1]);
					iz_O = (orientation[2]);
				}
				
				if(acceleration != null)
				{
					ix_A = (acceleration[0]);
					iy_A = (acceleration[1]);
					iz_A = (acceleration[2]);
				}
			}
						
			String s = "/COMP_X/" + Float.toString(ix_O) + "/COMP_Y/" + Float.toString(iy_O) + "/COMP_Z/" + Float.toString(iz_O) +
					"/ACC_X/" + Float.toString(ix_A) + "/ACC_Y/" + Float.toString(iy_A) + "/ACC_Z/" + Float.toString(iz_A) + "\n";
			
			byte[] data = s.getBytes();
			
			try 
			{			
				size_p = data.length;
				DatagramPacket packet = new DatagramPacket(data, size_p, serverAddr, port_UDP);
				socket.send(packet);
//				Log.e(tag, "packet sent: ");
			} catch (Exception e) 
			{	
				Log.e(tag, "Error send: ", e);
			}
			
			try {
				Thread.sleep(50);												// was 100
			} catch (InterruptedException e) {Log.e(tag, "Error: ", e);}
		}
		socket.close();
	}

	public synchronized void stop_thread()
	{
		STOP = true;		
	}
}

