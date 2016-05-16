package com.mifmif.common.regex.utils.yield;

import java.util.Optional;

interface Completed<T> extends Message<T> {
    static <T> Completed<T> completed() {
        return () -> Optional.empty();
    }
}
