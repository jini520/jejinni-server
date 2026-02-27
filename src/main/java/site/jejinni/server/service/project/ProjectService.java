package site.jejinni.server.service.project;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.jejinni.server.domain.entity.project.Project;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.dto.project.ProjectDetailDto;
import site.jejinni.server.dto.project.ProjectListItemDto;
import site.jejinni.server.dto.project.ProjectListDto;
import site.jejinni.server.dto.project.ProjectRequestDto;
import site.jejinni.server.repository.project.ProjectRepository;
import site.jejinni.server.service.file.FileStorageService;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final FileStorageService fileStorageService;

	public ApiResponse<ProjectListDto> getProjectList(Pageable pageable) {
		Page<Project> projects = projectRepository.findAllByOrderByOrderAsc(pageable);
		Page<ProjectListItemDto> dtoPage = projects.map(this::toListDto);

		ProjectListDto data = ProjectListDto.builder()
				.items(dtoPage.getContent())
				.totalPages(dtoPage.getTotalPages())
				.totalElements(dtoPage.getTotalElements())
				.size(dtoPage.getSize())
				.number(dtoPage.getNumber())
				.first(dtoPage.isFirst())
				.last(dtoPage.isLast())
				.build();

		return new ApiResponse<>(data);
	}

	@Transactional
	public ApiResponse<ProjectDetailDto> createProject(ProjectRequestDto request) {
		Project project = Project.builder()
				.title(request.getTitle())
				.description(request.getDescription())
				.skills(request.getSkills())
				.participants(request.getParticipants())
				.period(request.getPeriod())
				.contentImageUrls(request.getContentImageUrls())
				.contents(request.getContents())
				.order(request.getOrder())
				.build();

		project = projectRepository.save(project);

		ProjectDetailDto data = ProjectDetailDto.builder()
				.id(project.getId())
				.title(project.getTitle())
				.description(project.getDescription())
				.skills(project.getSkills())
				.participants(project.getParticipants())
				.period(project.getPeriod())
				.contentImageUrls(project.getContentImageUrls())
				.contents(project.getContents())
				.order(project.getOrder())
				.build();

		return new ApiResponse<>(data);
	}

	public ApiResponse<ProjectDetailDto> getProjectDetail(UUID projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

		return new ApiResponse<>(buildDetailResponse(project));
	}

	@Transactional
	public ApiResponse<ProjectDetailDto> updateProject(UUID projectId, ProjectRequestDto request) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

		if (request.getTitle() != null) {
			project.updateTitle(request.getTitle());
		}
		if (request.getDescription() != null) {
			project.updateDescription(request.getDescription());
		}
		if (request.getSkills() != null) {
			project.updateSkills(request.getSkills());
		}
		if (request.getParticipants() != null) {
			project.updateParticipants(request.getParticipants());
		}
		if (request.getPeriod() != null) {
			project.updatePeriod(request.getPeriod());
		}
		if (request.getContentImageUrls() != null) {
			project.updateContentImageUrls(request.getContentImageUrls());
		}
		if (request.getContents() != null) {
			project.updateContents(request.getContents());
		}
		if (request.getOrder() != null) {
			project.updateOrder(request.getOrder());
		}

		return new ApiResponse<>(buildDetailResponse(project));
	}

	/**
	 * 프로젝트를 삭제합니다. 업로드된 이미지 디렉터리와 DB 레코드를 모두 삭제합니다.
	 */
	@Transactional
	public void deleteProject(UUID projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

		fileStorageService.deleteProjectImageDir(projectId);
		projectRepository.delete(project);
	}

	/**
	 * 프로젝트의 contentImageUrls에 이미지 URL 한 개를 추가합니다.
	 */
	@Transactional
	public ApiResponse<ProjectDetailDto> addContentImageUrl(UUID projectId, String newImageUrl) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

		String[] existing = project.getContentImageUrls();
		if (existing == null) {
			existing = new String[0];
		}
		String[] updated = Arrays.copyOf(existing, existing.length + 1);
		updated[existing.length] = newImageUrl;
		project.updateContentImageUrls(updated);

		return new ApiResponse<>(buildDetailResponse(project));
	}

	/**
	 * 프로젝트의 contentImageUrls에서 해당 이미지 URL을 제거하고 파일을 삭제합니다.
	 */
	@Transactional
	public ApiResponse<ProjectDetailDto> removeContentImageUrl(UUID projectId, UUID fileId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

		String targetUrl = "/api/projects/" + projectId + "/images/" + fileId;
		String[] existing = project.getContentImageUrls();
		if (existing == null) {
			existing = new String[0];
		}
		String[] updated = Arrays.stream(existing)
				.filter(url -> !Objects.equals(url, targetUrl))
				.toArray(String[]::new);

		if (updated.length == existing.length) {
			throw new IllegalArgumentException("Image not found in project: " + fileId);
		}

		project.updateContentImageUrls(updated);
		fileStorageService.deleteProjectImage(projectId, fileId);

		return new ApiResponse<>(buildDetailResponse(project));
	}

	private ProjectDetailDto buildDetailResponse(Project project) {
		ProjectDetailDto data = ProjectDetailDto.builder()
				.id(project.getId())
				.title(project.getTitle())
				.description(project.getDescription())
				.skills(project.getSkills())
				.participants(project.getParticipants())
				.period(project.getPeriod())
				.contentImageUrls(project.getContentImageUrls())
				.contents(project.getContents())
				.order(project.getOrder())
				.build();

		return data;
	}

	private ProjectListItemDto toListDto(Project project) {
		return ProjectListItemDto.builder()
				.id(project.getId())
				.title(project.getTitle())
				.description(project.getDescription())
				.skills(project.getSkills())
				.period(project.getPeriod())
				.order(project.getOrder())
				.build();
	}
}
