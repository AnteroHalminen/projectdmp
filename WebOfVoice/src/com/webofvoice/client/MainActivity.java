package com.webofvoice.client;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	public static String SENDER_ID = "5613935475";
	public static String TAG = "MainActivity";
	private CloudRecorder cloudRecorder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		} else {
		    Log.v(TAG, "Already registered");
		}
		
		cloudRecorder = new CloudRecorder();

		final Button tangent = (Button) findViewById(R.id.tangentButton);
        tangent.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					cloudRecorder.startRecording(v.getContext());
					return true;
				case MotionEvent.ACTION_UP:
					cloudRecorder.stopRecording();
					return true;
				default:
					return false;
				}
			}
		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
