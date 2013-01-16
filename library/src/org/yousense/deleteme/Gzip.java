package org.yousense.deleteme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.gson.internal.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.util.Log;

public class Gzip {
	/**
	 * Reads entire GZipped file in UTF-8 encoding into a string.
	 */
	public static String readGzipFile(String filename) throws IOException {
		return readGzipFile(new File(filename));
	}
	
	/**
	 * Reads entire GZipped file in UTF-8 encoding into a string.
	 */
	public static String readGzipFile(File file) throws IOException {
		final int BUFSIZE = 8192;
		
		InputStreamReader reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), Files.UTF8);
		try {
			StringBuilder sb = new StringBuilder();			
			char[] buf = new char[BUFSIZE];
			
			int read = 0;
			while ((read = reader.read(buf)) != -1) {
				sb.append(buf,  0, read);
			}
			
			return sb.toString();		
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Returns true if GZipped file contents is equal to the other uncompressed file contents.
	 */
	public static boolean gzipFileContentsEqual(File gzipFile, File uncompressedFile) {
		try {
			GZIPInputStream gzipped = new GZIPInputStream(FileUtils.openInputStream(gzipFile));
			BufferedInputStream uncompressed = new BufferedInputStream(FileUtils.openInputStream(uncompressedFile));
			boolean equal = IOUtils.contentEquals(gzipped, uncompressed);
			gzipped.close();
			uncompressed.close();
			return equal;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Gzips file, creating a new file with the same filename but ".gz" appended.
	 * Does not delete original.
	 * Does not clean up after errors.
	 */
	public static void gzip(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzipOutputFile(file)));
		try {
			try {
				Streams.copyStream(in, out);
			} finally {
				in.close();
			}
		} finally {
			out.close();
		}
	}
	
	/**
	 * Gzips file, creating a new file with the same filename but ".gz" appended.
	 * Attempts to clean up output file if there was an error.
	 * Does not delete original.
	 * Returns true if no errors happened.
	 */
	public static boolean tryGzipCleanupOnError(File file) {
		try {
			gzip(file);
			return true;
		} catch (IOException e) {
			Log.e("pclient", "gzip failed", e);
			gzipOutputFile(file).delete();
			return false;
		}
	}
	
	/**
	 * Gzips file, creating a new file with the same filename but ".gz" appended.
	 * Checks that the data in the final file matches input file.
	 * Attempts to clean up output file if there was an error.
	 * Does not delete original.
	 * Returns true if eventually succeeded.
	 * TODO: make this work for non-UTF8 files. 
	 */
	public static boolean tryGzipCheckOutputCleanupOnError(File file, int retries) {
		if (retries < 1)
			throw new IllegalArgumentException("Cannot try gzipping less than once.");
		
		for (int retry = 0; retry < retries; ++retry) {
			Log.d("pclient", "gzip try " + retry);
			if (tryGzipCleanupOnError(file)) {
				if (gzipFileContentsEqual(gzipOutputFile(file), file)) {	
					return true;
				} else {
					Log.e("pclient", "Gzip does not match original!");
					gzipOutputFile(file).delete();
				}
			}				
		}
		return false;
	}
	
	/**
	 * Returns the file with ".gz" appended.
	 */
	public static File gzipOutputFile(File inputfile) {
		return new File(inputfile.getAbsolutePath() + ".gz");
	}
}
