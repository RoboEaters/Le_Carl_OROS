package carl.inputs;

import android.util.Log;

public class IOIO_values 
{
	public float IR_right, IR_left, IR_front_left, IR_front_right;
	public int pwm_servo, pwm_motor;
	static int DEFAULT_PWM = 1500;
	boolean PWM_MOTOR_SET, PWM_SERVO_SET, CLOSED_IOIO;

	public IOIO_values()
	{
		IR_right = 0f;
		IR_left = 0f;
		IR_front_left = 0f;
		IR_front_right = 0f;
		pwm_motor = DEFAULT_PWM;
		pwm_servo = DEFAULT_PWM;	
		PWM_MOTOR_SET = false;
		PWM_SERVO_SET = false;
		CLOSED_IOIO = false;
	}

	public synchronized void set_IR(float r, float l, float fr, float fl)
	{
		IR_right = r;
		IR_left = l;
		IR_front_left = fl;
		IR_front_right = fr;
	}

	public synchronized void set_pwm(int m, int s)
	{	
		if(CLOSED_IOIO == false)
		{
			if(PWM_MOTOR_SET || PWM_SERVO_SET)
			{
				try {wait();} 
				catch(InterruptedException e) {Log.e("IOIO vals","error set", e);}
			}
			pwm_motor = m;
			pwm_servo = s;	
			PWM_MOTOR_SET = true;
			PWM_SERVO_SET = true;
			notify();					//awake GET thread
		}
	}

	public synchronized int get_pwm_motor()
	{
		if(CLOSED_IOIO == false)
		{
			if(!PWM_MOTOR_SET)
			{
				try {wait();} 
				catch(InterruptedException e) {Log.e("IOIO vals","error get, CLOSED_IOIO: " + CLOSED_IOIO, e);}
			}			
			PWM_MOTOR_SET = false;
			awake_SET_thread();
		}
		else pwm_motor = DEFAULT_PWM;
		
		return pwm_motor;
	}

	public synchronized int get_pwm_servo()
	{	
		if(CLOSED_IOIO == false)
		{
			if(!PWM_SERVO_SET)
			{
				try {wait();} 
				catch(InterruptedException e) {Log.e("IOIO vals","error get, CLOSED_IOIO: " + CLOSED_IOIO, e);}
			}			
			PWM_SERVO_SET = false;
			awake_SET_thread();
		}
		else pwm_servo = DEFAULT_PWM;
		
		return pwm_servo;
	}

	private void awake_SET_thread()
	{
		if(!PWM_MOTOR_SET && !PWM_SERVO_SET)
			notify();
	}

	public synchronized void STOP()
	{
		CLOSED_IOIO = true;
		notifyAll();
	}
}
