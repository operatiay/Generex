package com.mifmif.common.regex.utils.yield;

import java.util.Optional;

public class IfAbsent {
    public static <T> Then<T> ifAbsent(Optional<T> optional) {
        return runnable -> {
            if (!optional.isPresent()) {
                runnable.run();
            }
        };
    }
}
