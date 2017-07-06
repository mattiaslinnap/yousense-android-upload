package org.yousense.eventlog;

import org.yousense.eventlog.data.Event;

public interface EventListener {
    /**
     * Called from EventLog on successfully written appends.
     */
    public void eventAdded(Event event);
}
