package com.webofvoice.client;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.util.Log;

public class CloudPlayer {
	
	private MediaPlayer mediaPlayer;
	public static final String TAG = "CloudPlayer";
	
	public class BroadcastMessage {
		private static final String KEY_MESSAGE_ID = "message_id";
		private static final String KEY_DOWNLOAD_URL = "sample_url";
		private static final String KEY_CHANNEL_NAME = "channel_name";
		private static final String KEY_LATITUDE = "latitude";
		private static final String KEY_LONGITUDE = "longitude";
		
		public String messageId;
		public String url;
		public double latitude;
		public double longitude;
		public String channelName;
		public File file;
		
		public BroadcastMessage(Bundle gcmExtras) {
			
			
			Set<String> keys = gcmExtras.keySet();
			Iterator<String> keyIterator = keys.iterator();
			while (keyIterator.hasNext()) {
				Log.v(TAG, keyIterator.next());
			}
			
			
			if (gcmExtras.containsKey(KEY_MESSAGE_ID)) {
				messageId = gcmExtras.getString(KEY_MESSAGE_ID);
			}
			if (gcmExtras.containsKey(KEY_DOWNLOAD_URL)) {
				url = gcmExtras.getString(KEY_DOWNLOAD_URL);
			}
			if (gcmExtras.containsKey(KEY_CHANNEL_NAME)) {
				channelName = gcmExtras.getString(KEY_CHANNEL_NAME);
			}
			if (gcmExtras.containsKey(KEY_LATITUDE)) {
				latitude = gcmExtras.getDouble(KEY_LATITUDE);
			}
			if (gcmExtras.containsKey(KEY_LONGITUDE)) {
				longitude = gcmExtras.getDouble(KEY_LONGITUDE);
			}
		}
	}
	
	private CloudPlayer() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.e(TAG, "Finished playing");
		        mp.stop();
			}
		});
		mediaPlayer.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "MediaPlayer error");
				mp.stop();
				mp.reset();
				// TODO Auto-generated method stub
				return false;
			}
		});
	}

	private static CloudPlayer instance;
	
	public static synchronized CloudPlayer getInstance() {
		if (instance == null) instance = new CloudPlayer();
		return instance;
	}
	
	public void play(BroadcastMessage message) {
		
		if (message.url == null || message.url.isEmpty()) {
			return;
		} else {
			message.file = TempFileHelper.getInstance().makeTempFile("wovPlay");
			WebConnector.getInstance().downloadFile(message);
		}
	}

	// handle sample download completion
	public void onSampleReady(BroadcastMessage message) {
        try {
        	if (mediaPlayer.isLooping()) {
        		Log.e(TAG, "loopin' around");
        	}
        	if (mediaPlayer.isPlaying()) {
        		Log.e(TAG, "playin' around");
        	}
        	mediaPlayer.stop();
        	mediaPlayer.reset();
			mediaPlayer.setDataSource(message.file.getAbsolutePath());
		} catch (IllegalArgumentException e) {
			Log.e(TAG, e.toString());
		} catch (SecurityException e) {
			Log.e(TAG, e.toString());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
        try {
    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
        mediaPlayer.start();
        mediaPlayer.seekTo(0);
	}
}
