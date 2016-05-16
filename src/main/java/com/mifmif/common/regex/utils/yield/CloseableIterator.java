package com.mifmif.common.regex.utils.yield;

import java.util.Iterator;

public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
    void close();
}
