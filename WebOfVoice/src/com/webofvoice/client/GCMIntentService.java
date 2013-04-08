package com.webofvoice.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
	protected void onMessage(Context arg0, Intent arg1) {
		Log.v(TAG, arg0 + ", " + arg1);
	}

    @Override
	protected void onError(Context arg0, String arg1) {
		Log.v(TAG, arg0 + ", " + arg1);
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.v(TAG, arg0 + ", " + arg1);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.v(TAG, arg0 + ", " + arg1);
	}
}
