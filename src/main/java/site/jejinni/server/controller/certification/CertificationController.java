package site.jejinni.server.controller.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.jejinni.server.dto.certification.*;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.service.certification.CertificationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController {

	private final CertificationService certificationService;

	// 전체 조회
	@GetMapping
	public ResponseEntity<ApiResponse<CertificationsDto>> getAllCertifications() {
		ApiResponse<CertificationsDto> certifications = certificationService.getAllCertifications();
		return ResponseEntity.ok(certifications);
	}

	// ===== Certification 엔드포인트 =====
	@PostMapping("/certifications")
	public ResponseEntity<ApiResponse<CertificationDto>> createCertification(@RequestBody CertificationRequestDto dto) {
		ApiResponse<CertificationDto> created = certificationService.createCertification(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/certifications/{id}")
	public ResponseEntity<ApiResponse<CertificationDto>> getCertificationById(@PathVariable UUID id) {
		ApiResponse<CertificationDto> certification = certificationService.getCertificationById(id);
		return ResponseEntity.ok(certification);
	}

	@PutMapping("/certifications/{id}")
	public ResponseEntity<ApiResponse<CertificationDto>> updateCertification(
			@PathVariable UUID id,
			@RequestBody CertificationRequestDto dto) {
		ApiResponse<CertificationDto> updated = certificationService.updateCertification(id, dto);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/certifications/{id}")
	public ResponseEntity<Void> deleteCertification(@PathVariable UUID id) {
		certificationService.deleteCertification(id);
		return ResponseEntity.noContent().build();
	}

	// ===== Award 엔드포인트 =====
	@PostMapping("/awards")
	public ResponseEntity<ApiResponse<AwardDto>> createAward(@RequestBody AwardRequestDto dto) {
		ApiResponse<AwardDto> created = certificationService.createAward(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@GetMapping("/awards/{id}")
	public ResponseEntity<ApiResponse<AwardDto>> getAwardById(@PathVariable UUID id) {
		ApiResponse<AwardDto> award = certificationService.getAwardById(id);
		return ResponseEntity.ok(award);
	}

	@PutMapping("/awards/{id}")
	public ResponseEntity<ApiResponse<AwardDto>> updateAward(
			@PathVariable UUID id,
			@RequestBody AwardRequestDto dto) {
		ApiResponse<AwardDto> updated = certificationService.updateAward(id, dto);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/awards/{id}")
	public ResponseEntity<Void> deleteAward(@PathVariable UUID id) {
		certificationService.deleteAward(id);
		return ResponseEntity.noContent().build();
	}
}

