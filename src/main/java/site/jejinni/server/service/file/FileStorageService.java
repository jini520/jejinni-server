package site.jejinni.server.service.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {

  private final Map<FileType, Path> fileStorageLocations;

  public static class FileInfo {
    private final UUID id;
    private final String extension;
    private final String originalFileName;

    public FileInfo(UUID id, String extension) {
      this.id = id;
      this.extension = extension != null ? extension : "";
      this.originalFileName = null;
    }

    public FileInfo(UUID id, String extension, String originalFileName) {
      this.id = id;
      this.extension = extension != null ? extension : "";
      this.originalFileName = originalFileName;
    }

    public UUID getId() {
      return id;
    }

    public String getExtension() {
      return extension;
    }

    public String getOriginalFileName() {
      return originalFileName;
    }
  }

  public FileStorageService(
      @Value("${file.upload-dir.images:/var/lib/jejinni-server/uploads/images}") String imageUploadDir,
      @Value("${file.upload-dir.documents:/var/lib/jejinni-server/uploads/documents}") String documentUploadDir,
      @Value("${file.upload-dir.resumes:/var/lib/jejinni-server/uploads/resumes}") String resumeUploadDir,
      @Value("${file.upload-dir.portfolios:/var/lib/jejinni-server/uploads/portfolios}") String portfolioUploadDir) {

    this.fileStorageLocations = new HashMap<>();

    Path imageLocation = Paths.get(imageUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(imageLocation);
      this.fileStorageLocations.put(FileType.IMAGE, imageLocation);
    } catch (IOException ex) {
      throw new RuntimeException("이미지 저장 디렉토리를 생성할 수 없습니다: " + imageUploadDir, ex);
    }

    Path documentLocation = Paths.get(documentUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(documentLocation);
      this.fileStorageLocations.put(FileType.DOCUMENT, documentLocation);
    } catch (IOException ex) {
      throw new RuntimeException("문서 저장 디렉토리를 생성할 수 없습니다: " + documentUploadDir, ex);
    }

    Path resumeLocation = Paths.get(resumeUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(resumeLocation);
      this.fileStorageLocations.put(FileType.RESUME, resumeLocation);
    } catch (IOException ex) {
      throw new RuntimeException("이력서 저장 디렉토리를 생성할 수 없습니다: " + resumeUploadDir, ex);
    }

    Path portfolioLocation = Paths.get(portfolioUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(portfolioLocation);
      this.fileStorageLocations.put(FileType.PORTFOLIO, portfolioLocation);
    } catch (IOException ex) {
      throw new RuntimeException("포트폴리오 저장 디렉토리를 생성할 수 없습니다: " + portfolioUploadDir, ex);
    }
  }

  private Path getStorageLocation(FileType fileType) {
    Path location = fileStorageLocations.get(fileType);
    if (location == null) {
      throw new IllegalArgumentException("지원하지 않는 파일 타입입니다: " + fileType);
    }
    return location;
  }

  public FileInfo storeFile(MultipartFile file, FileType fileType) {
    return storeFile(file, fileType, null);
  }

  public FileInfo storeFile(MultipartFile file, FileType fileType, UUID fileId) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
    }

    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    if (fileId == null) {
      fileId = UUID.randomUUID();
    }
    String fileName = fileId.toString() + fileExtension;
    String originalName = originalFilename != null ? originalFilename : "";

    try {
      Path storageLocation = getStorageLocation(fileType);
      Path targetLocation = storageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return new FileInfo(fileId, fileExtension, originalName);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 저장할 수 없습니다: " + fileName, ex);
    }
  }

  /**
   * 프로젝트 콘텐츠용 이미지를 저장합니다.
   * 저장 경로: [imageUploadDir]/[projectId]/[fileId].[extension]
   */
  public FileInfo storeImageInProjectDir(MultipartFile file, UUID projectId) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
    }

    String originalFilename = file.getOriginalFilename();
    String fileExtension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    UUID fileId = UUID.randomUUID();
    String fileName = fileId.toString() + fileExtension;
    String originalName = originalFilename != null ? originalFilename : "";

    try {
      Path imageRoot = getStorageLocation(FileType.IMAGE);
      Path projectDir = imageRoot.resolve(projectId.toString());
      Files.createDirectories(projectDir);
      Path targetLocation = projectDir.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return new FileInfo(fileId, fileExtension, originalName);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 저장할 수 없습니다: " + fileName, ex);
    }
  }

  /**
   * 프로젝트 콘텐츠 이미지를 리소스로 로드합니다.
   */
  public Resource loadProjectImageAsResource(UUID projectId, UUID fileId) {
    String extension = getProjectImageExtension(projectId, fileId);
    String fileName = fileId.toString() + (extension != null ? extension : "");
    try {
      Path projectDir = getStorageLocation(FileType.IMAGE).resolve(projectId.toString());
      Path filePath = projectDir.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      }
      throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
    } catch (Exception ex) {
      throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName, ex);
    }
  }

  public String getProjectImageExtension(UUID projectId, UUID fileId) {
    try {
      Path projectDir = getStorageLocation(FileType.IMAGE).resolve(projectId.toString());
      if (!Files.isDirectory(projectDir)) {
        return "";
      }
      try (Stream<Path> paths = Files.list(projectDir)) {
        String idString = fileId.toString();
        return paths
            .filter(Files::isRegularFile)
            .filter(path -> path.getFileName().toString().startsWith(idString))
            .findFirst()
            .map(path -> {
              String name = path.getFileName().toString();
              int lastDot = name.lastIndexOf(".");
              return lastDot > 0 ? name.substring(lastDot) : "";
            })
            .orElse("");
      }
    } catch (IOException ex) {
      return "";
    }
  }

  public Resource loadFileAsResource(UUID id, FileType fileType) {
    String extension = getFileExtension(id, fileType);
    String fileName = id.toString() + (extension != null ? extension : "");
    try {
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      if (resource.exists()) {
        return resource;
      } else {
        throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
      }
    } catch (Exception ex) {
      throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName, ex);
    }
  }

  public boolean fileExists(UUID id, FileType fileType) {
    try {
      String extension = getFileExtension(id, fileType);
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      return Files.exists(filePath);
    } catch (Exception ex) {
      return false;
    }
  }

  /**
   * @deprecated 파일 크기는 이제 DB에서 조회합니다. FileService.getFileInfo()를 사용하세요.
   */
  @Deprecated
  public long getFileSize(UUID id, FileType fileType) {
    throw new UnsupportedOperationException("파일 크기는 DB에서 조회해야 합니다. FileService.getFileInfo()를 사용하세요.");
  }

  /**
   * @deprecated 파일 생성일은 이제 DB에서 조회합니다. FileService.getFileInfo()를 사용하세요.
   */
  @Deprecated
  public LocalDateTime getFileCreatedAt(UUID id, FileType fileType) {
    throw new UnsupportedOperationException("파일 생성일은 DB에서 조회해야 합니다. FileService.getFileInfo()를 사용하세요.");
  }

  /**
   * @deprecated 파일 수정일은 이제 DB에서 조회합니다. FileService.getFileInfo()를 사용하세요.
   */
  @Deprecated
  public LocalDateTime getFileUpdatedAt(UUID id, FileType fileType) {
    throw new UnsupportedOperationException("파일 수정일은 DB에서 조회해야 합니다. FileService.getFileInfo()를 사용하세요.");
  }

  /**
   * @deprecated 파일 리스트는 이제 DB에서 조회합니다. FileService.getFileList()를 사용하세요.
   */
  @Deprecated
  public List<FileInfo> getFileList(FileType fileType, int page, int size) {
    throw new UnsupportedOperationException("파일 리스트는 DB에서 조회해야 합니다. FileService.getFileList()를 사용하세요.");
  }

  /**
   * @deprecated 파일 개수는 이제 DB에서 조회합니다. FileService.getFileList()를 사용하세요.
   */
  @Deprecated
  public long getFileCount(FileType fileType) {
    throw new UnsupportedOperationException("파일 개수는 DB에서 조회해야 합니다. FileService.getFileList()를 사용하세요.");
  }

  public FileInfo updateFile(UUID oldId, MultipartFile newFile, FileType fileType) {
    if (newFile.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
    }

    if (oldId != null) {
      // 기존 파일 삭제 (메타데이터 파일은 유지하지 않음)
      deleteFile(oldId, fileType);
      // 기존 ID를 재사용하여 새 파일 저장
      return storeFile(newFile, fileType, oldId);
    }
    return storeFile(newFile, fileType);
  }

  /**
   * @deprecated 원본 파일명은 이제 DB에서 가져와야 합니다. FileService.getFileInfo()를 사용하세요.
   */
  @Deprecated
  public String getOriginalFileName(UUID id, FileType fileType) {
    // DB에서 가져와야 하므로 더 이상 파일 시스템에서 읽지 않음
    return null;
  }

  public String getFileExtension(UUID id, FileType fileType) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      // UUID로 시작하는 파일 찾기
      try (Stream<Path> paths = Files.list(storageLocation)) {
        String idString = id.toString();
        return paths
            .filter(Files::isRegularFile)
            .filter(path -> {
              String fileName = path.getFileName().toString();
              return fileName.startsWith(idString);
            })
            .findFirst()
            .map(path -> {
              String fileName = path.getFileName().toString();
              int lastDotIndex = fileName.lastIndexOf(".");
              if (lastDotIndex > 0) {
                return fileName.substring(lastDotIndex);
              }
              return "";
            })
            .orElse("");
      }
    } catch (IOException ex) {
      return "";
    }
  }

  public void deleteFile(UUID id, FileType fileType) {
    try {
      String extension = getFileExtension(id, fileType);
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 삭제할 수 없습니다: " + id, ex);
    }
  }

  /**
   * 프로젝트 콘텐츠 이미지를 파일 시스템에서 삭제합니다.
   */
  public void deleteProjectImage(UUID projectId, UUID fileId) {
    try {
      String extension = getProjectImageExtension(projectId, fileId);
      String fileName = fileId.toString() + (extension != null ? extension : "");
      Path projectDir = getStorageLocation(FileType.IMAGE).resolve(projectId.toString());
      Path filePath = projectDir.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new RuntimeException("프로젝트 이미지를 삭제할 수 없습니다: " + fileId, ex);
    }
  }

  /**
   * 프로젝트에 업로드된 모든 이미지 디렉터리를 삭제합니다.
   * [imageUploadDir]/[projectId]/ 하위 파일 및 디렉터리 전체 삭제.
   */
  public void deleteProjectImageDir(UUID projectId) {
    try {
      Path projectDir = getStorageLocation(FileType.IMAGE).resolve(projectId.toString());
      if (!Files.exists(projectDir) || !Files.isDirectory(projectDir)) {
        return;
      }
      try (Stream<Path> walk = Files.walk(projectDir)) {
        walk.sorted(Comparator.reverseOrder())
            .forEach(path -> {
              try {
                Files.delete(path);
              } catch (IOException ex) {
                throw new RuntimeException("프로젝트 이미지 디렉터리를 삭제할 수 없습니다: " + projectId, ex);
              }
            });
      }
    } catch (IOException ex) {
      throw new RuntimeException("프로젝트 이미지 디렉터리를 삭제할 수 없습니다: " + projectId, ex);
    }
  }
}
