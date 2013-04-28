package carl.gui;

import java.util.Vector;

import carl.utils.File_RW;
import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class My_spinner_Class extends Spinner 
{
	Vector<String> list_items = new Vector<String>();
	String file_name;
	
	// constructors (each calls initialize)
	public My_spinner_Class(Context context) {
		super(context);
//		this.initialise();
	}
	public My_spinner_Class(Context context, AttributeSet attrs) {
		super(context, attrs);
//		this.initialise();
	}
	public My_spinner_Class(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		this.initialise();
	}
	
	public void set_file_name(String s)
	{
		file_name = s.toString();
		this.initialise();
	}

	// declare object to hold data values
	private ArrayAdapter<String> arrayAdapter;

	// add the selected item to the end of the list
	public void addItem(String item) 
	{
		if(item.length() >0 && arrayAdapter.getPosition(item)== -1)
		{
			arrayAdapter.add(item);
			this.setEnabled(true);
			this.setSelection(arrayAdapter.getCount());
			File_RW.write_file(getContext(), file_name, item, true);
		}
		else
		{
			//			Toast.makeText(this.getContext(), "IP address already saved", Toast.LENGTH_LONG).show();
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(this.getContext()).create();
			alertDialog.setTitle("Error");
			if(item.length() ==0) alertDialog.setMessage("null value!  \n\n (press back to return)");
			else alertDialog.setMessage("Value already exists!  \n\n (press back to return)");
			alertDialog.show();
		}
	}
	
	public boolean remove_item()
	{
		boolean b=true;
		String s = getSelected();
		int size = arrayAdapter.getCount();
		if(s != null)
		{
			
			arrayAdapter.remove(s);
			size--;
			File_RW.reset_file(getContext(), file_name);

			for(int i=0; i<size; i++)
			{
				File_RW.write_file(getContext(), file_name, arrayAdapter.getItem(i), true);
			}
		}
		if(size == 0)
		{
			this.setEnabled(false);
			b = false;
		}
		return b;
	}

	// return the current selected item
	public String getSelected() 
	{
		if (this.getCount() > 0) return arrayAdapter.getItem(super.getSelectedItemPosition());
		else return null;
	}
	
	// remove all items from the list and disable it
	public void clearItems() {
		arrayAdapter.clear();
		this.setEnabled(false);
	}

	// internal routine to set up the array adapter, bind it to the spinner and disable it as it is empty
	private void initialise() 
	{
		arrayAdapter = new ArrayAdapter<String>(super.getContext(), android.R.layout.simple_spinner_item);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.setAdapter(arrayAdapter);
		
		list_items = File_RW.read_file(getContext(), file_name);
		int size = list_items.size();
		for(int i=0; i<size; i++)
		{
			arrayAdapter.add(list_items.elementAt(i));
			this.setEnabled(true);
			this.setSelection(arrayAdapter.getCount());
		}
		 if(size ==0) this.setEnabled(false);
	}
}
