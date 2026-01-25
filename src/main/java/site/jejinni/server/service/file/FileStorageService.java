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

  /**
   * 파일 정보를 담는 내부 클래스
   */
  public static class FileInfo {
    private final UUID id;
    private final String extension;
    private final String fileName; // id + extension 조합 (실제 저장된 파일명)

    public FileInfo(UUID id, String extension) {
      this.id = id;
      this.extension = extension != null ? extension : "";
      this.fileName = this.id.toString() + this.extension;
    }

    public UUID getId() {
      return id;
    }

    public String getExtension() {
      return extension;
    }

    public String getFileName() {
      return fileName;
    }
  }

  public FileStorageService(
      @Value("${file.upload-dir.images:/var/lib/jejinni-server/uploads/images}") String imageUploadDir,
      @Value("${file.upload-dir.documents:/var/lib/jejinni-server/uploads/documents}") String documentUploadDir) {

    this.fileStorageLocations = new HashMap<>();

    // 이미지 저장 경로 설정
    Path imageLocation = Paths.get(imageUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(imageLocation);
      this.fileStorageLocations.put(FileType.IMAGE, imageLocation);
    } catch (IOException ex) {
      throw new RuntimeException("이미지 저장 디렉토리를 생성할 수 없습니다: " + imageUploadDir, ex);
    }

    // 문서 저장 경로 설정
    Path documentLocation = Paths.get(documentUploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(documentLocation);
      this.fileStorageLocations.put(FileType.DOCUMENT, documentLocation);
    } catch (IOException ex) {
      throw new RuntimeException("문서 저장 디렉토리를 생성할 수 없습니다: " + documentUploadDir, ex);
    }
  }

  /**
   * 파일 타입에 따른 저장 경로 가져오기
   */
  private Path getStorageLocation(FileType fileType) {
    Path location = fileStorageLocations.get(fileType);
    if (location == null) {
      throw new IllegalArgumentException("지원하지 않는 파일 타입입니다: " + fileType);
    }
    return location;
  }

  /**
   * 파일 업로드 (Create)
   * 
   * @param file     업로드할 파일
   * @param fileType 파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 정보 (UUID와 extension 분리)
   */
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
    FileInfo fileInfo = new FileInfo(fileId, fileExtension);
    String fileName = fileInfo.getFileName();

    try {
      Path storageLocation = getStorageLocation(fileType);
      Path targetLocation = storageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      return fileInfo;
    } catch (IOException ex) {
      throw new RuntimeException("파일을 저장할 수 없습니다: " + fileName, ex);
    }
  }

  /**
   * 파일명에서 UUID와 extension 분리
   * 
   * @param fileName 파일명 (UUID + extension)
   * @return 파일 정보
   */
  public FileInfo parseFileName(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      throw new IllegalArgumentException("파일명이 비어있습니다.");
    }

    int lastDotIndex = fileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
      // 확장자가 없는 경우
      return new FileInfo(UUID.fromString(fileName), "");
    }

    String idString = fileName.substring(0, lastDotIndex);
    String extension = fileName.substring(lastDotIndex);

    return new FileInfo(UUID.fromString(idString), extension);
  }

  /**
   * 파일 다운로드 (Read) - ID와 extension으로 조회
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 리소스
   */
  public Resource loadFileAsResource(UUID id, String extension, FileType fileType) {
    String fileName = id.toString() + (extension != null ? extension : "");
    return loadFileAsResourceByFileName(fileName, fileType);
  }

  /**
   * 파일 다운로드 (Read) - 파일명으로 조회 (내부용)
   * 
   * @param fileName 파일명
   * @param fileType 파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 리소스
   */
  private Resource loadFileAsResourceByFileName(String fileName, FileType fileType) {
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

  /**
   * 파일 삭제 (Delete) - ID와 extension으로 삭제
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   */
  public void deleteFile(UUID id, String extension, FileType fileType) {
    String fileName = id.toString() + (extension != null ? extension : "");
    deleteFileByFileName(fileName, fileType);
  }

  /**
   * 파일 삭제 (Delete) - 파일명으로 삭제 (내부용)
   * 
   * @param fileName 파일명
   * @param fileType 파일 타입 (IMAGE 또는 DOCUMENT)
   */
  private void deleteFileByFileName(String fileName, FileType fileType) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      if (!Files.exists(filePath)) {
        throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
      }
      Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      throw new RuntimeException("파일을 삭제할 수 없습니다: " + fileName, ex);
    }
  }

  /**
   * 파일 업데이트 (Update) - 기존 파일 삭제 후 새 파일 저장
   * 
   * @param oldId        기존 파일 ID (UUID)
   * @param oldExtension 기존 확장자
   * @param newFile      새 파일
   * @param fileType     파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 새로 저장된 파일 정보
   */
  public FileInfo updateFile(UUID oldId, String oldExtension, MultipartFile newFile, FileType fileType) {
    if (newFile.isEmpty()) {
      throw new IllegalArgumentException("업로드할 파일이 비어있습니다.");
    }

    if (oldId != null) {
      deleteFile(oldId, oldExtension, fileType);
    }
    return storeFile(newFile, fileType);
  }

  /**
   * 파일 존재 여부 확인
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 존재 여부
   */
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

  /**
   * 파일 크기 조회
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 크기 (bytes)
   */
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

  /**
   * 파일 생성일 조회
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 생성일
   */
  public LocalDateTime getFileCreatedAt(UUID id, String extension, FileType fileType) {
    try {
      String fileName = id.toString() + (extension != null ? extension : "");
      Path storageLocation = getStorageLocation(fileType);
      Path filePath = storageLocation.resolve(fileName).normalize();
      FileTime creationTime = (FileTime) Files.getAttribute(filePath, "creationTime");
      if (creationTime == null) {
        // creationTime을 지원하지 않는 파일 시스템의 경우 lastModifiedTime 사용
        creationTime = Files.getLastModifiedTime(filePath);
      }
      return LocalDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
    } catch (IOException ex) {
      throw new RuntimeException("파일 생성일을 조회할 수 없습니다: " + id, ex);
    }
  }

  /**
   * 파일 수정일 조회
   * 
   * @param id        파일 ID (UUID)
   * @param extension 확장자
   * @param fileType  파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 수정일
   */
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

  /**
   * 파일 리스트 조회
   * 
   * @param fileType 파일 타입 (IMAGE 또는 DOCUMENT)
   * @param page     페이지 번호 (0부터 시작)
   * @param size     페이지 크기
   * @return 파일 정보 리스트
   */
  public List<FileInfo> getFileList(FileType fileType, int page, int size) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      List<FileInfo> fileList = new ArrayList<>();

      try (Stream<Path> paths = Files.list(storageLocation)) {
        List<Path> sortedPaths = paths
            .filter(Files::isRegularFile)
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

          // UUID와 extension 분리
          int lastDotIndex = fileName.lastIndexOf(".");
          if (lastDotIndex == -1) {
            // 확장자가 없는 경우
            UUID fileId = UUID.fromString(fileName);
            fileList.add(new FileInfo(fileId, ""));
          } else {
            String idString = fileName.substring(0, lastDotIndex);
            String extension = fileName.substring(lastDotIndex);
            UUID fileId = UUID.fromString(idString);
            fileList.add(new FileInfo(fileId, extension));
          }
        }
      }

      return fileList;
    } catch (IOException ex) {
      throw new RuntimeException("파일 리스트를 조회할 수 없습니다: " + fileType, ex);
    }
  }

  /**
   * 파일 총 개수 조회
   * 
   * @param fileType 파일 타입 (IMAGE 또는 DOCUMENT)
   * @return 파일 총 개수
   */
  public long getFileCount(FileType fileType) {
    try {
      Path storageLocation = getStorageLocation(fileType);
      try (Stream<Path> paths = Files.list(storageLocation)) {
        return paths.filter(Files::isRegularFile).count();
      }
    } catch (IOException ex) {
      throw new RuntimeException("파일 개수를 조회할 수 없습니다: " + fileType, ex);
    }
  }
}
