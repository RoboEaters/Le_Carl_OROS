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
package carl.inputs;

import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;

public class Camera_feedback
{
	private static final String TAG = "IP_cam";	
	Camera mCamera;
	public SurfaceView parent_context;
	List<Size> mSupportedPreviewSizes;
	Size mPreviewSize ;
	int width_screen, height_screen;
	int idx_selected_size;
	public Bitmap mBitmap;
	public byte[] data_image;	
	
	SurfaceTexture dummy_surface;
		
	public Camera_feedback(SurfaceView context,int idx_size)
	{
		parent_context = context;	
		idx_selected_size = idx_size;			// index used to set preview size
		Context ctx = parent_context.getContext();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		width_screen = display.getWidth();
		height_screen = display.getHeight();
		
		try 
		{			 
			mCamera = Camera.open();      
			
			dummy_surface = new SurfaceTexture(1);
			
			Camera.Parameters parameters = mCamera.getParameters(); 
			mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();	
			mPreviewSize = mSupportedPreviewSizes.get(idx_selected_size);
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			parameters.setPreviewFrameRate(30);
			parameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
//			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//			parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			mCamera.setParameters(parameters);
			
	        try {
	            mCamera.setPreviewTexture(dummy_surface);
	        } catch (IOException t) {    	}

	        mCamera.setPreviewCallback(new cam_PreviewCallback());   
	        mCamera.startPreview();
		} 
		catch (Exception exception) 
		{
			Log.e(TAG, "Error: ", exception);
		}
	}
    
    public synchronized void stop_camera()
    {
    	if(mCamera != null)
    	{
    		mCamera.setPreviewCallback(null);
    		mCamera.stopPreview();
    		mCamera.release();
    		mCamera = null;
    	}
    }

	// Preview callback used whenever new frame is available...send image via UDP !!!
	private class cam_PreviewCallback implements PreviewCallback 
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{		
			set_data(data);
			if (mBitmap == null)
			{
				mBitmap = Bitmap.createBitmap(camera.getParameters().getPreviewSize().width, 
						camera.getParameters().getPreviewSize().height, Bitmap.Config.ARGB_8888);
			}
		}
	}
	
	public synchronized void set_data(byte[] d)
    {
		data_image = d;
    }
	
	public synchronized byte[] get_data()
    {
		return data_image.clone();
    }	
}
