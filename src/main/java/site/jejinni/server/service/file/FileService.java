package site.jejinni.server.service.file;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.jejinni.server.domain.entity.file.File;
import site.jejinni.server.dto.file.FileDto;
import site.jejinni.server.dto.file.FileListDto;
import site.jejinni.server.repository.file.FileRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

  private final FileRepository fileRepository;
  private final FileStorageService fileStorageService;
  private final EntityManager entityManager;

  /**
   * 파일 리스트 조회 (DB에서 조회)
   */
  @Transactional(readOnly = true)
  public FileListDto getFileList(FileType fileType, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<File> filePage = fileRepository.findByFileTypeOrderByCreatedAtDesc(fileType, pageable);

    List<FileDto> fileDtoList = filePage.getContent().stream()
        .map(file -> FileDto.builder()
            .id(file.getId())
            .originalFileName(file.getOriginalFileName())
            .fileType(file.getFileType())
            .fileSize(file.getFileSize())
            .contentType(file.getContentType())
            .downloadUrl(getDownloadUrl(file.getId(), fileType))
            .exists(true)
            .createdAt(file.getCreatedAt())
            .updatedAt(file.getUpdatedAt())
            .build())
        .collect(Collectors.toList());

    return FileListDto.builder()
        .items(fileDtoList)
        .totalPages(filePage.getTotalPages())
        .totalElements(filePage.getTotalElements())
        .size(filePage.getSize())
        .number(filePage.getNumber())
        .first(filePage.isFirst())
        .last(filePage.isLast())
        .build();
  }

  /**
   * 파일 업로드 (DB에 저장)
   */
  @Transactional
  public FileDto uploadFile(MultipartFile file, FileType fileType) {
    // 파일 시스템에 저장
    FileStorageService.FileInfo fileInfo = fileStorageService.storeFile(file, fileType);

    // DB에 저장 (ID는 파일 시스템에서 생성된 UUID 사용)
    // ID가 이미 존재하는지 확인 (동시 업로드 방지)
    if (fileRepository.existsById(fileInfo.getId())) {
      throw new RuntimeException("파일 ID가 이미 존재합니다: " + fileInfo.getId());
    }

    File fileEntity = File.builder()
        .originalFileName(fileInfo.getOriginalFileName())
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .fileType(fileType)
        .build();

    // 파일 시스템에서 생성된 UUID를 DB에도 사용
    // persist()를 사용하여 새 엔티티로 저장 (save()는 merge를 시도하므로 문제 발생)
    fileEntity.setId(fileInfo.getId());
    entityManager.persist(fileEntity);
    entityManager.flush(); // 즉시 DB에 반영하여 createdAt, updatedAt을 가져오기 위해

    return FileDto.builder()
        .id(fileEntity.getId())
        .originalFileName(fileEntity.getOriginalFileName())
        .fileSize(fileEntity.getFileSize())
        .contentType(fileEntity.getContentType())
        .fileType(fileEntity.getFileType())
        .downloadUrl(getDownloadUrl(fileEntity.getId(), fileType))
        .exists(true)
        .createdAt(fileEntity.getCreatedAt())
        .updatedAt(fileEntity.getUpdatedAt())
        .build();
  }

  /**
   * 파일 정보 조회 (DB에서 조회)
   */
  @Transactional(readOnly = true)
  public FileDto getFileInfo(UUID id, FileType fileType) {
    File file = fileRepository.findByIdAndFileType(id, fileType)
        .orElse(null);

    if (file == null) {
      return FileDto.builder()
          .id(id)
          .fileType(fileType)
          .exists(false)
          .build();
    }

    // 파일 시스템에서 실제 파일 존재 여부 확인 (최소화)
    boolean fileExists = fileStorageService.fileExists(id, fileType);

    return FileDto.builder()
        .id(file.getId())
        .originalFileName(file.getOriginalFileName())
        .fileType(file.getFileType())
        .fileSize(file.getFileSize())
        .contentType(file.getContentType())
        .downloadUrl(getDownloadUrl(file.getId(), fileType))
        .exists(fileExists)
        .createdAt(file.getCreatedAt())
        .updatedAt(file.getUpdatedAt())
        .build();
  }

  /**
   * 파일 업데이트 (DB 업데이트)
   */
  @Transactional
  public FileDto updateFile(UUID id, MultipartFile newFile, FileType fileType) {
    File existingFile = fileRepository.findByIdAndFileType(id, fileType)
        .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + id));

    // 파일 시스템에서 기존 파일 삭제 및 새 파일 저장
    FileStorageService.FileInfo newFileInfo = fileStorageService.updateFile(id, newFile, fileType);

    // DB 업데이트
    existingFile.updateFile(
        newFileInfo.getOriginalFileName(),
        newFile.getSize(),
        newFile.getContentType());

    File updatedFile = fileRepository.save(existingFile);

    return FileDto.builder()
        .id(updatedFile.getId())
        .originalFileName(updatedFile.getOriginalFileName())
        .fileSize(updatedFile.getFileSize())
        .contentType(updatedFile.getContentType())
        .fileType(updatedFile.getFileType())
        .downloadUrl(getDownloadUrl(updatedFile.getId(), fileType))
        .exists(true)
        .createdAt(updatedFile.getCreatedAt())
        .updatedAt(updatedFile.getUpdatedAt())
        .build();
  }

  /**
   * 파일 삭제 (DB에서 삭제)
   */
  @Transactional
  public void deleteFile(UUID id, FileType fileType) {
    File file = fileRepository.findByIdAndFileType(id, fileType)
        .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + id));

    // 파일 시스템에서 삭제
    fileStorageService.deleteFile(id, fileType);

    // DB에서 삭제
    fileRepository.delete(file);
  }

  /**
   * 다운로드 URL 생성
   */
  private String getDownloadUrl(UUID id, FileType fileType) {
    return switch (fileType) {
      case RESUME -> "/api/resumes/download/" + id;
      case PORTFOLIO -> "/api/portfolios/download/" + id;
      case IMAGE, DOCUMENT ->
        throw new IllegalArgumentException("IMAGE와 DOCUMENT 타입은 지원하지 않습니다. RESUME 또는 PORTFOLIO 타입을 사용하세요.");
    };
  }

  /**
   * 파일 다운로드를 위한 Content-Disposition 헤더 값 생성 (공통 유틸리티)
   */
  public String createContentDispositionHeader(String originalFileName, String resourceFilename) {
    if (originalFileName != null && !originalFileName.isEmpty()) {
      // 한글 및 특수문자 처리를 위해 UTF-8 인코딩 사용
      String encodedFileName = java.net.URLEncoder.encode(originalFileName, java.nio.charset.StandardCharsets.UTF_8)
          .replace("+", "%20"); // 공백 문자 처리
      // ASCII 문자만 포함된 경우 filename 파라미터도 추가, 그렇지 않으면 filename*만 사용
      boolean isAsciiOnly = originalFileName.matches("^[\\x00-\\x7F]*$");
      if (isAsciiOnly) {
        return String.format(
            "attachment; filename=\"%s\"; filename*=UTF-8''%s",
            originalFileName.replace("\"", "\\\""), // 따옴표 이스케이프
            encodedFileName);
      } else {
        // 한글이 포함된 경우 filename*만 사용
        return String.format("attachment; filename*=UTF-8''%s", encodedFileName);
      }
    } else {
      // originalFileName이 null이거나 비어있을 경우 resource filename 사용
      return "attachment; filename=\"" + resourceFilename + "\"";
    }
  }
}
