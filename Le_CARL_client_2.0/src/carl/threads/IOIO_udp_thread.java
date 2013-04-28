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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import carl.inputs.IOIO_values;

import android.util.Log;

public class IOIO_udp_thread extends Thread 
{
	final String tag = "IOIO_UDP";
	InetAddress serverAddr;
	DatagramSocket socket;	
	String ip_address;
	int port_ioio;

	IOIO_thread ioio_thread;
	boolean STOP, RC;
	
	IOIO_values ioio_values;
	
	int serv_val, moto_val;
//	float IR_front_val, IR_left_val, IR_right_val, IR_back_val;
	short sIR_front_val, sIR_left_val, sIR_right_val, sIR_back_val;
	int size_p;

	public IOIO_udp_thread(IOIO_thread ioio_t, String ip, int port, boolean rc_mode)
	{
		ioio_thread = ioio_t;
		ip_address = ip;
		port_ioio = port;
		STOP = false;
		RC = rc_mode;
	}

	@Override
	public final void run() 
	{
		try
		{ 
			serverAddr = InetAddress.getByName(ip_address);
			socket = new DatagramSocket();
			socket.setSoTimeout(100);		//set timeout 100 ms to read/receive
		}
		catch (Exception exception) 
		{
			Log.e(tag, "Error: ", exception);
		}
		
		while(STOP == false)
		{
			ioio_values = ioio_thread.get_ioio_vals();
			
			sIR_front_val = (short)(10000 * ioio_values.IR_front_left);
			sIR_left_val = (short) (10000 * ioio_values.IR_left);
			sIR_right_val = (short)(10000 * ioio_values.IR_right);
			sIR_back_val = (short) (10000 * ioio_values.IR_front_right);        	

			byte[] data = new byte[8];
			data[0] = (byte) (sIR_front_val >> 8);
			data[1] = (byte) sIR_front_val;    			
			data[2] = (byte) (sIR_left_val >> 8);
			data[3] = (byte) sIR_left_val;    			
			data[4] = (byte) (sIR_right_val >> 8);
			data[5] = (byte) sIR_right_val;    			
			data[6] = (byte) (sIR_back_val >> 8);
			data[7] = (byte) sIR_back_val;    

//			Log.i("IOIO_udp thread","IR " + sIR_front_val + " "+ sIR_left_val + " "+ sIR_right_val + " "+ sIR_back_val);

			size_p = data.length;
			DatagramPacket packet = new DatagramPacket(data, size_p, serverAddr, port_ioio);
			try 
			{
				socket.send(packet); 											
			} 
			catch (IOException e) {Log.e("IOIO_udp thread","error sending: ", e);}	

			
			if(RC == true)		//get pwm commands from server and set to IOIO
			{
				byte[] data4 = new byte[5];
				DatagramPacket receivePacket2 = new DatagramPacket(data4, data4.length);
				try
				{
					socket.receive(receivePacket2);	
					byte[] data5 = receivePacket2.getData();
//					AUTO = data5[0]; 				
//					if(AUTO == 0)
//					{
						serv_val = (int) ((data5[1] & 0xff) << 8 | (data5[2] & 0xff)); 
						moto_val = (int) ((data5[3] & 0xff) << 8 | (data5[4] & 0xff));
//						ioio_thread.set_PWM_values(serv_val, moto_val);
						ioio_values.set_pwm(moto_val, serv_val);
//					}
				}
				catch (Exception e) {}
			}
//			else
//			{
				try {
					Thread.sleep(20);				// was 50
				} catch (InterruptedException e) {Log.e(tag, "Error: ", e);}
//			}
		}
		socket.close();
	}

	public synchronized void stop_thread()
	{
		STOP = true;
	}
}
