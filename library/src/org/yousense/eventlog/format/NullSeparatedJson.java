package org.yousense.eventlog.format;

import android.util.Log;
import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;
import org.yousense.eventlog.EventLog;

import java.io.UnsupportedEncodingException;

public class NullSeparatedJson implements Formatter {

    Gson gson = new Gson();

    public byte[] serialize(Object event) {
        try {
            byte[] bytes = gson.toJson(event).getBytes(EventLog.UTF8);
            return ArrayUtils.add(bytes, 0, (byte)0);
        } catch (UnsupportedEncodingException e) {
            Log.e(EventLog.TAG, "Unable to serialise object to JSON and then bytes.");
        }
    }
}
