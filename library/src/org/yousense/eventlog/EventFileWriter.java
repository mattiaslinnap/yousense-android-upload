package org.yousense.eventlog;

import android.content.Context;
import com.google.gson.Gson;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.yousense.common.*;
import org.yousense.eventlog.data.Event;
import org.yousense.eventlog.data.HeaderData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class EventFileWriter {

    private Gson gson;
    private Event pendingHeader;
    private BufferedWriter openWriter;

    public EventFileWriter(Context context) throws IOException {
        this.gson = new Gson();
        this.pendingHeader = new Event(context, "header", new HeaderData(context));
        this.openWriter = new BufferedWriter(new FileWriterWithEncoding(generateFilename(context, this.pendingHeader), Files.UTF8));
    }

    public synchronized void appendEvent(Context context, String tag, Object data) throws IOException {
        if (pendingHeader != null) {
            writeNullSeparatedJson(pendingHeader);
            pendingHeader = null;
        }
        writeNullSeparatedJson(new Event(context, tag, data));
    }

    public synchronized void close() throws IOException {
        openWriter.close();
    }

    private static File generateFilename(Context context, Event header) throws IOException {
        HeaderData hdata = (HeaderData)header.data;
        // Make sure filenames are generated in sorted filename order by zero-padding the counters.
        // Space is reserved for about 30 years (10k days) of operation:
        // 3 digits (1k) app version numbers.
        // 4 digits (10k) for number of restarts,
        // 6 digits (1M) for number of files,
        // 10 digits (100M) for number of events.
        // App version comes first, so that new filename formats will sort after the old ones.
        String filename = String.format("%s-v%03d-%s-r%04d-f%08d-e%010d-%s" + EventLog.OPEN_SUFFIX,
                hdata.appid,
                hdata.app_version_code,
                hdata.userid,
                hdata.counter_restart,
                hdata.counter_file,
                header.counter_event,
                Time.timestampNoSpacesWithMilliseconds(header.time_system));
        return new File(EventLog.getLogDirectory(), filename);
    }

    private void writeNullSeparatedJson(Object event) throws IOException {
        openWriter.write(0);
        gson.toJson(event, openWriter);
        openWriter.write('\n');
        openWriter.flush();
    }

}
