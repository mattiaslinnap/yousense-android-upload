package org.yousense.common;

import android.test.AndroidTestCase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.yousense.eventlog.data.Event;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypedJsonTest extends AndroidTestCase {

    Gson gson = new Gson();

    public static class DataA {
        public DataA(String a) { this.a = a; }
        public String a;
    }

    public static class DataB {
        public DataB(String b) { this.b = b; }
        public String b;
    }

    public void testBasicTypes() {
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("int", new TypeToken<Integer>(){}.getType());
        types.put("str", new TypeToken<String>(){}.getType());
        types.put("bool", new TypeToken<Boolean>(){}.getType());
        Map<String, Object> parsed = TypedJson.fromJsonObjectTypedByKeys("{\"int\": 123, \"str\":\"asd\", \"bool\": true}", types);
        assertEquals(3, parsed.size());
        assertEquals(123, ((Integer)parsed.get("int")).intValue());
        assertEquals("asd", (String)parsed.get("str"));
        assertEquals(true, ((Boolean)parsed.get("bool")).booleanValue());
    }

    public void testEvents() {
        HashMap<String, Event> data = new HashMap<String, Event>();
        data.put("tag.a", new Event(getContext(), "tag.a", new DataA("asd")));
        data.put("tag.b", new Event(getContext(), "tag.b", new DataB("bar")));
        String fullJson = gson.toJson(data);
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        Map<String, Object> parsed = TypedJson.fromJsonObjectTypedByKeys(fullJson, types);
        assertEquals(2, parsed.size());
        assertEquals("asd", ((Event<DataA>)parsed.get("tag.a")).data.a);
        assertEquals("bar", ((Event<DataB>)parsed.get("tag.b")).data.b);
    }

    public void testEventWithNullData() {
        HashMap<String, Event> data = new HashMap<String, Event>();
        data.put("tag.a", new Event(getContext(), "tag.a", new DataA("asd")));
        data.put("tag.b", new Event(getContext(), "tag.b", null));
        data.put("tag.c", new Event(getContext(), "tag.c", null));
        String fullJson = gson.toJson(data);
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Event<DataB>>(){}.getType());
        types.put("tag.c", new TypeToken<Event>(){}.getType());
        Map<String, Object> parsed = TypedJson.fromJsonObjectTypedByKeys(fullJson, types);
        assertEquals(3, parsed.size());
        assertEquals("asd", ((Event<DataA>)parsed.get("tag.a")).data.a);
        assertNull(((Event<DataB>)parsed.get("tag.b")).data);
        assertNull(((Event)parsed.get("tag.c")).data);
    }

    public void testEventOmittedForBadType() {
        HashMap<String, Event> data = new HashMap<String, Event>();
        data.put("tag.a", new Event(getContext(), "tag.a", new DataA("asd")));
        data.put("tag.b", new Event(getContext(), "tag.b", new DataB("bar")));
        String fullJson = gson.toJson(data);
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Event<DataA>>(){}.getType());
        types.put("tag.b", new TypeToken<Integer>(){}.getType());  // NOTE bad type
        Map<String, Object> parsed = TypedJson.fromJsonObjectTypedByKeys(fullJson, types);
        assertEquals(1, parsed.size());
        assertEquals("asd", ((Event<DataA>)parsed.get("tag.a")).data.a);
    }

    public void testEmptyResultsForAllBadTypes() {
        HashMap<String, Event> data = new HashMap<String, Event>();
        data.put("tag.a", new Event(getContext(), "tag.a", new DataA("asd")));
        data.put("tag.b", new Event(getContext(), "tag.b", new DataB("bar")));
        String fullJson = gson.toJson(data);
        Map<String, Type> types = new HashMap<String, Type>();
        types.put("tag.a", new TypeToken<Integer>(){}.getType());  // NOTE bad type
        types.put("tag.b", new TypeToken<Integer>(){}.getType());  // NOTE bad type
        Map<String, Object> parsed = TypedJson.fromJsonObjectTypedByKeys(fullJson, types);
        assertEquals(0, parsed.size());
    }
}
