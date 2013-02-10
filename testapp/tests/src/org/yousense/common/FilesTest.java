package org.yousense.common;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;


public class FilesTest extends AndroidTestCase {


    public void testGetSuffix() throws IOException {
        assertEquals(".log", Files.getSuffix(new File("foo.log")));
        assertEquals(".temp", Files.getSuffix(new File("foo.log.gz.temp")));
        assertEquals(".temp", Files.getSuffix(new File("/bar/asd/foo.log.gz.temp")));
    }

    public void testGetSuffixInvalidFails() {
        String[] bads = {"", "foo.", "/foo/bar", "/foo/bar.asd/trololo", "/foo/bar.asd/trololo.", "foobar_-:asd123"};
        for (String bad : bads) {
            try {
                String suffix = Files.getSuffix(new File(bad));
                fail("Expected IOException, but got \"" + suffix + "\"");
            } catch (IOException e) {
            }
        }
    }

    public void testAppendSuffix() throws IOException {
        assertEquals("foo.log", Files.appendSuffix(new File("foo"), ".log").getName());
        assertEquals("foo.log", Files.appendSuffix(new File("/foo/bar/asd/foo"), ".log").getName());
        assertEquals("foo.log.gz.temp", Files.appendSuffix(new File("foo.log.gz"), ".temp").getName());
        assertEquals("/foo/bar/foo.log.gz.temp", Files.appendSuffix(new File("/foo/bar/foo.log.gz"), ".temp").getAbsolutePath());
    }

    public void testAppendSuffixFailsForRepeatedSuffix() {
        try {
            Files.appendSuffix(new File("foo.log"), ".log");
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    public void testReplaceSuffix() throws IOException {
        assertEquals("foo.log", Files.replaceSuffix(new File("foo.open"), ".log").getName());
        assertEquals("foo.log.bar", Files.replaceSuffix(new File("foo.log.gz"), ".bar").getName());
        assertEquals("/asd/foo/bar/foo.log.bar", Files.replaceSuffix(new File("/asd/foo/bar/foo.log.gz"), ".bar").getAbsolutePath());
    }

    public void testReplaceSuffixFailsForRepeatedSuffix() {
        try {
            Files.replaceSuffix(new File("foo.log"), ".log");
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }

    public void testCheckValidSuffix() throws IOException {
        Files.checkValidSuffix(".log");
        Files.checkValidSuffix(".open");
        Files.checkValidSuffix(".temp");
        Files.checkValidSuffix(".abcdefghij");
    }

    public void testCheckValidSuffixInvalidFails() {
        String[] bads = {null, "", ".", "log", ".abcdefghijk", ".foo12", ".foo bar"};
        for (String bad : bads) {
            try {
                Files.checkValidSuffix(bad);
                fail("Expected IOException");
            } catch (IOException e) {
            }
        }
    }
}
