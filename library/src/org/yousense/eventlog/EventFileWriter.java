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

/**
 *
 */
public class EventFileWriter {

    private Gson gson;
    private Event pendingHeader;
    private File openFile;
    private BufferedWriter openWriter;

    public EventFileWriter(Context context) throws IOException {
        this.gson = new Gson();
        this.pendingHeader = new Event(context, "header", new HeaderData(context));
        this.openFile = generateFilename(context, this.pendingHeader);
        this.openWriter = new BufferedWriter(new FileWriterWithEncoding(this.openFile, Files.UTF8));
    }

    public File getOpenFile() {
        return openFile;
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
        // TODO: make sure filenames are generated in sorted order.
        String filename = String.format("%s-%s-r%d-f%d-e%d-%s",
                hdata.appid,
                hdata.userid,
                hdata.counter_restart,
                hdata.counter_file,
                header.counter_event,
                Time.timestampNoSpacesWithMilliseconds(header.time_system));
        return new File(EventLog.getOpenDirectory(context), filename);
    }

    private void writeNullSeparatedJson(Object event) throws IOException {
        openWriter.write(0);
        gson.toJson(event, openWriter);
        openWriter.write('\n');
        openWriter.flush();
    }

}
