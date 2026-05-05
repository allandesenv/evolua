package com.evolua.user.application;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AvatarStorageService {
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

  private final Path rootPath;

  public AvatarStorageService(
      @Value("${app.profile.avatar-storage-path:./data/avatars}") String rootPath) {
    this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
  }

  public String store(String userId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Avatar file is required");
    }

    var extension = extractExtension(file.getOriginalFilename());
    try {
      Files.createDirectories(rootPath);
      var fileName = userId + "-" + UUID.randomUUID() + extension;
      var destination = rootPath.resolve(fileName).normalize();
      if (!destination.startsWith(rootPath)) {
        throw new IllegalArgumentException("Invalid avatar path");
      }
      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      return fileName;
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to store avatar", exception);
    }
  }

  public Resource loadAsResource(String fileName) {
    try {
      var filePath = rootPath.resolve(fileName).normalize();
      if (!filePath.startsWith(rootPath)) {
        throw new IllegalArgumentException("Invalid avatar path");
      }
      var resource = new UrlResource(filePath.toUri());
      if (!resource.exists()) {
        throw new IllegalArgumentException("Avatar not found");
      }
      return resource;
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to read avatar", exception);
    }
  }

  public String contentType(Resource resource) {
    try {
      var contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
      return contentType == null ? "application/octet-stream" : contentType;
    } catch (Exception exception) {
      return "application/octet-stream";
    }
  }

  public void deleteForUser(String userId) {
    try {
      if (!Files.exists(rootPath)) {
        return;
      }
      try (var stream = Files.list(rootPath)) {
        stream
            .filter(path -> path.getFileName().toString().startsWith(userId + "-"))
            .forEach(
                path -> {
                  try {
                    Files.deleteIfExists(path);
                  } catch (IOException ignored) {
                    // Best-effort cleanup of local avatar files.
                  }
                });
      }
    } catch (IOException ignored) {
      // Best-effort cleanup of local avatar files.
    }
  }

  private String extractExtension(String originalFileName) {
    if (originalFileName == null || !originalFileName.contains(".")) {
      throw new IllegalArgumentException("Avatar extension is required");
    }

    var extension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("Unsupported avatar format");
    }
    return extension;
  }
}
