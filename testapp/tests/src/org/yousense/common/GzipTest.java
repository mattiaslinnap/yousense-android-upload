package org.yousense.common;

import android.test.AndroidTestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class GzipTest extends AndroidTestCase {

    File dir;
    File original;
    File temp;
    File gzipped;

    @Override
    protected void setUp() throws Exception {
        dir = Files.getInternalSubdir(getContext(), "gziptest");
        original = new File(dir, "file");
        temp = new File(dir, "file.gz.temp");
        gzipped = new File(dir, "file.gz");
        FileUtils.write(original, "foobar");
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(dir);
    }

    public void testGzipFileContents() throws IOException {
        File result = Gzip.gzip(original);
        assertTrue(gzipped.exists());
        assertEquals("foobar", Gzip.readToString(result));
    }

    public void testReturnsGzipFilename() throws IOException {
        File result = Gzip.gzip(original);
        assertEquals(gzipped.getAbsolutePath(), result.getAbsolutePath());
    }

    public void testOriginalIsDeleted() throws IOException {
        Gzip.gzip(original);
        assertFalse(original.exists());
    }

    public void testTemporaryIsDeleted() throws IOException {
        Gzip.gzip(original);
        assertFalse(temp.exists());
    }


    public void testNoFileChangeIfGzipOpenFails() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void openGzip() throws IOException {
                    throw new IOException("Failing on purpose.");
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testNoFileChangeIfCopyFails() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void copy() throws IOException {
                    throw new IOException("Failing on purpose.");
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testNoFileChangeIfGzipCloseFails() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void closeGzip() throws IOException {
                    throw new IOException("Failing on purpose.");
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testNoFileChangeIfCompareFails() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void compare() throws IOException {
                    throw new IOException("Failing on purpose.");
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testNoFileChangeIfGzipStreamIsMalformed() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void compare() throws IOException {
                    FileUtils.write(temp, "invalid data");
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testNoFileChangeIfGzipStreamIsTruncated() throws IOException {
        try {
            Gzip.testableGzip(original, new Gzip.TestCallback() {
                void compare() throws IOException {
                    byte[] bytes = FileUtils.readFileToByteArray(temp);
                    bytes = Arrays.copyOf(bytes, bytes.length - 5);
                    FileUtils.writeByteArrayToFile(temp, bytes);
                }
            });
            fail("Expected IOException");
        } catch (IOException e) {
        }
        assertTrue(original.exists());
        assertFalse(temp.exists());
        assertFalse(gzipped.exists());
    }

    public void testGzipFileContentsIfFirstWriteIsMalformed() throws IOException {
        Gzip.TestCallback intermittent = new Gzip.TestCallback() {
            void compare() throws IOException {
                if (attempts == 0)
                    FileUtils.write(temp, "invalid data");
                ++attempts;
            }
        };
        File result = Gzip.testableGzip(original, intermittent);
        assertEquals(gzipped.getAbsolutePath(), result.getAbsolutePath());
        assertFalse(original.exists());
        assertFalse(temp.exists());
        assertTrue(gzipped.exists());
        assertEquals("foobar", Gzip.readToString(gzipped));
        assertEquals(2, intermittent.attempts);
    }
}
