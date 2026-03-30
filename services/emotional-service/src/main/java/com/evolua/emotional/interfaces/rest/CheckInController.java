package com.evolua.emotional.interfaces.rest;

import com.evolua.emotional.application.CheckInService;
import com.evolua.emotional.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/check-ins")
public class CheckInController {
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("id", "mood", "energyLevel", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final CheckInService service;
  private final CheckInMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public CheckInController(CheckInService service, CheckInMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create CheckIn")
  public ResponseEntity<ApiResponse<CheckInResponse>> create(@Valid @RequestBody CheckInRequest request) {
    var created =
        service.create(
            currentUserProvider.getCurrentUser().userId(),
            request.mood(),
            request.reflection(),
            request.energyLevel(),
            request.recommendedPractice());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping
  @Operation(summary = "List check-ins")
  public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String mood,
      @RequestParam(required = false) String energyRange,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var normalizedMood = normalize(mood);
    var normalizedEnergyRange = normalize(energyRange);
    var fromInstant = parseStartDate(from);
    var toInstant = parseEndDate(to);
    validateDateRange(fromInstant, toInstant);
    var energyBounds = parseEnergyRange(normalizedEnergyRange);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (normalizedMood != null) {
      filters.put("mood", normalizedMood);
    }
    if (normalizedEnergyRange != null) {
      filters.put("energyRange", normalizedEnergyRange);
    }
    if (from != null && !from.isBlank()) {
      filters.put("from", from.trim());
    }
    if (to != null && !to.isBlank()) {
      filters.put("to", to.trim());
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            normalizedMood,
            energyBounds[0],
            energyBounds[1],
            fromInstant,
            toInstant);

    return ResponseEntity.ok(
        ApiResponse.success(
            200,
            "Listed",
            PageResponse.from(
                result,
                mapper::toResponse,
                query.effectiveSortBy(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
                query.normalizedSortDir(),
                filters)));
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private Instant parseStartDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return LocalDate.parse(value.trim()).atStartOfDay().toInstant(ZoneOffset.UTC);
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("from must use yyyy-MM-dd format");
    }
  }

  private Instant parseEndDate(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return LocalDate.parse(value.trim()).plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("to must use yyyy-MM-dd format");
    }
  }

  private void validateDateRange(Instant from, Instant to) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("from must be before or equal to to");
    }
  }

  private Integer[] parseEnergyRange(String energyRange) {
    if (energyRange == null) {
      return new Integer[] {null, null};
    }

    return switch (energyRange.toLowerCase()) {
      case "low" -> new Integer[] {1, 3};
      case "medium" -> new Integer[] {4, 7};
      case "high" -> new Integer[] {8, 10};
      default -> throw new IllegalArgumentException("energyRange must be one of: low, medium, high");
    };
  }
}
