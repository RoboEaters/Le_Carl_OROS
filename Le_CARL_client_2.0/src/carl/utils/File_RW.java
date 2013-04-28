package carl.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Vector;
import android.content.Context;
import android.widget.Toast;

public class File_RW 
{
	static FileOutputStream fos = null;
	static FileInputStream fIn = null;		 
	static InputStreamReader isr = null;
	static OutputStreamWriter orw = null;
	

	public File_RW(){}

	public static void write_file(Context context, String file, String s, boolean BOOL_APPEND)
	{
		try 
		{
			if(BOOL_APPEND == true)fos = context.openFileOutput(file, Context.MODE_APPEND);
			else fos = context.openFileOutput(file, Context.MODE_PRIVATE);
			
			orw = new OutputStreamWriter(fos);
			BufferedWriter bf = new BufferedWriter(orw);
			bf.write(s);
			bf.newLine();
			bf.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, "pb write file 2",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	public static void reset_file(Context context, String file)
	{
		try {
			context.openFileOutput(file, Context.MODE_PRIVATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Vector<String> read_file(Context context, String file)
	{
		String str="";
		Vector<String> vs = new Vector<String>();
		try
		{			
			fIn = context.openFileInput(file);    
			if (fIn!=null) 
			{
				isr = new InputStreamReader(fIn);	
				BufferedReader reader = new BufferedReader(isr);
				
				while ((str = reader.readLine()) != null) 
				{	
					vs.add(str);
				}
				isr.close();
				fIn.close();
			}		
		}catch (IOException e) 
		{      
			e.printStackTrace();
			Toast.makeText(context, "pb read file",Toast.LENGTH_SHORT).show();
		}
		
		return vs;
	}
}
