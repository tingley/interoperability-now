package com.globalsight.tip;

import java.io.IOException;

/**
 * Backing store that persists data to local temporary files.
 */
public class TempFileBackingStore extends FileSystemBackingStore {

    public TempFileBackingStore() throws IOException {
        super(FileUtil.createTempDir("tipp"));
    }
}
