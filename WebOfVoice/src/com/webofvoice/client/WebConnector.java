package com.webofvoice.client;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public class WebConnector {

	private enum SoapActions {
		UNSET,
		REGISTER_TX,
		UPDATE_LOCATION,
		TUNE_CHANNEL
	}

	public static final String TAG = "WebConnector";

	private static final String WSDL_TARGET_NAMESPACE = "com.webofvoice.soap.server";
	private static final String SOAP_ADDRESS = "http://www.webofvoice.com/soap/";
	private static final String SERVER_URL = "http://www.webofvoice.com";
	
	private String tranceiverID = "";
    
	private static WebConnector instance = null;

    private WebConnector() {
    }

    public static synchronized WebConnector getInstance() {
        if(instance == null) {
            instance = new WebConnector();
        }
        return instance;
    }

    private void setTranceiverID(String tranceiverID) {

    	// if the ID looks good (i.e. non-empty, set initial location and channel)
    	if (!tranceiverID.isEmpty()) {
    		this.tranceiverID = tranceiverID;

			// TODO: get location from location API
			//new WebServiceCaller().updateLocation(Double.valueOf(60.1833), Double.valueOf(24.8333));
			new WebServiceCaller().tuneTranceiver("WhiteNoise");
    	}
	}
	
	public void registerTranceiver(String gcmRegistrationID) {
		new WebServiceCaller().registerTranceiver(gcmRegistrationID, "oretna.nenimlah@gmail.com");
	}
	
	/**
	 * Upload a file for broadcast
	 * 
	 * TODO: upload to Drive when implemented
	 * 
	 * @param file
	 */
	public void uploadFile(File file) {
		// a valid tranceiver ID is required for broadcasting messages
		if (tranceiverID.isEmpty()) {
			Log.e(TAG, "Can't upload files without tranceiver ID");
			file.delete();
		} else {
			new FileUploader(file).execute();
		}
	}
	
	private void onMessageSent(String messageId) {
		Log.v(TAG, "Message broadcast: " + messageId);
	}

	private static PropertyInfo makeIntProperty(String name, int value) {
		PropertyInfo pi = new PropertyInfo();
		pi.setElementType(null);
		pi.setType(PropertyInfo.INTEGER_CLASS);
		pi.setNamespace(WSDL_TARGET_NAMESPACE);
		pi.setName(name);
		pi.setValue(value);
		return pi;
	}

	private static PropertyInfo makeDecimalProperty(String name, Double value) {
		PropertyInfo pi = new PropertyInfo();
		pi.setElementType(null);
		pi.setType(Double.class);
		pi.setNamespace(WSDL_TARGET_NAMESPACE);
		pi.setName(name);
		pi.setValue(value);
		return pi;
	}

	private PropertyInfo makeStringProperty(String name, String value) {
		PropertyInfo pi = new PropertyInfo();
		pi.setElementType(null);
		pi.setType(PropertyInfo.STRING_CLASS);
		pi.setNamespace(WSDL_TARGET_NAMESPACE);
		pi.setName(name);
		pi.setValue(value);
		return pi;
	}

	private class FileUploader extends AsyncTask<String, Integer, String> {
		private static final String OPERATION_NAME = "broadcast_message_with_upload";

		private String token = "";
		private File sample = null;

		public FileUploader(File sample) {
			// TODO Auto-generated constructor stub
			this.sample = sample;
		}
		
		private String getTokenAndUpload() {
			token = getToken();
			return uploadFile();
		}

		private String getToken() {
			
			// Getting an upload token requires the following:
			// 1) valid Tranceiver ID
			// 2) generating a BroadcastMessage on server
			
			String res = null;
			SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,
												OPERATION_NAME);

			// TODO: Fix bogus token
			request.addProperty(makeIntProperty("token", 7777777));
			request.addProperty(makeStringProperty("tranceiver_id", tranceiverID));

			res = new WebServiceCaller().executeSoap(request);
		    return res;
		}
		
		private String uploadFile() {
			String messageId = "";
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost postRequest = new HttpPost(SERVER_URL + "/samples/" + token + "/upload");
				FileEntity entity = new FileEntity(sample, "audio/3gpp");
				postRequest.setEntity(entity);
				HttpResponse response = httpClient.execute(postRequest);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

					// extract the message ID
					messageId = EntityUtils.toString(response.getEntity());
					Log.v(TAG, "Successfully uploaded file " + sample.getAbsolutePath() + 
							" with token " + token +
							" for message id " + messageId);
				} else {
					String rtext = EntityUtils.toString(response.getEntity());
					Log.v(TAG, "Problem uploading: status: " + response.getStatusLine());
					String[] srt = rtext.split("\n");
					for (int i = 0; i < srt.length; i += 10) {
						String p = "";
						for (int j = i; j < srt.length && j < i + 10; ++j) {
							p += srt[j] + "\n";
						}
						Log.v("r" + i + ":", p);
					}
				}
			} catch (ClientProtocolException e) {
				Log.e(TAG, "ClientProtocolException" + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "IOException" + e.getMessage());
			}
			return messageId;
		}
		
    	@Override
		protected String doInBackground(String... arg0) {
			return getTokenAndUpload();
    	}

    	@Override
		protected void onPostExecute(String result) {
			Log.v(TAG, "Result is: " + result);
			onMessageSent(result);
		}
	}

	private class WebServiceCaller extends AsyncTask<SoapObject, Integer, String> {
		
		private static final String REGISTER_TX = "register_tranceiver";
		private static final String TUNE_TRANCEIVER = "tune_tranceiver";
		private static final String UPDATE_LOCATION = "update_location";
		
		private SoapActions soapMethod = SoapActions.UNSET;

		public void registerTranceiver(String gcmRegistrationId, String email) {
			SoapObject action = new SoapObject(WSDL_TARGET_NAMESPACE, REGISTER_TX);
			action.addProperty(makeStringProperty("gcm_reg_id", gcmRegistrationId));
			action.addProperty(makeStringProperty("owner_email", email));
			soapMethod = SoapActions.REGISTER_TX;
			execute(action);
		}
		
		public void updateLocation(Double latitude, Double longitude) {
			SoapObject action = new SoapObject(WSDL_TARGET_NAMESPACE, UPDATE_LOCATION);
			action.addProperty(makeStringProperty("tranceiver_id", tranceiverID));
			action.addProperty(makeDecimalProperty("latitude", latitude));
			action.addProperty(makeDecimalProperty("longitude", longitude));
			soapMethod = SoapActions.UPDATE_LOCATION;
			execute(action);
		}
		
		public void tuneTranceiver(String channelName) {
			SoapObject action = new SoapObject(WSDL_TARGET_NAMESPACE, TUNE_TRANCEIVER);
			action.addProperty(makeStringProperty("tranceiver_id", tranceiverID));
			action.addProperty(makeStringProperty("channel_name", channelName));
			soapMethod = SoapActions.TUNE_CHANNEL;
			execute(action);
		}

		public String executeSoap(SoapObject soapObject) {
	        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
	        envelope.encodingStyle = SoapSerializationEnvelope.ENV;
	        envelope.setAddAdornments(false);
	        envelope.implicitTypes = true;
	        envelope.bodyOut = soapObject;
	        HttpTransportSE ht = new HttpTransportSE(SOAP_ADDRESS);
	        ht.debug = true;
	        try {
	        	Log.v(TAG, "call: " + SOAP_ADDRESS + soapObject.getName());
				ht.call(SOAP_ADDRESS + soapObject.getName(), envelope);
				return envelope.getResponse().toString();
			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        Log.e(TAG, ht.requestDump);
			Log.e(TAG, ht.responseDump);
	        
	        return "";
		}

		@Override
		protected String doInBackground(SoapObject... arg0) {
			// TODO Check that arg0 makes sense...
			return executeSoap(arg0[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			Log.v(TAG, "Result is: " + result);
			switch (soapMethod) {
			case REGISTER_TX:
				Log.v(TAG, "reg tx");
				setTranceiverID(result);
				break;
			case UNSET:
				Log.v(TAG, "Soap action unset :(");
				break;
			default:
				break;
			}
		}
	}
}
