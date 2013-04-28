package carl.utils;

public class Maths_utils 
{
	public static float sigmoid(float input, float gain, float threshold, float zero)
	{
		float s = gain*(threshold - input);
		s = 1/(1+ (float)Math.exp(s));			
		
		if(s<zero) s = 0f;
		return s;
	}
	
	// Gaussian 2D calculated as in: Wikipedia Gaussian function :-)
	public static float gaussian_2D(float x, float y, float a, float x_mean, float y_mean, float x_sd, float y_sd)
	{
		float result;
		float xx = x-x_mean;
		float yy = y-y_mean;
		float x_spread = x_sd;
		float y_spread = y_sd;
		float A = a;

		xx *= xx;
		yy *= yy;
		x_spread = 2*(x_spread*x_spread);
		y_spread = 2*(y_spread*y_spread);
		
		xx = xx/x_spread;
		yy = yy/y_spread;
		xx = xx + yy;
		
		result = (float)(A*Math.exp(-xx));
		
		return result;		
	}	
}
