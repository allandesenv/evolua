package com.evolua.user.interfaces.rest;

import com.evolua.user.application.AvatarStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileAvatarController {
  private final AvatarStorageService avatarStorageService;

  public ProfileAvatarController(AvatarStorageService avatarStorageService) {
    this.avatarStorageService = avatarStorageService;
  }

  @GetMapping("/v1/public/profiles/avatar/{fileName:.+}")
  @Operation(summary = "Public avatar asset")
  public ResponseEntity<Resource> avatar(@PathVariable String fileName) {
    var resource = avatarStorageService.loadAsResource(fileName);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(avatarStorageService.contentType(resource)))
        .body(resource);
  }
}
