package com.evolua.notification.interfaces.rest;

import java.util.List;
import java.util.Map;

public record PageResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    String sortBy,
    String sortDir,
    Map<String, Object> filters) {}
