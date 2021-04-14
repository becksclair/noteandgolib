package com.heliasar.noteandgolib;

import android.content.Context;

public class Config {

	public static final boolean DEBUG = false;
	public static final boolean LOGGING = false;
	
	public static boolean IsPaid(Context context) {
		return context.getResources().getBoolean(R.bool.paid);
	}
	
	public static final String BASE_64_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArAY4FFlll95awqNQD8Xh7IJHO7gzIShCowq+n3XJfgJeJWJqApXyM/p8Jxm9XY7FAujRxuka1Z1JfnFaaZ6vpNTN5U6Tuc9axB3ObkG15iOYAPmQblQ8mwVMbqD+FsKeoTFJOAhK6vP5y6mWRVemDxZerrCzs12q7dGaK7Gzfy+jGSQgGuMUJhFrINX0oZanPiVl8/CpmlEEtY/yY/GOlKzahMgZE4TLH/2pjsOghTqZl4Omw+tUXsXCeykstYJ3Lpix+qNBLS00d+ObBYEaVcxuGKB1lSf/pWFRhmrbsTFM78PbPiSsPIfGcIbAFW8bfa47zZ63v9qKPRL0M3qtjwIDAQAB";

}
