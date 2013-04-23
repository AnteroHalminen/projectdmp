package com.webofvoice.client;

import java.io.File;
import java.io.IOException;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

public class CloudPlayer {
	
	private MediaPlayer mediaPlayer;
	
	public class BroadcastMessage {
		private static final String KEY_MESSAGE_ID = "message_id";
		private static final String KEY_DOWNLOAD_URL = "download_url";
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
	}

	private static CloudPlayer instance;
	
	public static synchronized CloudPlayer getInstance() {
		if (instance == null) instance = new CloudPlayer();
		return instance;
	}
	
	public void play(BroadcastMessage message) {
		
		message.file = TempFileHelper.getInstance().makeTempFile("wovPlay");
		WebConnector.getInstance().downloadFile(message);
	}

	// handle sample download completion
	public void onSampleReady(BroadcastMessage message) {
        try {
			mediaPlayer.setDataSource(message.file.getAbsolutePath());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mediaPlayer.start();
	}
}
