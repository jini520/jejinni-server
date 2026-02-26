package site.jejinni.server.controller.project;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.dto.project.ProjectDetailDto;
import site.jejinni.server.dto.project.ProjectListDto;
import site.jejinni.server.dto.project.ProjectRequestDto;
import site.jejinni.server.service.file.FileStorageService;
import site.jejinni.server.service.project.ProjectService;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

	private final ProjectService projectService;
	private final FileStorageService fileStorageService;

	@GetMapping
	public ResponseEntity<ApiResponse<ProjectListDto>> getProjectList(
			@PageableDefault(size = 10, sort = "order") Pageable pageable) {
		ApiResponse<ProjectListDto> projects = projectService.getProjectList(pageable);
		return ResponseEntity.ok(projects);
	}

	@PostMapping
	public ResponseEntity<ApiResponse<ProjectDetailDto>> createProject(
			@RequestBody ProjectRequestDto request) {
		ApiResponse<ProjectDetailDto> project = projectService.createProject(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(project);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProjectDetailDto>> getProjectDetail(@PathVariable UUID id) {
		ApiResponse<ProjectDetailDto> project = projectService.getProjectDetail(id);
		return ResponseEntity.ok(project);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProjectDetailDto>> updateProject(
			@PathVariable UUID id,
			@RequestBody ProjectRequestDto request) {
		ApiResponse<ProjectDetailDto> project = projectService.updateProject(id, request);
		return ResponseEntity.ok(project);
	}

	/**
	 * 프로젝트 콘텐츠용 이미지 업로드.
	 * 저장 경로: ./uploads/images/[project-uuid]/[file-uuid].[extension]
	 * 업로드 후 해당 프로젝트의 contentImageUrls에 URL이 추가됩니다.
	 */
	@PostMapping("/{id}/images")
	public ResponseEntity<ApiResponse<ProjectDetailDto>> uploadProjectImage(
			@PathVariable UUID id,
			@RequestParam("file") MultipartFile file) {

		FileStorageService.FileInfo fileInfo = fileStorageService.storeImageInProjectDir(file, id);
		String newImageUrl = "/api/projects/" + id + "/images/" + fileInfo.getId();

		ApiResponse<ProjectDetailDto> updated = projectService.addContentImageUrl(id, newImageUrl);
		return ResponseEntity.status(HttpStatus.CREATED).body(updated);
	}

	/**
	 * 프로젝트 콘텐츠 이미지 조회 (마크다운 등에서 참조하는 URL용).
	 */
	@GetMapping("/{projectId}/images/{fileId}")
	public ResponseEntity<Resource> getProjectImage(
			@PathVariable UUID projectId,
			@PathVariable UUID fileId) {
		Resource resource = fileStorageService.loadProjectImageAsResource(projectId, fileId);
		String ext = fileStorageService.getProjectImageExtension(projectId, fileId);
		MediaType mediaType = toMediaType(ext);
		return ResponseEntity.ok()
				.contentType(mediaType)
				.body(resource);
	}

	private static MediaType toMediaType(String extension) {
		if (extension == null) return MediaType.APPLICATION_OCTET_STREAM;
		return switch (extension.toLowerCase()) {
			case ".jpg", ".jpeg" -> MediaType.IMAGE_JPEG;
			case ".png" -> MediaType.IMAGE_PNG;
			case ".gif" -> MediaType.IMAGE_GIF;
			case ".webp" -> MediaType.parseMediaType("image/webp");
			default -> MediaType.APPLICATION_OCTET_STREAM;
		};
	}
}
