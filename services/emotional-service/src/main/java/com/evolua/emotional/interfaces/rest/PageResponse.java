package com.evolua.emotional.interfaces.rest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.data.domain.Page;

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
    Map<String, Object> filters) {
  public static <T, R> PageResponse<R> from(
      Page<T> source,
      Function<T, R> mapper,
      String sortBy,
      String sortDir,
      Map<String, Object> filters) {
    return new PageResponse<>(
        source.getContent().stream().map(mapper).toList(),
        source.getNumber(),
        source.getSize(),
        source.getTotalElements(),
        source.getTotalPages(),
        source.hasNext(),
        source.hasPrevious(),
        sortBy,
        sortDir,
        filters);
  }
}
