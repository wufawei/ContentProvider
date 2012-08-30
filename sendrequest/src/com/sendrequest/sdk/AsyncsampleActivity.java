package com.sendrequest.sdk;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sendrequest.sdk.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class AsyncsampleActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
    }
    

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
        
		String url = "http://www.google.com";
               
		String serviceName = "";
        String method = "GET";
        Uri uri= Uri.parse(url);
        try {
        	InputStream in = SendRequestUtil.sendRequest(this, serviceName, method, uri, null);
        	
        	if (in != null)
        	{
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                List<String> response = new ArrayList<String>();
                 
                String line = "";
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
                reader.close();
                
                Log.e("AfterResponse", response.toString());
        	}

            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("AfterResponse", e.toString());
		}
	}
    
    
}