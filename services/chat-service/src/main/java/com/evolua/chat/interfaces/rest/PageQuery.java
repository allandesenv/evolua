package com.evolua.chat.interfaces.rest;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageQuery {
  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 50;

  private final int page;
  private final int size;
  private final String search;
  private final String sortBy;
  private final String sortDir;

  public PageQuery(Integer page, Integer size, String search, String sortBy, String sortDir) {
    this.page = page == null || page < 0 ? DEFAULT_PAGE : page;
    this.size = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    this.search = search == null ? "" : search.trim();
    this.sortBy = sortBy == null ? "" : sortBy.trim();
    this.sortDir = "asc".equalsIgnoreCase(sortDir) ? "asc" : "desc";
  }

  public Pageable pageable(Set<String> allowedSortFields, String defaultSortBy) {
    return PageRequest.of(page, size, Sort.by(direction(), effectiveSortBy(allowedSortFields, defaultSortBy)));
  }

  public String normalizedSearch() {
    return search;
  }

  public String effectiveSortBy(Set<String> allowedSortFields, String defaultSortBy) {
    return allowedSortFields.contains(sortBy) ? sortBy : defaultSortBy;
  }

  public String normalizedSortDir() {
    return sortDir;
  }

  private Sort.Direction direction() {
    return "asc".equals(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
  }
}
