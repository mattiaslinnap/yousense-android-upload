package org.yousense.deleteme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;

import com.linnap.uplog.ObjectQueue;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

/**
 * Class for sending application logs to DDMS Log, file on SDCard, and a scrolling TextView on screen.
 */
public class LogEverywhere {

	public static final int FILE_REOPEN_ATTEMPTS = 2;  // How many times to try reopening the file if it cannot be written to. 
	
	String syslogTag;
	String sdcardDirectory;
	String sdcardFilename;
	TextView screenLogView;
	ObjectQueue objectQueue;
	
	int screenLogMaxLines;
	ArrayDeque<String> screenLogLines;
	String screenLogText;
	BufferedWriter fileWriter;
	
	/**
	 * sdcardFilename must be full path including filename.
	 * You must manually set android:scrollbars="vertical" in XML to the TextView.
	 */
	public LogEverywhere(String syslogTag,
			String sdcardDirectory,
			String sdcardFilename,
			TextView screenLogView,
			int screenLogMaxLines,
			ObjectQueue objectQueue) {
		this.syslogTag = syslogTag;
		this.sdcardDirectory = sdcardDirectory;
		this.sdcardFilename = sdcardFilename;
		this.screenLogView = screenLogView;
		this.screenLogMaxLines = screenLogMaxLines;
		this.objectQueue = objectQueue;
		this.screenLogLines = new ArrayDeque<String>(screenLogMaxLines);
		this.screenLogText = "";
		
		if (sdcardFilename != null) {
			tryOpeningLogFile();
		}
	}
	
	public synchronized void close() {
		if (fileWriter != null) {
			tryClosingLogFile();
		}
	}
	
	/**
	 * You must manually set android:scrollbars="vertical" in XML to the TextView.
	 */
	public synchronized void setScreenLogView(TextView screenLogView) {
		this.screenLogView = screenLogView;
		if (screenLogView != null) {
			screenLogView.setMovementMethod(new ScrollingMovementMethod());
			screenLogView.post(updateScreenLog);
		}	
	}
	
	// Logging API
	
	public synchronized void log(String message) {
		log(message, null);
	}
	
	public synchronized void log(String message, Throwable tr) {
		// Pass exception to the standard DDMS log handler. It will get stack trace itself.
		if (tr != null)
			Log.e(syslogTag, message, tr);  // Logs with throwables are errors.
		else
			Log.d(syslogTag, message); // Logs without throwables are debug messages.
		
		addScreenLogLine(Time.shortLocalTime() + ": " + message);  // Do not include full stack trace on screen.
		
		// Include full stack trace in file.
		if (tr != null)
			tryWritingToLogFile(String.format("%s: %s\n%s", Time.timestamp(), message, Log.getStackTraceString(tr)));
		else
			tryWritingToLogFile(String.format("%s: %s\n", Time.timestamp(), message));
		
		if (objectQueue != null) {
			objectQueue.append("log", new LogMessage(message, tr));
		}
	}
	
	// Helpers
	
	private void addScreenLogLine(String line) {
		screenLogLines.addFirst(line);
		if (screenLogLines.size() > screenLogMaxLines)
			screenLogLines.removeLast();
		screenLogText = Strings.join("\n", screenLogLines);
		
		if (screenLogView != null)
			screenLogView.post(updateScreenLog);
	}
	
	private Runnable updateScreenLog = new Runnable() {
		public void run() {
			if (screenLogView != null)
				screenLogView.setText(screenLogText);
		}
	};
	
	private void tryWritingToLogFile(String fullMessage) {
		for (int attempt = 0; attempt < FILE_REOPEN_ATTEMPTS; ++attempt) {
			try {
				if (fileWriter == null)
					throw new IOException("LogEverywhere.fileWriter is null");
				fileWriter.write(fullMessage);
				fileWriter.flush();
				break;
			} catch (IOException e) {
				tryClosingLogFile();
				tryOpeningLogFile();
			}
		}
	}
	
	private void tryOpeningLogFile() {
		try {
			new File(sdcardDirectory).mkdirs();
			new File(sdcardFilename).createNewFile();
			fileWriter = new BufferedWriter(new FileWriter(sdcardFilename, true));
			fileWriter.write(Time.timestamp() + ": Opened log\n");
			fileWriter.flush();
			logIoError("Opened log " + sdcardFilename, null);
		} catch (IOException e) {
			fileWriter = null;
			logIoError("Cannot open log " + sdcardFilename, e);
		}
	}
	
	private void tryClosingLogFile() {
		if (fileWriter != null) {
			try {
				fileWriter.close();
				logIoError("Closed log " + sdcardFilename, null);
			} catch (IOException e) {
				logIoError("Cannot close log " + sdcardFilename, e);
			}
			fileWriter = null;
		}
	}
	
	private void logIoError(String message, Throwable tr) {
		Log.e(syslogTag, message, tr);
		addScreenLogLine(Time.shortLocalTime() + ": " + message);
	}
	
	public static class LogMessage {
		String message;
		String stacktrace;
		public LogMessage(String message, Throwable tr) {
			this.message = message;
			if (tr != null)
				this.stacktrace = Log.getStackTraceString(tr);
		}		
	}
	
}
