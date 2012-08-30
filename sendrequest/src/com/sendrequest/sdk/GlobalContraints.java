package com.sendrequest.sdk;

import android.net.Uri;

public class GlobalContraints {

	private static final String PROVIDER_NAME = "com.appstore.market.cp";

	public static final Uri CONTENT_URI = Uri.parse("content://"
			+ PROVIDER_NAME + "/urlpermission");

	public static final String _ID = "id";

	public static final String SERVICE_NAME = "service_name";

	public static final String URL = "url";
	
	public static final String RULE = "rule";
}
