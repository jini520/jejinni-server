package site.jejinni.server.dto.file;

import lombok.Builder;
import lombok.Getter;
import site.jejinni.server.service.file.FileType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class FileDto {

  private UUID id;
  private String originalFileName;
  private String extension;
  private Long fileSize;
  private String contentType;
  private FileType fileType;
  private String downloadUrl;
  private Boolean exists;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
