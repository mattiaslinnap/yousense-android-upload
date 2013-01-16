package org.yousense.deleteme;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.util.Base64;

public class Alarm {

	public String raw_data_base64;
	
	// Parsed data
	public int id;
    public boolean enabled;
    public int hour;
    public int minutes;
    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;
    public boolean sunday;
    public long time;
    public boolean vibrate;
    public String label;
    public String alert;
    public boolean silent;
	
    public Alarm(byte[] raw_data) {    	
    	this.raw_data_base64 = Base64.encodeToString(raw_data, Base64.NO_WRAP);
    }
    
    public static Alarm parseAlarmFromBinaryIntentDataOnGingerbread(Intent intent) {
    	byte[] data = intent.getByteArrayExtra("intent.extra.alarm_raw");
    	if (data != null) {
    		Alarm alarm = new Alarm(data);
    		
    		// Parse raw data as a Parcel
    		Parcel p = Parcel.obtain();
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
            
            alarm.id = p.readInt();
            alarm.enabled = p.readInt() == 1;
            alarm.hour = p.readInt();
            alarm.minutes = p.readInt();
            int daysOfWeek = p.readInt();
            alarm.time = p.readLong();
            alarm.vibrate = p.readInt() == 1;
            alarm.label = p.readString();
            Uri alertUri = (Uri)p.readParcelable(null);
            if (alertUri != null)
            	alarm.alert = alertUri.toString();
            else
            	alarm.alert = null;
            alarm.silent = p.readInt() == 1;
            
            // Parse daysOfWeek:
            alarm.monday = (daysOfWeek & 0x01) > 0;
            alarm.tuesday = (daysOfWeek & 0x02) > 0;
            alarm.wednesday = (daysOfWeek & 0x04) > 0;
            alarm.thursday = (daysOfWeek & 0x08) > 0;
            alarm.friday = (daysOfWeek & 0x10) > 0;
            alarm.saturday = (daysOfWeek & 0x20) > 0;
            alarm.sunday = (daysOfWeek & 0x40) > 0;
    		
    		return alarm;
    	} else {
    		return null;
    	}
    }
}
