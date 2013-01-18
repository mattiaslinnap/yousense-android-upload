package org.yousense.eventlog.format;

/**
 * Interface for formatters that turn event objects into bytes. The bytes are appended to files.
 */
public interface Formatter {
    byte[] serialize(Object event);
}
