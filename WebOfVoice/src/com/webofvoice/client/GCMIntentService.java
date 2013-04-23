package com.webofvoice.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	public static String TAG = "GCMIntentService";

	public GCMIntentService() {
        Log.v(TAG, "emtpy ctor");
    }

    public GCMIntentService(String senderId) {
        super(senderId);
        Log.v(TAG, senderId);
    }

	@Override
	protected void onMessage(Context ctx, Intent intent) {
		Log.v(TAG, "onMessage: " + ctx + ", " + intent);
		Bundle extras = intent.getExtras();
		if (extras.containsKey("message_id")) {
			Log.v(TAG, "WOOHOO - got notified of broadcast message " + extras.getString("message_id"));
		}
	}

    @Override
	protected void onError(Context arg0, String arg1) {
		Log.v(TAG, "onError: " + arg0 + ", " + arg1);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.v(TAG, "onRegistered: " + arg0 + ", " + arg1);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.v(TAG, "onUnregistered: " + arg0 + ", " + arg1);
	}
}
