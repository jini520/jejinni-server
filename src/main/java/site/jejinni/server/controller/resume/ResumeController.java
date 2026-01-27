package site.jejinni.server.controller.resume;

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
import site.jejinni.server.service.file.FileService;
import site.jejinni.server.service.file.FileStorageService;
import site.jejinni.server.service.file.FileType;

import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

  private final FileService fileService;
  private final FileStorageService fileStorageService;
  private static final FileType FILE_TYPE = FileType.RESUME;

  /**
   * 이력서 리스트 조회 (Read)
   * GET /api/resumes?page=0&size=10
   */
  @GetMapping(produces = "application/json")
  public ResponseEntity<ApiResponse<FileListDto>> getResumeList(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    FileListDto fileListDto = fileService.getFileList(FILE_TYPE, page, size);
    return ResponseEntity.ok(new ApiResponse<>(fileListDto));
  }

  /**
   * 이력서 업로드 (Create)
   * POST /api/resumes/upload
   */
  @PostMapping("/upload")
  public ResponseEntity<ApiResponse<FileDto>> uploadResume(
      @RequestParam("file") MultipartFile file) {

    FileDto fileDto = fileService.uploadFile(file, FILE_TYPE);
    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 이력서 다운로드 (Read)
   * GET /api/resumes/download/{id}
   */
  @GetMapping("/download/{id}")
  public ResponseEntity<Resource> downloadResume(@PathVariable UUID id) {
    Resource resource = fileStorageService.loadFileAsResource(id, FILE_TYPE);

    // 원본 파일명 가져오기 (DB에서 조회)
    String originalFileName = fileService.getFileInfo(id, FILE_TYPE).getOriginalFileName();
    String resourceFilename = resource.getFilename();

    // Content-Disposition 헤더 생성 (공통 유틸리티 사용)
    String contentDispositionValue = fileService.createContentDispositionHeader(originalFileName, resourceFilename);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionValue)
        .body(resource);
  }

  /**
   * 이력서 정보 조회 (Read)
   * GET /api/resumes/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<FileDto>> getResumeInfo(@PathVariable UUID id) {
    FileDto fileDto = fileService.getFileInfo(id, FILE_TYPE);
    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 이력서 업데이트 (Update)
   * PUT /api/resumes/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<FileDto>> updateResume(
      @PathVariable UUID id,
      @RequestParam("file") MultipartFile file) {

    FileDto fileDto = fileService.updateFile(id, file, FILE_TYPE);
    return ResponseEntity.ok(new ApiResponse<>(fileDto));
  }

  /**
   * 이력서 삭제 (Delete)
   * DELETE /api/resumes/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteResume(@PathVariable UUID id) {
    fileService.deleteFile(id, FILE_TYPE);
    return ResponseEntity.ok(new ApiResponse<>("이력서가 삭제되었습니다: " + id));
  }
}
