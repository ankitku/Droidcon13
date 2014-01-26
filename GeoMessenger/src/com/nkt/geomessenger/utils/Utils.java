package com.nkt.geomessenger.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {

	public static final double R = 6372.8; // In kilometers

	public static double haversine(double lat1, double lon1, double lat2,
			double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2)
				* Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static <T> boolean isOneOf(T item, T... items) {
		for (T t : items) {
			if (item.equals(t))
				return true;
		}
		return false;

	}

	public static String getFilledUrl(String url, List<NameValuePair> list) {
		if (!url.endsWith("?"))
			url += "?";

		if (list == null)
			return url;

		String paramString = URLEncodedUtils.format(list, "utf-8");
		url += paramString;
		return url;
	}

	public static long diff(long time, int field) {
		long fieldTime = getFieldInMillis(field);
		Calendar cal = Calendar.getInstance();
		long now = cal.getTimeInMillis();
		return (time / fieldTime - now / fieldTime);
	}

	private static final long getFieldInMillis(int field) {
		final Calendar cal = Calendar.getInstance();
		long now = cal.getTimeInMillis();
		cal.add(field, 1);
		long after = cal.getTimeInMillis();
		return after - now;
	}

	public static String getHumanReadableTime(long timestamp) {
		Date date = new Date(timestamp * 1000);
		StringBuilder sb = new StringBuilder();

		SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a ");
		SimpleDateFormat date_format = new SimpleDateFormat("EEE, dd MMM");

		sb.append("sent at " + time_format.format(date) + ", ");

		String pickupdaytext = null;
		int pickuptimestatus = (int) Utils.diff(date.getTime(),
				Calendar.DAY_OF_YEAR);

		if (pickuptimestatus == 0)
			pickupdaytext = "Today";
		else if (pickuptimestatus == -1)
			pickupdaytext = "Yesterday";
		else if (pickuptimestatus == 1)
			pickupdaytext = "Tomorrow";
		else {
			pickupdaytext = date_format.format(date);
		}

		sb.append(pickupdaytext);

		return sb.toString();
	}

	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static Bitmap decodeSampledBitmapFromFile(String path) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;

		int reqHeight = height / 4;
		int reqWidth = width / 4;

		options.inPreferredConfig = Bitmap.Config.RGB_565;
		int inSampleSize = 1;

		if (height > reqHeight) {
			inSampleSize = Math.round((float) height / (float) reqHeight);
		}
		int expectedWidth = width / inSampleSize;

		if (expectedWidth > reqWidth) {
			// if(Math.round((float)width / (float)reqWidth) > inSampleSize) //
			// If bigger SampSize..
			inSampleSize = Math.round((float) width / (float) reqWidth);
		}

		options.inSampleSize = inSampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(path, options);
	}

	public static File convertBitmapToFile(Context context, Bitmap bmp,
			String filename) throws IOException {
		// create a file to write bitmap data
		File f = new File(context.getCacheDir(), filename);
		f.createNewFile();

		// Convert bitmap to byte array
		Bitmap bitmap = bmp;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();

		// write the bytes in file
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);

		fos.flush();
		return f;
	}

}
