package org.yousense.deleteme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import uk.ac.cam.cl.dtg.androidshortcuts.Files;
import uk.ac.cam.cl.dtg.androidshortcuts.Gzip;
import uk.ac.cam.cl.dtg.androidshortcuts.LaunchCounter;
import uk.ac.cam.cl.dtg.androidshortcuts.PersistentCounter;
import uk.ac.cam.cl.dtg.androidshortcuts.PersistentCounter.LazyPersistentCounterHolder;
import uk.ac.cam.cl.dtg.androidshortcuts.Time;
import uk.ac.cam.cl.dtg.androidshortcuts.Version;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Manages one persistent queue of objects. Stored as time-stamped JSON files on SDCard, GZipped after regular intervals or on request. 
 */
public class ObjectQueue {

	public static final String MAGIC_MARKER = "\0A\0b\0b\0a\0";
	public static final long COUNTER_PERSIST_INTERVAL = 100;	
	
	Gson gson = new Gson();
	
	Context context;
	File queuesStorageDirectory;
	String name;
	File storageDir;
	
	File openFile;
	BufferedWriter openWriter;
	long openCounter;
	PersistentCounter queueCounter;
	PersistentCounter globalCounter;
	LazyPersistentCounterHolder counterHolder;
	LaunchCounter launchCounter;
	
	public ObjectQueue(Context context, File queuesStorageDirectory, LazyPersistentCounterHolder counterHolder, LaunchCounter launchCounter, String name) {
		this.context = context;
		this.queuesStorageDirectory = queuesStorageDirectory;
		this.name = name;
		this.storageDir = new File(queuesStorageDirectory, name);

		this.openFile = null;
		this.openWriter = null;
		this.openCounter = 0;
		this.queueCounter = counterHolder.get(context, "objectqueue-" + name, COUNTER_PERSIST_INTERVAL);
		this.globalCounter = counterHolder.get(context, "objectqueueglobal", COUNTER_PERSIST_INTERVAL);
		this.counterHolder = counterHolder;
		this.launchCounter = launchCounter;
		
		this.storageDir.mkdirs();
		tryRotateOpenFile(true); // GZip existing, open new file.
	}
	
	/// PUBLIC INTERFACE
	
	/**
	 * Serialises object as JSON into the latest storage file. Object must be serialisable by Gson.
	 */
	public synchronized boolean append(String tag, Object data) {
		try {			
			tryWriteWithHeader(tag, data);
			return true;
		} catch (IOException e) {
			tryRotateOpenFile(false);
			try {
				tryWriteWithHeader(tag, data);
				return true;
			} catch (IOException e2) {
				return false;
			}
		}
	}
		
	/**
	 * Forces the queue to close the existing storage file and open a new one.
	 * 
	 */
	public synchronized boolean tryRotateOpenFile(boolean gzipPendingFiles) {
		// Close open file, if it exists.
		if (openWriter != null) {
			try {
				openWriter.close();
			} catch (IOException e) {				
			}
			openWriter = null;
			openFile = null;
			openCounter = 0;
		}
		
		if (gzipPendingFiles) {
			// Remove all normal files of 0 length.
			for (File file : Files.listdir(storageDir, usualFilenameFilter)) {
				if (file.length() == 0)
					file.delete();
			}
					
			// gzip all normal files, delete originals.
			for (File file : Files.listdir(storageDir, usualFilenameFilter)) {
				if (Gzip.tryGzipCheckOutputCleanupOnError(file, 3)) {
					// gzipping succeeded, delete original.
					file.delete();
				} else {
					// gzipping failed after 3 tries. Ignore for now, keep original for next time.
					Log.e("pclient", "gzip failed for 3 tries");
				}
			}
		}
		
		// open new file.
		try {
			openFile = nextFile();
			openWriter = new BufferedWriter(new FileWriter(openFile));
			openCounter = 1;
			return true;
		} catch (IOException e) {
			openFile = null;
			openWriter = null;
			openCounter = 0;
			return false;
		}
	}
	
	/**
	 * Retrieves filenames of all compressed storage files.
	 */	
	public synchronized File[] getGzipFiles() {
		return Files.listdir(storageDir, gzFilenameFilter);
	}
	
	/**
	 * Deletes compressed storage file - perhaps after the data is no longer needed.
	 * Returns true if delete succeeded.
	 */
	public synchronized boolean deleteGzipFile(File file) {
		return file.delete();
	}
	
	public String getName() {
		return name;
	}
	
	public synchronized String debugGetAllFilenames() {
		StringBuilder sb = new StringBuilder();
		for (File file : Files.listdir(storageDir))
			sb.append(file.getAbsolutePath() + " ");
		return sb.toString();
	}
	
	/// Helpers
	
	private TaggedObject nextTaggedObject(String tag, Object data) {
		PersistentCounter keyCounter = counterHolder.get(context, String.format("key-%s.%s", name, tag), COUNTER_PERSIST_INTERVAL);
		return new TaggedObject(tag, data, openCounter++, keyCounter.getNext(), queueCounter.getNext(), globalCounter.getNext());
	}
	
	private File nextFile() {
		String filename = String.format("%s-%s-%s-g%d-q%d-b%d-%s",
				Version.appPackageName(context),
				Identity.getInstallId(context),
				name,
				globalCounter.getNext(),
				queueCounter.getNext(),
				launchCounter.getValue(),
				Time.timestamp());
		return new File(storageDir, filename);
	}
	
	private void tryWriteWithHeader(String event, Object data) throws IOException {
		if (openCounter <= 1)
			tryWrite(nextTaggedObject("header", new HeaderObjectData(context, name, launchCounter.getValue())));
		tryWrite(nextTaggedObject(event, data));
	}
	
	private void tryWrite(Object object) throws IOException {
		if (openWriter != null) {
			openWriter.write(MAGIC_MARKER);
			gson.toJson(object, openWriter);
		} else {
			throw new IOException("Cannot write to a null writer");
		}
	}
	
	private static FilenameFilter gzFilenameFilter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".gz");
		}		
	};
	
	private static FilenameFilter usualFilenameFilter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return !filename.endsWith(".gz");
		}		
	};
}
