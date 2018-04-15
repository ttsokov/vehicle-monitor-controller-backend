package org.ttsokov.vehicle_monitor_controller.utils;

import java.net.URLDecoder;

//import com.sap.security.core.server.csi.IXSSEncoder;
//import com.sap.security.core.server.csi.XSSEncoder;

public class Utils {

	public static String encodeText(String text) {

		String result = null;
		if (text != null && text.length() > 0) {

// Optional jar containing security related functionality, which have to be downloaded and included in the classpath
//			IXSSEncoder xssEncoder = XSSEncoder.getInstance();

			try {
//				result = (String) xssEncoder.encodeURL(text).toString();
				result = URLDecoder.decode(text, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return result;
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
