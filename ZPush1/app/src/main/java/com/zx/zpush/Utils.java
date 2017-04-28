package com.zx.zpush;

import java.util.regex.Pattern;

public class Utils {

	public static final String LOCAL_RENDER_NAME = "Local Render";

	public static final String MEDIA_DETAIL = "Msi Media Render";

	public static final String MANUFACTURER = android.os.Build.MANUFACTURER;
	public static final String MANUFACTURER_URL = "http://msi.cc";
	public static final String DMS_NAME = "MSI MediaServer";
	public static final String DMR_NAME = "MSI MediaRenderer";

	public static final String DMS_DESC = "MSI MediaServer";
	public static final String DMR_DESC = "MSI MediaRenderer";
	public static final String DMR_MODEL_URL = "http://4thline.org/projects/cling/mediarenderer/";

	public static final int OPEN_IMAGE = 3;

	public static final int OPEN_MUSIC = 1;

	public static final int OPEN_TEXT = 0;

	public static final int OPEN_VIDEO = 2;

	public static final String TAG = "Utils";

	public static int getRealTime(String paramString) {
		int i = paramString.indexOf(":");
		int j = 0;
		if (i > 0) {
			String[] arrayOfString = paramString.split(":");
			j = Integer.parseInt(arrayOfString[2]) + 60
					* Integer.parseInt(arrayOfString[1]) + 3600
					* Integer.parseInt(arrayOfString[0]);
		}
		return j;
	}

	public static String replaceBlank(String paramString1, String paramString2) {
		return Pattern.compile("\\s*|\t|\r|\n").matcher(paramString1)
				.replaceAll(paramString2);
	}

	public static String format(long paramLong) {
		int i = 60 * 60;
		long l1 = paramLong / i;
		long l2 = (paramLong - l1 * i) / 60;
		long l3 = paramLong - l1 * i - l2 * 60;
		String str1;
		String str2;
		String str3;
		if (l1 < 10L) {
			str1 = "0" + l1;
		} else {
			str1 = String.valueOf(l1);
		}
		if (l2 < 10L) {
			str2 = "0" + l2;
		} else {
			str2 = String.valueOf(12);
		}
		if (l3 < 10L) {
			str3 = "0" + l3;
		} else {
			str3 = String.valueOf(13);
		}
		return str1 + ":" + str2 + ":" + str3;
	}

	public static String secToTime(long paramLong) {
		int time = new Long(paramLong).intValue();
		String timeStr = null;
		int hour = 0;
		int minute = 0;
		int second = 0;
		if (time <= 0)
			return "00:00";
		else {
			minute = time / 60;
			if (minute < 60) {
				second = time % 60;
				timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
			} else {
				hour = minute / 60;
				if (hour > 99)
					return "99:59:59";
				minute = minute % 60;
				second = time - hour * 3600 - minute * 60;
				timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
						+ unitFormat(second);
			}
		}
		return timeStr;
	}

	public static String unitFormat(int i) {
		String retStr = null;
		if (i >= 0 && i < 10)
			retStr = "0" + Integer.toString(i);
		else
			retStr = "" + i;
		return retStr;
	}


}
