package org.yousense.eventlog;

import android.os.Build;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.yousense.common.*;
import org.yousense.upload.UploadService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class EventLogAndGzipServiceTest extends AndroidTestCase {

    public static final String FUNNY = "xIñtërnâtiônàlizætiønx";

    File logdir;
    File uploaddir;

    public static class TestData {
        String key = FUNNY;
    }

    public void testExternalStorageWritable() {
        File external = getContext().getExternalFilesDir(null);
        assertNotNull(external);
        assertTrue(external.exists());
        assertTrue(external.isDirectory());
        assertTrue(external.canRead());
        assertTrue(external.canWrite());
        assertTrue(external.canExecute());
    }

    public void setUp() throws IOException {
        EventLog.init(null);
        EventLog.resetWriterAndDeleteLogDirectory(getContext());
        UploadService.deleteUploadDirectory(getContext());
        logdir = EventLog.getLogDirectory(getContext());
        uploaddir = UploadService.getUploadDirectory(getContext());
    }

    public String suffixes(File[] files) throws IOException {
        ArrayList<String> strs = new ArrayList<String>();
        for (File file : files) {
            strs.add(Files.getSuffix(file));
        }
        return StringUtils.join(strs, ", ");
    }

    public void assertNoFiles(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            MoreAsserts.assertEmpty(suffixes(files), Arrays.asList(files));
        }
    }

    public File assertOneFile(File dir) throws IOException {
        File[] files = dir.listFiles();
        assertNotNull(files);
        assertEquals(suffixes(files), 1, files.length);
        return files[0];
    }

    public void assertOneEmptyOpenFile(File dir) throws IOException {
        File file = assertOneFile(dir);
        assertEquals(0, file.length());
        assertTrue(file.getAbsolutePath().endsWith(".open"));
    }

    public File assertOneFlushedOpenFile(File dir) throws IOException {
        File file = assertOneFile(dir);
        assertTrue(file.length() > 0);
        assertTrue(file.getAbsolutePath().endsWith(".open"));
        return file;
    }

    public File assertOneGzippedLogFile(File dir) throws IOException {
        File file = assertOneFile(dir);
        assertTrue(file.length() > 0);
        assertTrue(file.getAbsolutePath().endsWith(".log.gz"));
        return file;
    }

    public void assertTwoGzippedLogFiles(File dir) throws IOException {
        File[] files = dir.listFiles();
        assertNotNull(files);
        assertEquals(suffixes(files), 2, files.length);
        for (File file : files) {
            assertTrue(file.length() > 0);
            assertTrue(file.getAbsolutePath().endsWith(".log.gz"));
        }
    }

    public String getContentsNoWhitespace(File file, boolean gzipped) throws IOException {
        String contents;
        if (gzipped)
            contents = Gzip.readToString(file);
        else
            contents = FileUtils.readFileToString(file, Files.UTF8);
        // Strip all spaces and newlines (even within strings) to remove potential pretty-printing.
        contents = contents.replace(" ", "");
        contents = contents.replace("\n", "");
        return contents;
    }

    public void assertContainsCount(String fileContents, String substring, int count) {
        assertEquals(count, StringUtils.countMatches(fileContents, substring));
    }

    public void assertHeader(String fileContents) {
        assertContainsCount(fileContents, "\"tag\":\"header\"", 1);
        assertContainsCount(fileContents, "\"appid\":\"" + AppId.appId(getContext()) + "\"", 1);
        assertContainsCount(fileContents, "\"userid\":\"" + UserId.userId(getContext()) + "\"", 1);
        assertContainsCount(fileContents, "\"app_version_code\":" + AppId.versionCode(getContext()), 1);
        assertContainsCount(fileContents, "\"build\":{", 1);
        assertContainsCount(fileContents, "\"device\":\"", 1);
        assertContainsCount(fileContents, "\"os_sdk\":" + Build.VERSION.SDK_INT, 1);
    }

    public void assertHeaderAndEvents(String fileContents, String tag, String dataValue, int count) {
        assertHeader(fileContents);
        assertContainsCount(fileContents, "\"tag\":\"" + tag + "\",", count);
        if (dataValue != null) {
            assertContainsCount(fileContents, "\"data\":{\"key\":\"" + dataValue + "\"}", count);
        } else {
            assertContainsCount(fileContents, "\"data\":{", 1);  // Only Header data
        }
    }

    public void testNoInitAppendWritesNothing() throws IOException {
        EventLog.append("test", null);
        assertNoFiles(logdir);
        assertNoFiles(uploaddir);
    }

    public void testNoInitRotateFails() throws IOException {
        try {
            EventLog.append("test", null);
            EventLog.rotateAndStartGzip();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertNoFiles(logdir);
        assertNoFiles(uploaddir);
    }

    public void testNoInitRotateAndUploadFails() throws IOException {
        try {
            EventLog.append("test", null);
            EventLog.rotateAndStartGzipAndUpload();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
        }
        assertNoFiles(logdir);
        assertNoFiles(uploaddir);
    }

    public void testInitDoesNotWrite() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        assertNoFiles(logdir);
        assertNoFiles(uploaddir);
    }

    public void testAppendIsFlushedToOpenFile() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.append(FUNNY, null);
        assertNoFiles(uploaddir);
        File open = assertOneFlushedOpenFile(logdir);
        String contents = getContentsNoWhitespace(open, false);
        assertHeaderAndEvents(contents, FUNNY, null, 1);
    }

    public void testTwoAppends() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.append("test", null);
        EventLog.append("test", null);
        assertNoFiles(uploaddir);
        File open = assertOneFlushedOpenFile(logdir);
        String contents = getContentsNoWhitespace(open, false);
        assertHeaderAndEvents(contents, "test", null, 2);
    }

    public void testAppendWithData() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.append("test", new TestData());
        assertNoFiles(uploaddir);
        File open = assertOneFlushedOpenFile(logdir);
        String contents = getContentsNoWhitespace(open, false);
        assertHeaderAndEvents(contents, "test", FUNNY, 1);
    }

    public void testAppendRotate() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.append(FUNNY, null);
        EventLog.rotateAndStartGzip();
        Time.sleepIgnoreInterrupt(2000);
        assertOneEmptyOpenFile(logdir);
        File gzipped = assertOneGzippedLogFile(uploaddir);
        String contents = getContentsNoWhitespace(gzipped, true);
        assertHeaderAndEvents(contents, FUNNY, null, 1);
    }

    public void testRotateTwice() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.append("test", null);
        EventLog.rotateAndStartGzip();
        EventLog.append("test", null);
        EventLog.rotateAndStartGzip();
        Time.sleepIgnoreInterrupt(3000);
        assertOneEmptyOpenFile(logdir);
        assertTwoGzippedLogFiles(uploaddir);
    }

    public void testRotateOmitsEmptyFiles() throws IOException {
        EventLog.init(getContext().getApplicationContext());
        EventLog.rotateAndStartGzip();
        EventLog.rotateAndStartGzip();
        EventLog.rotateAndStartGzip();
        EventLog.append("test", null);
        EventLog.rotateAndStartGzip();
        EventLog.rotateAndStartGzip();
        EventLog.rotateAndStartGzip();
        Time.sleepIgnoreInterrupt(3000);
        assertOneEmptyOpenFile(logdir);
        assertOneGzippedLogFile(uploaddir);
    }

}
