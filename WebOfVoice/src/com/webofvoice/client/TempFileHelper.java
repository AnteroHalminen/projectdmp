package com.webofvoice.client;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class TempFileHelper {
	public static final String TAG = "TempFileHelper";
	
	private static TempFileHelper instance;
	private File cacheDir = null;

	public synchronized static TempFileHelper getInstance() {
		if (instance == null) instance = new TempFileHelper();
		return instance;
	}
	
	public void setContext(Context context) {
		cacheDir = context.getCacheDir();
	}
	
	public File makeTempFile(String prefix) {
		if (cacheDir == null) {
			Log.e(TAG, "Please set context for reading cache dir");
			return null;
		}

		if (prefix.isEmpty()) prefix = "wovClient";

		File temp = null; 
		try {
			temp = File.createTempFile(prefix, ".3gp", cacheDir);
		} catch (IOException e) {
			Log.e(TAG, "failed to create temporary file for sample (in " + cacheDir.getPath() + ")");
			return null;
		}
		return temp;
	}
}
