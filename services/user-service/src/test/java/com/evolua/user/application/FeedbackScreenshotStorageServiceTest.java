package com.evolua.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FeedbackScreenshotStorageServiceTest {
  @TempDir Path tempDir;

  @Test
  void storeShouldAcceptImageAndDeleteByUser() {
    var service = new FeedbackScreenshotStorageService(tempDir.toString());
    var file = new MockMultipartFile("screenshot", "bug.png", "image/png", new byte[] {1, 2, 3});

    var fileName = service.store("user-1", file);

    assertThat(fileName).startsWith("user-1-").endsWith(".png");
    assertThat(Files.exists(tempDir.resolve(fileName))).isTrue();

    service.deleteForUser("user-1");

    assertThat(Files.exists(tempDir.resolve(fileName))).isFalse();
  }

  @Test
  void storeShouldRejectUnsupportedImage() {
    var service = new FeedbackScreenshotStorageService(tempDir.toString());
    var file = new MockMultipartFile("screenshot", "notes.txt", "text/plain", new byte[] {1});

    assertThatThrownBy(() -> service.store("user-1", file))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Formato de captura nao suportado.");
  }
}
