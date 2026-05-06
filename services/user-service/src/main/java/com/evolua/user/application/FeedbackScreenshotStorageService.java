package com.evolua.user.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FeedbackScreenshotStorageService {
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

  private final Path rootPath;

  public FeedbackScreenshotStorageService(
      @Value("${app.feedback.screenshot-storage-path:./data/feedback-screenshots}") String rootPath) {
    this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
  }

  public String store(String userId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Captura de tela vazia.");
    }

    var extension = extractExtension(file.getOriginalFilename());
    try {
      Files.createDirectories(rootPath);
      var fileName = userId + "-" + UUID.randomUUID() + extension;
      var destination = rootPath.resolve(fileName).normalize();
      if (!destination.startsWith(rootPath)) {
        throw new IllegalArgumentException("Caminho de captura invalido.");
      }
      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
      return fileName;
    } catch (IOException exception) {
      throw new IllegalStateException("Nao foi possivel armazenar a captura.", exception);
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
                    // Best-effort cleanup of local feedback files.
                  }
                });
      }
    } catch (IOException ignored) {
      // Best-effort cleanup of local feedback files.
    }
  }

  private String extractExtension(String originalFileName) {
    if (originalFileName == null || !originalFileName.contains(".")) {
      throw new IllegalArgumentException("A captura precisa ter extensao.");
    }

    var extension = originalFileName.substring(originalFileName.lastIndexOf('.')).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException("Formato de captura nao suportado.");
    }
    return extension;
  }
}
