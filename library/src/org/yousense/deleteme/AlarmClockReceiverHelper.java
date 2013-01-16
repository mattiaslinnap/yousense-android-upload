package org.yousense.deleteme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receives alarm ringing events from the AlarmClock/DeskClock application.
 * Does not receive events for setting the alarm, or pressing the snooze/dismiss buttons.
 * 
 * Snoozing can be identified based on the flags in the raw alarm data.
 * Normal alarms can be set only on 1-minute boundaries, but snooze alarms are set with millisecond accuracy.
 * There are other potential flags that may identify a snooze-repeat alarm going off as well.
 * 
 * Add the following to AndroidManifest.xml:
 * <receiver android:name="your-receiver-class">
 *   <intent-filter>
 *     <action android:name="com.android.alarmclock.ALARM_ALERT" />
 *     <action android:name="com.android.deskclock.ALARM_ALERT" />
 *   </intent-filter>
 * </receiver>
 */
abstract public class AlarmClockReceiverHelper extends BroadcastReceiver {

	@Override
	public final void onReceive(Context context, Intent intent) {
		onAlarmIntent(context, intent);
		Alarm alarm = Alarm.parseAlarmFromBinaryIntentDataOnGingerbread(intent);
		// TODO: test this on various Android OS, SDK, manufacturer extensions, etc., and write other parsing methods. 
		onAlarmParsed(context, alarm);
	}

	/**
	 * It may be useful to log this, to notice cases where the alarm data parsing crashes on a new system.
	 */
	abstract public void onAlarmIntent(Context context, Intent intent);
	
	/**
	 * Alarm may be null if failed to parse binary data from intent.
	 */
	abstract public void onAlarmParsed(Context context, Alarm alarm);

}
