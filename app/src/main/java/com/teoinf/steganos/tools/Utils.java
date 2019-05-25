package com.teoinf.steganos.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import dalvik.system.DexFile;

public class Utils {
	
	public static final int MAX_BYTE_TO_HIDE = 30720000; // 30mo
	public static final int MAX_CHAR_BEFORE_CREATE_FILE_ON_DECODE = 100;
	public static long  currentTime;
	public static long  previousTime;
	

	public static byte setLSB(byte b, int bitValue) {
		return (byte) (b & ~1 | bitValue);
	}


	public static byte setSpecificBit(byte b, int bitValue, int offset) {
		if (bitValue == 1)
			return (byte) (b | (1 << offset));
		
		return (byte) (b & ~(1 << offset));

	}


	public static String byteToBinStr(byte b) {
		return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}


	public static int getBit(byte b, int index) {
		return b >> index & 1;
	}


	public static int getLSB(byte b) {
		return b >> 0 & 1;
	}


	public static int getBitInByteArray(byte[] array, int bitOffset) {
		int byteNb = (int)bitOffset / 8;
		int[] invert = {7,6,5,4,3,2,1,0};
		int bitNb =  invert[bitOffset - (byteNb * 8)];

		return Utils.getBit(array[byteNb], bitNb);
	}

	public static byte[] setBitInByteArray(byte[] array, int bitValue, int bitOffset) {
		int byteNb = (int)bitOffset / 8;
		int[] invert = {7,6,5,4,3,2,1,0};
		int bitNb =  invert[bitOffset - (byteNb * 8)];

		array[byteNb] = Utils.setSpecificBit(array[byteNb], bitValue, bitNb);
		return array;
	}

	public static byte[] intToByteArray(int number) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		b.putInt(number);
		byte[] toHideByteArray = b.array();

		return toHideByteArray;
	}

	
	public static List<String> getClassesPathFromPackage(Context context, String packName) {
		DexFile dex;
		Enumeration<String> enumClasses;
		String tmp;
		List<String> classes = new ArrayList<String>();
		
		 try {
			 dex = new DexFile(context.getPackageCodePath());
		     enumClasses = dex.entries();
		     while (enumClasses.hasMoreElements()) {
		    	 tmp = enumClasses.nextElement();
		    	 if (tmp.substring(0, tmp.lastIndexOf(".")).equals(packName) && !tmp.contains("$")) {
		    		 classes.add(tmp);
		    	 }
		     }
		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 return (classes);
	}
	
	public static String convertClassNameToReadableName(String name) {
		StringBuffer res = new StringBuffer("");
		
		if (name.contains(".") && name.lastIndexOf(".") != name.length() - 1) {
			res.append(name.substring(name.lastIndexOf(".") + 1));
		} else {
			res.append(name);
		}
		
		if (!res.toString().toUpperCase().equals(res.toString())) {
			for (int i = 0; i < res.length(); ++i) {
				if ((res.charAt(i) >= 'A' && res.charAt(i) <= 'Z' || res.charAt(i) >= '0' && res.charAt(i) <= '9')
						&& i != 0 && res.charAt(i - 1) != ' ') {
					res.insert(i, ' ');
					++i;
				}
			}
		}
		return (res.toString());
	}

	public static String getBasenameFromPath(String path) {
		StringBuilder sb; 
		String chunks[];
		
		sb = new StringBuilder();
		if (path == null) {
			return (sb.toString());
		}
		
		chunks = path.split("/");
		if (chunks == null || chunks.length == 0) {
			return (sb.toString());
		}
		
		for (int i = 0; i < chunks.length - 1; ++i) {
			if (chunks[i] != null && chunks[i].length() != 0) {
				sb.append('/').append(chunks[i]);
			}
		}
		sb.append("/");
		return (sb.toString());
	}

	public static String getRealPathFromUri(Context context, Uri uri) {
		Cursor cursor;
		String projection[] = { MediaStore.Video.Media.DATA };
		int idx;
		String ret = "";
		
		cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor == null) {
			return (uri.getPath());
		}
		idx = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
		if (idx != -1) {
			cursor.moveToFirst();
			ret = cursor.getString(idx);
		}
		cursor.close();
		return (ret);
	}
	
	public static String getFileNameFromPath(String path) {
		String ret = "";
		File file;
		
		if (path != null) {
			file = new File(path);
			ret = file.getName();
		}
		return (ret);
	}
	
	public static byte[] getContentOfFileAsByteArray(String path) {
		FileInputStream inputStream;
		File file;
		byte ret[] = null;
		
		file = new File(path);
		try {
			inputStream = new FileInputStream(new File(path));
			ret = new byte[(int) file.length()];
			inputStream.read(ret);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return ret;
	}
	
	public static String getCurrentDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static long getFileSize(String path) {
		File file = new File(path);
		
		if (file != null && file.isFile()) {
			return file.length();
		}
		return -1;
	} 
	
	@SuppressWarnings("deprecation")
	public static long getAvailableBytesOnSdcard() {
		String path = Environment.getExternalStorageDirectory().getPath();
		StatFs fs = new StatFs(path);
		return (long) fs.getBlockSize() * (long) fs.getAvailableBlocks();
	}
    public static void printTime(String s) {
        previousTime = currentTime;
        currentTime = System.currentTimeMillis();
        Log.i("STEGA", s + (currentTime - previousTime));
    }
    public static void setStartTime(){        currentTime = System.currentTimeMillis();
	}
	
}
