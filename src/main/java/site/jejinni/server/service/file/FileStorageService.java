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
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
      @Value("${file.upload-dir.documents:/var/lib/jejinni-server/uploads/documents}") String documentUploadDir) {

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
  }

  private Path getStorageLocation(FileType fileType) {
    Path location = fileStorageLocations.get(fileType);
    if (location == null) {
      throw new IllegalArgumentException("지원하지 않는 파일 타입입니다: " + fileType);
    }
    return location;
  }

  public FileInfo storeFile(MultipartFile file, FileType fileType) {
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
      Path storageLocation = getStorageLocation(fileType);
      Path targetLocation = storageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      // 원본 파일명을 메타데이터 파일로 저장
      if (!originalName.isEmpty()) {
        Path metaFile = storageLocation.resolve(fileId.toString() + ".meta");
        Files.writeString(metaFile, originalName);
      }

      return new FileInfo(fileId, fileExtension, originalName);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 저장할 수 없습니다: " + fileName, ex);
    }
  }

  public Resource loadFileAsResource(UUID id, String extension, FileType fileType) {
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

  public boolean fileExists(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      return Files.exists(filePath);
    } catch (Exception ex) {
      return false;
    }
  }

  public long getFileSize(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      return Files.size(filePath);
    } catch (IOException ex) {
      throw new RuntimeException("파일 크기를 조회할 수 없습니다: " + id, ex);
    }
  }

  public LocalDateTime getFileCreatedAt(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      FileTime creationTime = (FileTime) Files.getAttribute(filePath, "creationTime");
      if (creationTime == null) {
        creationTime = Files.getLastModifiedTime(filePath);
      }
      return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
    } catch (IOException ex) {
      throw new RuntimeException("파일 생성일을 조회할 수 없습니다: " + id, ex);
    }
  }

  public LocalDateTime getFileUpdatedAt(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
      return LocalDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
    } catch (IOException ex) {
      throw new RuntimeException("파일 수정일을 조회할 수 없습니다: " + id, ex);
    }
  }

  public List<FileInfo> getFileList(FileType fileType, int page, int size) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      List<FileInfo> fileList = new ArrayList<>();

      try (Stream<Path> paths = Files.list(storageLocation)) {
        List<Path> sortedPaths = paths
            .filter(Files::isRegularFile)
            .filter(path -> !path.getFileName().toString().endsWith(".meta")) // 메타데이터 파일 제외
            .sorted((p1, p2) -> {
              try {
                return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
              } catch (IOException e) {
                return 0;
              }
            })
            .toList();

        int start = page * size;
        int end = Math.min(start + size, sortedPaths.size());

        for (int i = start; i < end; i++) {
          Path filePath = sortedPaths.get(i);
          String fileName = filePath.getFileName().toString();

          int lastDotIndex = fileName.lastIndexOf(".");
          UUID fileId;
          String extension;
          if (lastDotIndex == -1) {
            fileId = UUID.fromString(fileName);
            extension = "";
          } else {
            String idString = fileName.substring(0, lastDotIndex);
            extension = fileName.substring(lastDotIndex);
            fileId = UUID.fromString(idString);
          }

          // 메타데이터 파일에서 원본 파일명 읽기
          String originalFileName = null;
          try {
            Path metaFile = storageLocation.resolve(fileId.toString() + ".meta");
            if (Files.exists(metaFile)) {
              originalFileName = Files.readString(metaFile).trim();
            }
          } catch (IOException e) {
            // 메타데이터 파일 읽기 실패 시 무시
          }

          fileList.add(new FileInfo(fileId, extension, originalFileName));
        }
      }

      return fileList;
    } catch (IOException ex) {
      throw new RuntimeException("파일 리스트를 조회할 수 없습니다: " + fileType, ex);
    }
  }

  public long getFileCount(FileType fileType) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      try (Stream<Path> paths = Files.list(storageLocation)) {
        return paths
            .filter(Files::isRegularFile)
            .filter(path -> !path.getFileName().toString().endsWith(".meta")) // 메타데이터 파일 제외
            .count();
      }
    } catch (IOException ex) {
      throw new RuntimeException("파일 개수를 조회할 수 없습니다: " + fileType, ex);
    }
  }

  public FileInfo updateFile(UUID oldId, String oldExtension, MultipartFile newFile, FileType fileType) {
    if (newFile.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
    }

    if (oldId != null) {
      deleteFile(oldId, oldExtension, fileType);
    }
    return storeFile(newFile, fileType);
  }

  public String getOriginalFileName(UUID id, FileType fileType) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      Path metaFile = storageLocation.resolve(id.toString() + ".meta");
      if (Files.exists(metaFile)) {
        return Files.readString(metaFile).trim();
      }
      return null;
    } catch (IOException ex) {
      return null;
    }
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
              return fileName.startsWith(idString) && !fileName.endsWith(".meta");
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

  public void deleteFile(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      Files.deleteIfExists(filePath);

      // 메타데이터 파일도 삭제
      Path metaFile = storageLocation.resolve(id.toString() + ".meta");
      Files.deleteIfExists(metaFile);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 삭제할 수 없습니다: " + id, ex);
    }
  }
}
