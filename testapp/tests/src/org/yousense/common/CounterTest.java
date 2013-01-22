package org.yousense.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import org.yousense.common.Counter;

import java.lang.String;

public class CounterTest extends AndroidTestCase {

    Context context;
    long first;

    public void setUp() {
        context = getContext();
        first = Counter.PERSIST_INTERVAL * 2 + 1;
        Counter.resetAll(context);
    }

    public void testInitialValuesStartFromTwicePersistInterval() {
        assertEquals(first, Counter.getNext(context, "foo"));
        assertEquals(first + 1, Counter.getNext(context, "foo"));
        assertEquals(first + 2, Counter.getNext(context, "foo"));
    }

    public void testCountersAreIndependent() {
        assertEquals(first, Counter.getNext(context, "foo"));
        assertEquals(first, Counter.getNext(context, "bar"));
        assertEquals(first, Counter.getNext(context, "asd"));
        assertEquals(first + 1, Counter.getNext(context, "foo"));
        assertEquals(first + 1, Counter.getNext(context, "bar"));
        assertEquals(first + 1, Counter.getNext(context, "asd"));
    }

    public void testValueIsStoredEveryPersistInterval() {
        // PERSIST_INTERVAL - 1 steps
        for (int i = 1; i < Counter.PERSIST_INTERVAL; ++i)
            assertEquals(Counter.PERSIST_INTERVAL * 2 + i, Counter.getNext(context, "foo"));

        // Still initial value stored
        assertEquals(Counter.PERSIST_INTERVAL * 2, storedValue("foo"));

        // One more step stores new value
        assertEquals(Counter.PERSIST_INTERVAL * 3, Counter.getNext(context, "foo"));
        assertEquals(Counter.PERSIST_INTERVAL * 3, storedValue("foo"));
    }

    public void testValueIsJumpedAndStoredOnLoad() {
        assertEquals(first, Counter.getNext(context, "foo"));
        assertEquals(Counter.PERSIST_INTERVAL * 2, storedValue("foo"));

        Counter.clearCache(context, "foo");
        assertEquals(Counter.PERSIST_INTERVAL * 4 + 1, Counter.getNext(context, "foo"));
        assertEquals(Counter.PERSIST_INTERVAL * 4, storedValue("foo"));
    }

    public long storedValue(String name) {
        SharedPreferences prefs = context.getSharedPreferences(Counter.PREFS_FILE, 0);
        assertTrue(prefs.contains(name));
        return prefs.getLong(name, 0);
    }
}
