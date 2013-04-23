package com.webofvoice.client;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class CloudRecorder {
	private static final String TAG = "CloudRecorder";
	private MediaRecorder mediaRecorder = null;
	private File outputFile = null;
	private WebConnector webConnector = null;
	
	public CloudRecorder() {
	}
	
	public void startRecording() {
		
		if (null != outputFile) {
			Log.e(TAG, "spurious call to startRecording before stopRecording");
			return;
		}
		outputFile = TempFileHelper.getInstance().makeTempFile("wovRec");
		if (null == outputFile) {
			return;
		}
		
		String outputFileName = outputFile.getPath();
		Log.v(TAG, "recording to " + outputFileName);

		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(outputFileName);

		try {
			mediaRecorder.prepare();
			mediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            outputFile.delete();
        }
	}

	public void stopRecording() {
		Log.v(TAG, "recording complete");

		if (null != mediaRecorder) {
			try {
				mediaRecorder.stop();
			} catch(RuntimeException e) {
				Log.e(TAG, "MediaRecorder.stop() failed!");
			} finally {
				mediaRecorder.reset();
				mediaRecorder.release();
				mediaRecorder = null;
			}
		}

		if (null != outputFile) {

			// TODO: Send the file @ outputFileName to cloud or wherever
			if (webConnector == null) webConnector = WebConnector.getInstance();

			// hand the file over to webConnector that will delete the file when done
			webConnector.uploadFile(outputFile);

			outputFile = null;
		}
	}
}
