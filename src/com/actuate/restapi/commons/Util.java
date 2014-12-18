package com.actuate.restapi.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.Asserts;

public class Util
{
	/**
	 * Hide constructor to make class static
	 */
	protected Util() {
	}
	
	public static void log(String message) {
		System.out.println(message);
	}
	
	public static String getMethodName() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		if(stack != null && stack.length > 2) {
			String fullName = stack[2].getClassName() + "." + stack[2].getMethodName();
			return fullName.substring(fullName.lastIndexOf(".") + 1);
		} else {
			return null;
		}
	}
	/**
	 * Generates a String value from thread id and millies, which should be
	 * unique for the specific application instance.
	 * 
	 * @return String value
	 */
	public static String getUniqueID()
	{
		return Long.toHexString(Thread.currentThread().getId())+ Long.toHexString(System.currentTimeMillis());
	}

	public static boolean sleep(int delayInMilliseconds)
	{
		try {
			Thread.sleep(delayInMilliseconds);
		} catch(Throwable e) {
			Asserts.check(false, "Sleep() failed:\n" + e.toString());
		}
		return true;
	}
	
	public static String getXmlDateString(Long dayOffset, String timeZone) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		f.setTimeZone(TimeZone.getTimeZone(timeZone));
		if(dayOffset != null && dayOffset != 0) {
			return f.format(new Date((Long)((new Date()).getTime() + 1000*60*60*24*dayOffset)));
		} else {
			return f.format(new Date());
		}
	}
	
	public static String getXmlDateString(Long dayOffset) {
		return Util.getXmlDateString(dayOffset, "GMT");
	}
	
	public static String getDayOfWeekString(Long dayOffset, String timeZone) {
		SimpleDateFormat f = new SimpleDateFormat("EEE");
		f.setTimeZone(TimeZone.getTimeZone(timeZone));
		if(dayOffset != null && dayOffset != 0) {
			return f.format(new Date((Long)((new Date()).getTime() + 1000*60*60*24*dayOffset)));
		} else {
			return f.format(new Date());
		}
	}
	
	public static String getDayOfMonthString(int dayOffset, String timeZone) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone(timeZone));
		cal.add(Calendar.DAY_OF_MONTH, dayOffset);
		
		if(cal.get(Calendar.DAY_OF_MONTH) == cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
			return "0";
		} else {
			return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		}
	}
	
	public static String getXmlTimeString(Long hourOffset, Long minOffset, Long secOffset, String timeZone) {
		SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone(timeZone));
		Long offset = 0L;
		if(hourOffset != null && hourOffset != 0) {
			offset += 1000*60*60*hourOffset;
		}
		if(minOffset != null && minOffset != 0) {
			offset += 1000*60*minOffset;
		}
		if(secOffset != null && secOffset != 0) {
			offset += 1000*secOffset;
		}
		return f.format((offset == 0)?(new Date()):(new Date((Long)((new Date()).getTime() + offset))));
	}
	
	public static String getXmlTimeString(Long hourOffset, Long minOffset, Long secOffset) {
		return Util.getXmlTimeString(hourOffset, minOffset, secOffset, "GMT");
	}
	
	public static String getXmlDateTimeString(Long hourOffset, Long minOffset, Long secOffset, String timeZone) {
		return getXmlDateString(0L, timeZone) + "T" + getXmlTimeString(hourOffset, minOffset, secOffset, timeZone);
	}
	
	public static String getXmlDateTimeString(Long hourOffset, Long minOffset, Long secOffset) {
		return getXmlDateString(0L) + "T" + getXmlTimeString(hourOffset, minOffset, secOffset);
	}
	
	public static String getXmlDateString(Calendar cal, String timeZone) {
		String formatted = null;
		if(cal != null) {
			Asserts.check(cal instanceof GregorianCalendar, "Not an instance of Gregorian calendar");
			GregorianCalendar c = (GregorianCalendar)cal.clone();
			c.setTimeZone(TimeZone.getTimeZone(timeZone));
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			formatted = f.format(cal.getTime());
		}
		return formatted;
	}
	
	public static String getXmlDateString(Calendar cal) {
		return Util.getXmlDateString(cal, "GMT");
	}
	
	public static String getXmlTimeString(Calendar cal, String timeZone) {
		String formatted = null;
		if(cal != null) {
			Asserts.check(cal instanceof GregorianCalendar, "Not an instance of Gregorian calendar");
			GregorianCalendar c = (GregorianCalendar)cal.clone();
			c.setTimeZone(TimeZone.getTimeZone(timeZone));
			SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
			formatted = f.format(cal.getTime());
		}
		return formatted;
	}
	
	public static String getXmlTimeString(Calendar cal) {
		return Util.getXmlTimeString(cal, "GMT");
	}
	
	public static String getXmlDateTimeString(Calendar cal, String timeZone) {
		return getXmlDateString(cal, timeZone) + "T" + getXmlTimeString(cal, timeZone);
	}
	
	public static String getXmlDateTimeString(Calendar cal) {
		return getXmlDateString(cal) + "T" + getXmlTimeString(cal);
	}
	
	public static String getFileExtension(String filePath) {
		String extension = "";
		int i = filePath.lastIndexOf('.');
		if (i > 0) {
		    extension = filePath.substring(i+1);
		}
		return extension;
	}
	
	public static boolean equalOrNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 != null && o2 != null) {
			if(!o1.getClass().getName().equals(o2.getClass().getName())) {
				return false;
			}
			if(o1.getClass().getName().startsWith("[")) {
				return equalOrNull(o1, o2); 
			}
			return o1.equals(o2);
		} else {
			return false;
		}
	}
	
	public static boolean equalOrNull(Object[] o1, Object[] o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else if (o1 != null && o2 != null) {
			if(o1.length != o2.length) {
				return false;
			}
			for(int i = 0; i < o1.length; i++) {
				if(!equalOrNull(o1[i], o2[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static Object nullIfEmpty(Object obj) {
		if(obj == null) {
			return null;
		}
		if(obj instanceof java.util.HashMap && ((java.util.HashMap<?,?>) obj).size() == 0) {
			return null;
		}
		return obj;
	}
	
	public static String getProcessOutput(Process p) {
		String output = "";
		String s = null;
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		try {
	        // read the output from the command
	        while ((s = stdInput.readLine()) != null) {
	        	output = output + "\n" + s;
	        }
		} catch (Throwable e) {
			Asserts.check(false, "Cannot read standard input" + e.toString());
		}
        return output;
	}

	public static String getProcessError(Process p) {
		String error = "";
		String s = null;
        BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(p.getErrorStream()));
        try {
	        // read any errors from the attempted command
	        while ((s = stdError.readLine()) != null) {
	        	error = error + "\r\n" + s;
	        }
        } catch (Throwable e) {
			Asserts.check(false, "Cannot read standard input" + e.toString());
		}
        return error;
	}
	
	public static String encodeFileToBase64Binary(String fileName) {
		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] encoded = Base64.encodeBase64(bytes);
		String encodedString = new String(encoded);
		return encodedString;
	}

	public static String decodeFileToBase64Binary(String fileName) {
		File file = new File(fileName);
		byte[] bytes = loadFile(file);
		byte[] decoded = Base64.decodeBase64(bytes);
		String decodedString = new String(decoded);
		return decodedString;
	}
	
	private static byte[] loadFile(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (Throwable e) {
			Asserts.check(false, "File not found" + e.toString());
		}
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int)length];

		int offset = 0;
		int numRead = 0;
		try {
			while (offset < bytes.length
			   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
			}
		
			is.close();
		} catch (Throwable e) {
			Asserts.check(false, "Cannot read from file" + file.getName() + e.toString());
		}

		if (offset < bytes.length) {
			Asserts.check(false, "Could not completely read file " + file.getName());
		}

		return bytes;
	}
	
	public static void throwException(String message) throws ClientProtocolException {
		Util.log(message);
		throw new ClientProtocolException(message);
	}
	
	public static void throwException(String message, Throwable e) throws ClientProtocolException {
		Util.log(message + "\n" + e.toString());
		throw new ClientProtocolException(message, e);
	}
	
	public static File saveToFile(byte[] array, String filePath) {
		OutputStream outputStream = null;
		File fh = new File(filePath);
		try {
			outputStream = new FileOutputStream(fh);
			outputStream.write(array);
		} catch (Throwable e) {
			Asserts.check(false, "Failed to write to file: " + filePath, e);
		} finally {
			try {
				outputStream.flush();
				outputStream.close();
			} catch (Throwable e) {
				Asserts.check(false, "Cannot close the stream: " + filePath, e);
			}
		}
		return fh;
	}
	
	public static File saveToFile(InputStream in, File file) throws IOException {
	    OutputStream out = new FileOutputStream(file);
	    byte[] buffer = new byte[8 * 1024];
	    int bytesRead = 0;
	    while(true) {
	    	bytesRead = in.read(buffer);
	    	if(bytesRead >= 0) {
	    		out.write(buffer, 0, bytesRead);
	    	} else {
	    		break;
	    	}
	    }
	    in.close();
	    out.close();
		Asserts.check(file != null && file.exists(), "File not found: " + file.getAbsolutePath());
		return file;
	}

	//###################### Date/Time related methods ##################################

	public String dateStr(int addYears, int addMonths, int addDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, addYears);
		calendar.add(Calendar.MONTH, addMonths);
		calendar.add(Calendar.DAY_OF_MONTH, addDays);
		return new java.text.SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
	}
	
	public String timeStr(int addHours, int addMinutes, int addSeconds) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR_OF_DAY, addHours);
		calendar.add(Calendar.MINUTE, addMinutes);
		calendar.add(Calendar.SECOND, addSeconds);
		return new java.text.SimpleDateFormat("HH:mm:ss").format(calendar.getTime());
	}

}

