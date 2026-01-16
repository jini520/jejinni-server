package site.jejinni.server.controller.education;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.jejinni.server.dto.education.EducationDto;
import site.jejinni.server.dto.education.EducationRequestDto;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.service.education.EducationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/edu")
@RequiredArgsConstructor
public class EducationController {

	private final EducationService educationService;

	// 전체 조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<EducationDto>>> getAllEducations() {
		ApiResponse<List<EducationDto>> educations = educationService.getAllEducations();
		return ResponseEntity.ok(educations);
	}

	// 생성
	@PostMapping
	public ResponseEntity<ApiResponse<EducationDto>> createEducation(@RequestBody EducationRequestDto dto) {
		ApiResponse<EducationDto> created = educationService.createEducation(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	// 단건 조회
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<EducationDto>> getEducationById(@PathVariable UUID id) {
		ApiResponse<EducationDto> education = educationService.getEducationById(id);
		return ResponseEntity.ok(education);
	}

	// 수정
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<EducationDto>> updateEducation(
			@PathVariable UUID id,
			@RequestBody EducationRequestDto dto) {
		ApiResponse<EducationDto> updated = educationService.updateEducation(id, dto);
		return ResponseEntity.ok(updated);
	}

	// 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEducation(@PathVariable UUID id) {
		educationService.deleteEducation(id);
		return ResponseEntity.noContent().build();
	}
}
