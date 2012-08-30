package com.sendrequest.sdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class SendRequestUtil {
    
	private static final String TAG = SendRequestUtil.class.getName();
	private static int sEventType = 0;
	
    private static void attachUriWithQuery(HttpRequestBase request, Uri uri, List<BasicNameValuePair> params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            }
            else {
                Uri.Builder uriBuilder = uri.buildUpon();
                
                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : params) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }
                
                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString(), e);
        }
    }
    
    /**
     * send http request
     * @param ctx,  context of Activity
     * @param serviceName, service name
     * @param method, GET/POST/PUT/DELETE
     * @param action, Uri, for example, Uri.parse("http://search.twitter.com/search.json")
     * @param params, List<BasicNameValuePair>, can be null if not exist.
     * 
     * return InputStream, see demo for detail use
     */
    
	public static InputStream sendRequest(Context ctx, String serviceName, String method, Uri action, List<BasicNameValuePair> params) throws Exception
	{
		
		//Log.d("sendRequest", "serviceName is " + serviceName);
		
		String result = getContentProviderValues(ctx, serviceName, action.toString());
		
		Log.d("sendRequest", "result is " + result);
		
		if(result == "" || result  == null || result.equals("NO"))
			return null;
		
        // Here we define our base request object which we will
        // send to our REST service via HttpClient.
        HttpRequestBase request = null;
       
        // Let's build our request based on the HTTP verb we were
        // given.
        
        method = method.toUpperCase();
        if (method.equals("GET"))
        {
        	sEventType = 0;
            request = new HttpGet();
            attachUriWithQuery(request, action, params);
        }
        else if (method.equals("DELETE"))
        {
        	sEventType = 3;
            request = new HttpDelete();
            attachUriWithQuery(request, action, params);
        }
        else if (method.equals("POST"))
        {
        	sEventType = 1;
            request = new HttpPost();
            request.setURI(new URI(action.toString()));
            
            // Attach form entity if necessary. Note: some REST APIs
            // require you to POST JSON. This is easy to do, simply use
            // postRequest.setHeader('Content-Type', 'application/json')
            // and StringEntity instead. Same thing for the PUT case 
            // below.
            HttpPost postRequest = (HttpPost) request;
            
            if (params != null) {
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);                        
                postRequest.setEntity(formEntity);
            }
        }
        else if (method.equals("PUT"))
        {
        	sEventType = 2;
            request = new HttpPut();
            request.setURI(new URI(action.toString()));
            
            // Attach form entity if necessary.
            HttpPut putRequest = (HttpPut) request;
            
            if (params != null) {
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
                putRequest.setEntity(formEntity);
            }
        }
        
        if (request != null) {
            HttpClient client = new DefaultHttpClient();
            
            HttpResponse response = client.execute(request);
            
            HttpEntity responseEntity = response.getEntity();
            StatusLine responseStatus = response.getStatusLine();
            int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
            
            
            String responseBody = null;
            if (responseEntity != null) {
                //responseBody = EntityUtils.toString(responseEntity);
                //Log.e(TAG, "responseBody:" + responseBody);
            	
            	//save log to cp
            	
            	String serviceURL = action.toString();
            	int eventType = sEventType;        	
            	String deviceUser = "";
            	String operationResult = Integer.toString(statusCode);
            	String operationDesc = "";    	
            	String createDate = now(); 
            	
            	saveLog2ContentProvider(ctx,serviceURL, eventType
            			,deviceUser, operationResult, operationDesc, createDate);
            	
                return responseEntity.getContent();
            }
            
            
            return null;
            
        }
		return null;
	}
	
	private static String getContentProviderValues(Context ctx, String serviceName, String url) {
		StringBuilder builder = new StringBuilder();

		
		String columns[] = new String[] { GlobalContraints.SERVICE_NAME, GlobalContraints.URL, GlobalContraints.RULE};
		
	
		Cursor cursor = ctx.getContentResolver().query(GlobalContraints.CONTENT_URI,
				                     columns,
				                     GlobalContraints.URL + "=?" + " AND " + GlobalContraints.SERVICE_NAME + "=?",
				//new String[] { "http://www.baidu.com/1", "service1"},
				new String[] { url, serviceName},
				null);
		
		while (cursor.moveToNext()) {
			builder.append(
							cursor.getString(cursor
									.getColumnIndex(GlobalContraints.RULE)));
		}
		cursor.close();
		// Log.d("builder result", builder.toString());
		return builder.toString();
	}
	
	private static void  saveLog2ContentProvider(Context ctx, String serviceURL, 
	        int eventType, String deviceUser, String operationResult, 
	        String operationDesc, String createDate) {

		ContentValues values = new ContentValues();
    	values.put("serviceURL", serviceURL);
    	values.put("eventType", eventType);        	
    	values.put("deviceUser", "");
    	values.put("operationResult", operationResult);
    	values.put("operationDesc", operationDesc);    	
    	values.put("createDate", createDate); 
				
		Uri result = ctx.getContentResolver().insert(GlobalContraints.CONTENT_URI, values);
		Log.d("saveLog2ContentProvider", " done");
	}
	
	
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    return sdf.format(cal.getTime());

	  }
}
