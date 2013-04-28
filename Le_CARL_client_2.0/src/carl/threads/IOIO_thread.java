package carl.threads;

import carl.gui.Main_activity;
import carl.inputs.IOIO_values;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

public class IOIO_thread extends BaseIOIOLooper 
{
	PwmOutput motor_output, servo_output;
	AnalogInput IR_left, IR_right, IR_front_left, IR_front_right;
	Main_activity the_gui;					// reference to the main activity
	int pwm_servo, pwm_motor, pwm_M, pwm_S;
	float right, left, front_left, front_right;
	byte INVERTED;	
	IOIO_values ioio_vals;

	public IOIO_thread(Main_activity gui)
	{
		the_gui = gui;
		INVERTED = 0;		
		ioio_vals = new IOIO_values();
	}

	@Override
	public void setup() throws ConnectionLostException 
	{
		try 
		{
			motor_output = ioio_.openPwmOutput(5, 100);	
			servo_output = ioio_.openPwmOutput(10, 100);
			IR_left = ioio_.openAnalogInput(44);
			IR_front_left = ioio_.openAnalogInput(42);
			IR_front_right = ioio_.openAnalogInput(40);
			IR_right = ioio_.openAnalogInput(38);			
		} 
		catch (ConnectionLostException e){throw e;}
	}

	@Override
	public void loop() throws ConnectionLostException 
	{
		try 
		{		
			left = IR_left.read();
			right = IR_right.read();
			front_left = IR_front_left.read();
			front_right = IR_front_right.read();
			ioio_vals.set_IR(right, left, front_right, front_left);

//			the_gui.set_IR(left, right, front_right, front_left);
			
			pwm_M = ioio_vals.get_pwm_motor();
			pwm_S = ioio_vals.get_pwm_servo();

			if(INVERTED == 1)
			{
				pwm_M = 1500 - (pwm_M-1500);
				pwm_S = 1500 - (pwm_S-1500);
			}

//						Log.i("IOIO", "pwm servo: " + pwm_S +  "pwm motor: " +pwm_M + ", inv: " + INVERTED);
			motor_output.setPulseWidth(pwm_M);
			servo_output.setPulseWidth(pwm_S);
			
			Thread.sleep(20);
		} catch (InterruptedException e) {
			ioio_.disconnect();
		} catch (ConnectionLostException e) {
			throw e;
		}
	}
	
	public synchronized void set_inverted(byte inv)
	{
		INVERTED = inv;
	}
	
	public synchronized IOIO_values get_ioio_vals()
	{
		return ioio_vals;
	}
}
