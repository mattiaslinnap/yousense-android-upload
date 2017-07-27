package org.yousense.common;

import android.os.Handler;

/**
 * Repeater class, useful wrapper for Runnables and Handlers for refreshing content in Activities.
 * Put your code into run(), and call start()/stop() in onResume() and onPause().
 */
public abstract class Repeater {

	Handler handler;
	long interval;
	boolean running;  // This is required to take care of the race condition of run() executing while calling stop().
	
	public Repeater(Handler handler, long intervalMillis) {
		this.handler = handler;
		this.interval = intervalMillis;
		this.running = false;
	}
	
	public synchronized void start() {
		running = true;
		handler.post(runnable);
	}
	
	public synchronized void stop() {
		running = false;
		handler.removeCallbacks(runnable);
	}
	
	Runnable runnable = new Runnable() {
		public void run() {
			Repeater.this.run();
			
			synchronized (Repeater.this) {
				if (running)
					handler.postDelayed(this, interval);
			}
		}
	};
	
	abstract public void run();
}
