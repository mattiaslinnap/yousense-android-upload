package org.yousense.common;

import android.test.AndroidTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;


public class FilesTest extends AndroidTestCase {

    File dir;

    public void setUp() throws IOException {
        dir = Files.getInternalSubdir(getContext(), "filestest");
        // Delete it to ensure files are deleted, and recreate again.
        FileUtils.deleteDirectory(dir);
        dir = Files.getInternalSubdir(getContext(), "filestest");
    }

    public void makeFiles(String... filenames) throws IOException {
        for (String filename : filenames) {
            FileUtils.write(new File(dir, filename), filename);
        }
    }

    public void assertFiles(String... filenamesAndContents) throws IOException {
        assertEquals("assertFiles() requires an even number of arguments.", 0, filenamesAndContents.length % 2);
        File[] actual = Files.listFilesSorted(dir, null);
        ArrayList<String> actualNames = new ArrayList<String>();
        for (File file : actual)
            actualNames.add(file.getName());
        assertEquals("Invalid number of files found: " + StringUtils.join(actualNames, ", "), filenamesAndContents.length / 2, actual.length);
        for (int i = 0; i < filenamesAndContents.length; i += 2) {
            String filename = filenamesAndContents[i];
            String contents = filenamesAndContents[i + 1];
            assertEquals(filename, actual[i / 2].getName());
            assertEquals(contents, FileUtils.readFileToString(actual[i / 2], Files.UTF8));
        }
    }

    public void assertSortedFilter(FileFilter filter, String... expected) throws IOException {
        File[] actual = Files.listFilesSorted(dir, filter);
        ArrayList<String> actualNames = new ArrayList<String>();
        for (File file : actual)
            actualNames.add(file.getName());
        assertEquals("Invalid number of files found: " + StringUtils.join(actualNames, ", "), expected.length, actual.length);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], actualNames.get(i));
        }
    }

    public void testListFilesSortedNoFilter() throws IOException {
        makeFiles("c.x", "b.y", "a.z");
        assertSortedFilter(null, "a.z", "b.y", "c.x");
    }

    public void testListFilesSortedNoFilterNoSuffixes() throws IOException {
        makeFiles("c", "b", "a");
        assertSortedFilter(null, "a", "b", "c");
    }

    public void testListFilesSortedMatchFilter() throws IOException {
        makeFiles("c.x", "b.y", "a.z", "c.z");
        assertSortedFilter(new Files.SuffixFilter(".z", true), "a.z", "c.z");
    }

    public void testListFilesSortedMatchFilterNoMatches() throws IOException {
        makeFiles("c.x", "b.y", "a.z", "c.z");
        assertSortedFilter(new Files.SuffixFilter(".foo", true));
    }

    public void testListFilesSortedMatchFilterNoFiles() throws IOException {
        assertSortedFilter(new Files.SuffixFilter(".foo", true));
    }

    public void testListFilesSortedNomatchFilter() throws IOException {
        makeFiles("c.x", "b.y", "a.z", "c.z");
        assertSortedFilter(new Files.SuffixFilter(".z", false), "b.y", "c.x");
    }

    public void testListFilesSortedNomatchFilterNoMatches() throws IOException {
        makeFiles("c.x", "b.x", "a.x");
        assertSortedFilter(new Files.SuffixFilter(".x", false));
    }

    public void testListFilesSortedNomatchFilterNoFiles() throws IOException {
        assertSortedFilter(new Files.SuffixFilter(".x", false));
    }

    public void testMoveAllFilesSortedSuffix() throws IOException {
        makeFiles("a.open", "b.open", "c.log");
        Files.moveAllFilesSortedSuffix(dir, ".open", ".log");
        assertFiles("a.log", "a.open", "b.log", "b.open", "c.log", "c.log");
    }

    public void testMoveAllFilesSortedSuffixNoMatches() throws IOException {
        makeFiles("a.open", "b.open", "c.log");
        Files.moveAllFilesSortedSuffix(dir, ".foo", ".log");
        assertFiles("a.open", "a.open", "b.open", "b.open", "c.log", "c.log");
    }

    public void testMoveAllFilesSortedSuffixManyTimes() throws IOException {
        makeFiles("a.open", "b.open", "c.log");
        Files.moveAllFilesSortedSuffix(dir, ".log", ".foo");
        Files.moveAllFilesSortedSuffix(dir, ".open", ".foo");
        Files.moveAllFilesSortedSuffix(dir, ".foo", ".bar");
        assertFiles("a.bar", "a.open", "b.bar", "b.open", "c.bar", "c.log");
    }

    public void testMoveAllFilesSortedSuffixOverwrites() throws IOException {
        makeFiles("a.open", "a.log");
        Files.moveAllFilesSortedSuffix(dir, ".open", ".log");
        assertFiles("a.log", "a.open");
    }

    public void testGetAbsolutePathWorksOnRelativeFiles() {
        assertEquals("/foo.bar", new File("foo.bar").getAbsolutePath());
    }

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
