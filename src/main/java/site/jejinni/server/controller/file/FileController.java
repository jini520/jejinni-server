package site.jejinni.server.controller.file;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.dto.file.FileDto;
import site.jejinni.server.dto.file.FileListDto;
import site.jejinni.server.service.file.FileStorageService;
import site.jejinni.server.service.file.FileType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

  private final FileStorageService fileStorageService;

  /**
   * 파일 리스트 조회 (Read)
   * GET /api/files?type=IMAGE&page=0&size=10 또는 ?type=DOCUMENT&page=0&size=10
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<ApiResponse<FileListDto>> getFileList(
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    List<FileStorageService.FileInfo> fileInfoList = fileStorageService.getFileList(type, page, size);
    long totalElements = fileStorageService.getFileCount(type);
    int totalPages = (int) Math.ceil((double) totalElements / size);

    List<FileDto> fileDtoList = fileInfoList.stream().map(fileInfo -> {
      try {
        long fileSize = fileStorageService.getFileSize(fileInfo.getId(), fileInfo.getExtension(), type);
        LocalDateTime createdAt = fileStorageService.getFileCreatedAt(fileInfo.getId(), fileInfo.getExtension(), type);
        LocalDateTime updatedAt = fileStorageService.getFileUpdatedAt(fileInfo.getId(), fileInfo.getExtension(), type);

        return FileDto.builder()
            .id(fileInfo.getId())
            .originalFileName(fileInfo.getOriginalFileName())
            .fileType(type)
            .fileSize(fileSize)
            .downloadUrl(
                "/api/files/download/" + fileInfo.getId() + "?type=" + type)
            .exists(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
      } catch (Exception ex) {
        // 파일 정보 조회 실패 시 기본 정보만 반환
        return FileDto.builder()
            .id(fileInfo.getId())
            .originalFileName(fileInfo.getOriginalFileName())
            .fileType(type)
            .downloadUrl(
                "/api/files/download/" + fileInfo.getId() + "?type=" + type)
            .exists(true)
            .build();
      }
    }).collect(Collectors.toList());

    FileListDto fileListDto = FileListDto.builder()
        .items(fileDtoList)
        .totalPages(totalPages)
        .totalElements(totalElements)
        .size(size)
        .number(page)
        .first(page == 0)
        .last(page >= totalPages - 1)
        .build();

    return ResponseEntity.ok(new ApiResponse<>(fileListDto));
  }

  /**
   * 파일 업로드 (Create)
   * POST /api/files/upload?type=IMAGE 또는 /api/files/upload?type=DOCUMENT
   */
  @PostMapping("/upload")
  public ResponseEntity<ApiResponse<FileDto>> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type) {

    FileStorageService.FileInfo fileInfo = fileStorageService.storeFile(file, type);

    // originalFileName: 전체 파일명 저장 (확장자 포함)
    String originalName = fileInfo.getOriginalFileName() != null ? fileInfo.getOriginalFileName() : "";
    String extension = fileInfo.getExtension();

    // 파일 생성일과 수정일 조회
    LocalDateTime createdAt = fileStorageService.getFileCreatedAt(fileInfo.getId(), extension, type);
    LocalDateTime updatedAt = fileStorageService.getFileUpdatedAt(fileInfo.getId(), extension, type);

    FileDto fileDto = FileDto.builder()
        .id(fileInfo.getId())
        .originalFileName(originalName)
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .fileType(type)
        .downloadUrl("/api/files/download/" + fileInfo.getId() + "?type=" + type)
        .exists(true)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 파일 다운로드 (Read)
   * GET /api/files/download/{id}?type=IMAGE 또는 ?type=DOCUMENT
   */
  @GetMapping("/download/{id}")
  @SuppressWarnings("null")
  public ResponseEntity<Resource> downloadFile(
      @PathVariable UUID id,
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type) {
    String extension = fileStorageService.getFileExtension(id, type);
    Resource resource = fileStorageService.loadFileAsResource(id, extension, type);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + resource.getFilename() + "\"")
        .body(resource);
  }

  /**
   * 파일 정보 조회 (Read)
   * GET /api/files/{id}?type=IMAGE 또는 ?type=DOCUMENT
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<FileDto>> getFileInfo(
      @PathVariable UUID id,
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type) {
    String extension = fileStorageService.getFileExtension(id, type);
    boolean exists = fileStorageService.fileExists(id, extension, type);

    String originalFileName = fileStorageService.getOriginalFileName(id, type);
    FileDto fileDto = FileDto.builder()
        .id(id)
        .originalFileName(originalFileName)
        .fileType(type)
        .exists(exists)
        .build();

    if (exists) {
      try {
        long fileSize = fileStorageService.getFileSize(id, extension, type);
        LocalDateTime createdAt = fileStorageService.getFileCreatedAt(id, extension, type);
        LocalDateTime updatedAt = fileStorageService.getFileUpdatedAt(id, extension, type);
        fileDto = FileDto.builder()
            .id(id)
            .originalFileName(originalFileName)
            .fileType(type)
            .fileSize(fileSize)
            .downloadUrl("/api/files/download/" + id + "?type=" + type)
            .exists(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
      } catch (Exception ex) {
        try {
          LocalDateTime createdAt = fileStorageService.getFileCreatedAt(id, extension, type);
          LocalDateTime updatedAt = fileStorageService.getFileUpdatedAt(id, extension, type);
          fileDto = FileDto.builder()
              .id(id)
              .originalFileName(originalFileName)
              .fileType(type)
              .exists(true)
              .downloadUrl("/api/files/download/" + id + "?type=" + type)
              .createdAt(createdAt)
              .updatedAt(updatedAt)
              .build();
        } catch (Exception dateEx) {
          fileDto = FileDto.builder()
              .id(id)
              .originalFileName(originalFileName)
              .fileType(type)
              .exists(true)
              .downloadUrl("/api/files/download/" + id + "?type=" + type)
              .build();
        }
      }
    }

    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 파일 업데이트 (Update)
   * PUT /api/files/{id}?type=IMAGE 또는 ?type=DOCUMENT
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<FileDto>> updateFile(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type) {

    String oldExtension = fileStorageService.getFileExtension(id, type);
    FileStorageService.FileInfo newFileInfo = fileStorageService.updateFile(id, oldExtension, file, type);

    // originalFileName: 전체 파일명 저장 (확장자 포함)
    String originalName = newFileInfo.getOriginalFileName() != null ? newFileInfo.getOriginalFileName() : "";
    String newExtension = newFileInfo.getExtension();

    // 파일 생성일과 수정일 조회
    LocalDateTime createdAt = fileStorageService.getFileCreatedAt(newFileInfo.getId(), newExtension, type);
    LocalDateTime updatedAt = fileStorageService.getFileUpdatedAt(newFileInfo.getId(), newExtension, type);

    FileDto fileDto = FileDto.builder()
        .id(newFileInfo.getId())
        .originalFileName(originalName)
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .fileType(type)
        .downloadUrl("/api/files/download/" + newFileInfo.getId() + "?type=" + type)
        .exists(true)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 파일 삭제 (Delete)
   * DELETE /api/files/{id}?type=IMAGE 또는 ?type=DOCUMENT
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteFile(
      @PathVariable UUID id,
      @RequestParam(value = "type", defaultValue = "DOCUMENT") FileType type) {
    String extension = fileStorageService.getFileExtension(id, type);
    fileStorageService.deleteFile(id, extension, type);
    return ResponseEntity.ok(new ApiResponse<>("파일이 삭제되었습니다: " + id));
  }
}
