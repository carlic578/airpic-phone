/**
 * Copyright (c) 2007, Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//original http://labs.makemachine.net/2010/03/simple-android-photo-capture/

package com.android.airpic;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Airpic extends Activity
{
    private static final String TAG = "Airpic: ";
    protected Button _photoButton;
    protected Button _uploadButton;
    protected ImageView _image;
    protected TextView _field;
    protected String _path;
    protected String _file;
    protected boolean _taken;
    protected boolean viewingSettings=false;
    protected boolean uploadingPhoto=false;

    //File info
    String currentDateTimeString;
    Date currentDate;

    protected static final String PHOTO_TAKEN   = "photo_taken";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera);
		Log.i(TAG, "setContent");
        _image = ( ImageView ) findViewById( R.id.image );  //displays image in program
        _field = ( TextView ) findViewById( R.id.field );  //displays No photo take text
        _photoButton = ( Button ) findViewById( R.id.button );  //this is the photo button
        _photoButton.setOnClickListener( new PhotoButtonClickHandler() ); //event handler for button
        _uploadButton = ( Button ) findViewById( R.id.button2 ); //this is the upload button
        _uploadButton.setOnClickListener( new UploadButtonClickHandler() ); //event handler for button
        Log.i(TAG, "Created UI");

        //set path to store button
        _file = String.format("%s%s%s", Environment.getExternalStorageDirectory(), getResources().getText(R.string.pathForPicture), getResources().getText(R.string.nameForPicture));
        Log.i(TAG,"===========================");
        Log.i(TAG, _file.toString());
        Log.i(TAG,"===========================");

        _path = String.format("%s%s", Environment.getExternalStorageDirectory(), getResources().getText(R.string.pathForPicture));
       File picFolder = new File(_path);
       if(!picFolder.exists())
       {
    	   if(picFolder.mkdir())
    	   {
    		   Log.i(TAG,"****************************");
    		   Log.i(TAG,"Images Folder created");
    		   Log.i(TAG,"****************************");
    	   }
    	   else
    	   {
    		   Log.e(TAG,"****************************");
    		   Log.e(TAG,"Folder creation failed!!!!!");
    		   Log.e(TAG,"****************************");
    	   }
       }
    }

    //Abstract class that is an event handler for upload button
    public class UploadButtonClickHandler implements View.OnClickListener
    {
    	public void onClick( View view )
    	{
    		Log.i(TAG, "!!!!!!!!!!!!!!!!!!!");
    		Log.i(TAG, "Upload clicked");
    		Log.i(TAG, "!!!!!!!!!!!!!!!!!!!");
    		try
    		{
				FileServer.upload();
			}
    		catch (IOException e)
    		{
				e.printStackTrace();
			}

    	}
    }

    //Abstract class that is an event handler for photo button
    public class PhotoButtonClickHandler implements View.OnClickListener
    {
    	//what to do when button is clicked
        public void onClick( View view ){
            startCameraActivity();  //starts the camera activity
        }
    }

    //Starts camera intent
    protected void startCameraActivity()
    {
        File file = new File( _file );  //creates a file path from _file string
        Uri outputFileUri = Uri.fromFile( file ); //creates uri for file

        //Start intent
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );//output file here

        //Start Activity and wait for result
        startActivityForResult( intent, 1 ); //activity for result is used b/c we need to know when the camera exits
    }

    @Override  //abstract function which runs with result of activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Log.i(TAG, "==================================");
        Log.i( "Airpic", "resultCode: " + resultCode );
        Log.i(TAG, "==================================");
        switch( resultCode )
        {
            case 0:  //program exited without returning image
            	Log.i(TAG, "==================================");
                Log.i( "Airpic", "User cancelled" );
                Log.i(TAG, "==================================");
                break;

            default:
                onPhotoTaken();
                break;
        }
    }

    protected void onPhotoTaken()
    {
        _taken = true;  //set that a photo has been taken

        //setup some options to display image in program of pic we just took
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2; //reduce size of image

        //Decode image and set it to the image display
        Bitmap bitmap = BitmapFactory.decodeFile( _file, options );
        _image.setImageBitmap(bitmap);

      //get rid of _field so it doesn't take any layout space
        _field.setVisibility( View.GONE );
    }

    @Override  //save instance state during rotation
    protected void onSaveInstanceState( Bundle outState )
    {
        outState.putBoolean( Airpic.PHOTO_TAKEN, _taken );
    }

    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState)
    {
    	Log.i(TAG, "=============================");
        Log.i( "Airpic", "onRestoreInstanceState()");
    	Log.i(TAG, "=============================");
        if( savedInstanceState.getBoolean( Airpic.PHOTO_TAKEN ) )
        {
            onPhotoTaken();
        }
    }

    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuItem settings = menu.add(R.string.list_menu_settings);
    	settings.setIcon(android.R.drawable.ic_menu_preferences);
    	settings.setOnMenuItemClickListener(new OnMenuItemClickListener()
	    	{
	    		public boolean onMenuItemClick(MenuItem item)
	    		{
	    			Airpic.this.startActivity(new Intent(Airpic.this, SettingsActivity.class));
	    			return true;
				}
			}

    	);

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(viewingSettings)
        	{
        		viewingSettings=false;
        		setContentView(R.layout.camera);
        	}
        	else
        		this.finish();

        }
        return false;
    }


    /*
    Camera mCamera; //creates a camera instance for us
    boolean mPreviewRunning = false; //keeps track of if camera is running

    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);

		setContentView(R.layout.camera_api_test);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface);

		mSurfaceHolder = mSurfaceView.getHolder();
    	mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.addCallback(this);
    }


    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        MenuItem item = menu.add(0, 0, 0, "goto gallery");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Uri target = Uri.parse("content://media/external/images/media");
                Intent intent = new Intent(Intent.ACTION_VIEW, target);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera c) {
            Log.i(TAG, "PICTURE CALLBACK: data.length = " + data.length);
            mCamera.startPreview();
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }

        if (keyCode == KeyEvent.KEYCODE_CAMERA)
        {
        	Log.i(TAG, "====================");
        	Log.i(TAG, "Camera button pressed and picture callback called");
        	Log.i(TAG, "====================");
            return true;
        }

        return false;
    }

    protected void onResume()
    {
    	Log.i(TAG, "====================");
        Log.e(TAG, "onResume");
        Log.i(TAG, "====================");
        super.onResume();
    }

    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    protected void onStop()
    {
    	Log.i(TAG, "====================");
        Log.e(TAG, "onStop");
        Log.i(TAG, "====================");
        super.onStop();
    }


    public void surfaceCreated(SurfaceHolder holder)
    {
    	Log.i(TAG, "====================");
        Log.e(TAG, "surfaceCreated");
        Log.i(TAG, "====================");
        mCamera = Camera.open();
        mCamera.startPreview();
        mPreviewRunning = true;
    }


	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
    	Log.i(TAG, "====================");
        Log.e(TAG, "surfaceChanged");
        Log.i(TAG, "====================");

        // stopPreview() will crash if preview is not running
        if (mPreviewRunning)
        {
            mCamera.stopPreview();
        }

        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(w, h);
        mCamera.setParameters(p);
        try
        {
        	mCamera.setPreviewDisplay(holder);
        }
        catch(IOException m)
        {

        };
        mCamera.startPreview();
        mPreviewRunning = true;
    }


    public void surfaceDestroyed(SurfaceHolder holder)
    {
    	Log.i(TAG, "====================");
        Log.e(TAG, "surfaceDestroyed");
        Log.i(TAG, "====================");
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
  */
    private static final int i=0;//unused var to let me fold commented code above
}


