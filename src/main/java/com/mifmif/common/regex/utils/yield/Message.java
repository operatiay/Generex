package com.mifmif.common.regex.utils.yield;

import java.util.Optional;

interface Message<T> {
        Optional<T> value();
        static <T> Message<T> message(T value) {
            return () -> Optional.of(value);
        }
}
