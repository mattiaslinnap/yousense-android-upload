package org.yousense.eventlog;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.FileUtils;
import org.yousense.common.Time;
import org.yousense.eventlog.data.Event;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class EventLogLatestCacheTest extends AndroidTestCase {

    // test persistence
    // test latest
    // test timesince
    // test non-persistence

    public static final String FUNNY = "xIñtërnâtiônàlizætiønx";

    public static class DataA {
        public DataA(String a) { this.a = a; }
        String a;
    }

    public static class DataB {
        public DataB(String b) { this.b = b; }
        String b;
    }

    public void setUp() throws IOException {
        EventLog.init(null, null, null);
        DebugLog.disableAppendToEventLog();
        EventLog.resetWriterAndDeleteLogDirectory(getContext());
    }

    public void testGetLatest() {
        EventLog.init(getContext(), null, null);
        EventLog.append("test", new DataA("asd"));
        EventLog.append("test", new DataA(FUNNY));
        assertEquals(FUNNY, ((Event<DataA>) EventLog.getLatest("test")).data.a);
    }

    public void testTimeSince() {
        EventLog.init(getContext(), null, null);
        EventLog.append("test", new DataA("asd"));
        Time.sleepIgnoreInterrupt(2000);
        long delay = EventLog.timeSince("test");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 3000);
        EventLog.append("test", new DataA(FUNNY));
        delay = EventLog.timeSince("test");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
    }

    public void testGetLatestUnknownKey() {
        EventLog.init(getContext(), null, null);
        assertNull(EventLog.getLatest("nothere"));
        EventLog.append("test", new DataA("asd"));
        assertNull(EventLog.getLatest("nothere"));
    }

    public void testTimeSinceUnknownKey() {
        EventLog.init(getContext(), null, null);
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("nothere"));
        EventLog.append("test", new DataA("asd"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("nothere"));
    }

    public void testPersistAll() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertEquals("asd", ((Event<DataA>)EventLog.getLatest("tag.a")).data.a);
        assertEquals("bar", ((Event<DataB>)EventLog.getLatest("tag.b")).data.b);
        long delay = EventLog.timeSince("tag.a");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
        delay = EventLog.timeSince("tag.b");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
    }

    public void testPersistNone() {
        EventLog.init(getContext(), null, null);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, null);
        assertNull(EventLog.getLatest("tag.a"));
        assertNull(EventLog.getLatest("tag.b"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("tag.a"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("tag.b"));
    }

    public void testPersistSome() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>() {
        }.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertEquals("asd", ((Event<DataA>)EventLog.getLatest("tag.a")).data.a);
        long delay = EventLog.timeSince("tag.a");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
        assertNull(EventLog.getLatest("tag.b"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("tag.b"));
    }

    public void testPersistLessDoesNotLoadOldKeys() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));

        // Remove key from persistence.
        types.remove("tag.a");
        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertEquals("bar", ((Event<DataB>)EventLog.getLatest("tag.b")).data.b);
        long delay = EventLog.timeSince("tag.b");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
        assertNull(EventLog.getLatest("tag.a"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("tag.a"));
    }

    public void testPersistMoreCannotLoadOldKeys() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));
        EventLog.init(null, null, null);

        // Add new key to persistence.
        types.put("tag.b", new TypeToken<Event<DataB>>() {}.getType());
        EventLog.init(getContext(), null, types);
        assertEquals("asd", ((Event<DataA>)EventLog.getLatest("tag.a")).data.a);
        long delay = EventLog.timeSince("tag.a");
        assertTrue("Unexpected delay " + delay + " milliseconds.", 0 <= delay && delay <= 1000);
        assertNull(EventLog.getLatest("tag.b"));
        assertEquals(LatestCache.INFINITY, EventLog.timeSince("tag.b"));
    }

    public void testFileMissing() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));
        EventLog.getLatestCacheFile(getContext()).delete();

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertNull(EventLog.getLatest("tag.a"));
        assertNull(EventLog.getLatest("tag.b"));
    }

    public void testNonJsonFile() throws IOException {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));
        FileUtils.write(EventLog.getLatestCacheFile(getContext()), "Trololo");

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertNull(EventLog.getLatest("tag.a"));
        assertNull(EventLog.getLatest("tag.b"));
    }

    public void testWrongJsonFile() throws IOException {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        EventLog.init(getContext(), null, types);
        EventLog.append("tag.a", new DataA("asd"));
        EventLog.append("tag.b", new DataB("bar"));
        FileUtils.write(EventLog.getLatestCacheFile(getContext()), "[\"asd\"]");

        EventLog.init(null, null, null);
        EventLog.init(getContext(), null, types);
        assertNull(EventLog.getLatest("tag.a"));
        assertNull(EventLog.getLatest("tag.b"));
    }
}
