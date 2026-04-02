package com.hotel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class GenericFilter {

    private GenericFilter() {
    }

    public static <T> List<T> filter(List<T> source, Predicate<T> predicate) {
        List<T> results = new ArrayList<>();
        for (T item : source) {
            if (predicate.test(item)) {
                results.add(item);
            }
        }
        return results;
    }
}
