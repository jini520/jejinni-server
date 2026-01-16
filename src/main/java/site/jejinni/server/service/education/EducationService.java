package site.jejinni.server.service.education;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.jejinni.server.domain.entity.education.Education;
import site.jejinni.server.dto.education.EducationDto;
import site.jejinni.server.dto.education.EducationRequestDto;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.repository.education.EducationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EducationService {

	private final EducationRepository educationRepository;

	// 전체 조회
	public ApiResponse<List<EducationDto>> getAllEducations() {
		List<EducationDto> educations = educationRepository.findAllByOrderByOrderIndexAsc()
				.stream()
				.map(EducationDto::from)
				.collect(Collectors.toList());

		return new ApiResponse<>(educations);
	}

	// CRUD
	@Transactional
	public ApiResponse<EducationDto> createEducation(EducationRequestDto dto) {
		Education education = Education.builder()
				.education(dto.getEducation())
				.startDate(dto.getStartDate())
				.endDate(dto.getEndDate())
				.status(dto.getStatus())
				.description(dto.getDescription())
				.orderIndex(dto.getOrderIndex())
				.build();

		Education saved = educationRepository.save(education);
		return new ApiResponse<>(EducationDto.from(saved));
	}

	public ApiResponse<EducationDto> getEducationById(UUID id) {
		Education education = educationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Education not found: " + id));
		return new ApiResponse<>(EducationDto.from(education));
	}

	@Transactional
	public ApiResponse<EducationDto> updateEducation(UUID id, EducationRequestDto dto) {
		Education education = educationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Education not found: " + id));

		education.updateEducation(dto.getEducation());
		education.updateStartDate(dto.getStartDate());
		education.updateEndDate(dto.getEndDate());
		education.updateStatus(dto.getStatus());
		education.updateDescription(dto.getDescription());
		education.updateOrderIndex(dto.getOrderIndex());

		return new ApiResponse<>(EducationDto.from(education));
	}

	@Transactional
	public void deleteEducation(UUID id) {
		educationRepository.deleteById(id);
	}
}
