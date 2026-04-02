package com.evolua.chat.interfaces.rest;

import com.evolua.chat.application.MessageService;
import com.evolua.chat.infrastructure.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/messages")
public class MessageController {
  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "recipientId", "createdAt");
  private static final String DEFAULT_SORT_BY = "createdAt";

  private final MessageService service;
  private final MessageMapper mapper;
  private final CurrentUserProvider currentUserProvider;

  public MessageController(
      MessageService service, MessageMapper mapper, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.mapper = mapper;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  @Operation(summary = "Create Message")
  public ResponseEntity<ApiResponse<MessageResponse>> create(@Valid @RequestBody MessageRequest request) {
    var created =
        service.create(currentUserProvider.getCurrentUser().userId(), request.recipientId(), request.content());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(201, "Created", mapper.toResponse(created)));
  }

  @GetMapping
  @Operation(summary = "List messages")
  public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDir,
      @RequestParam(required = false) String recipientId) {
    var query = new PageQuery(page, size, search, sortBy, sortDir);
    var filters = new LinkedHashMap<String, Object>();
    if (!query.normalizedSearch().isBlank()) {
      filters.put("search", query.normalizedSearch());
    }
    if (recipientId != null && !recipientId.isBlank()) {
      filters.put("recipientId", recipientId.trim());
    }

    var result =
        service.list(
            currentUserProvider.getCurrentUser().userId(),
            query.pageable(ALLOWED_SORT_FIELDS, DEFAULT_SORT_BY),
            query.normalizedSearch(),
            recipientId == null ? null : recipientId.trim());

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
}
