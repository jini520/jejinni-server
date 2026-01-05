package site.jejinni.server.service.certification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.jejinni.server.domain.entity.certification.Award;
import site.jejinni.server.domain.entity.certification.Certification;
import site.jejinni.server.dto.certification.*;
import site.jejinni.server.dto.common.ApiResponse;
import site.jejinni.server.repository.certification.AwardRepository;
import site.jejinni.server.repository.certification.CertificationRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificationService {

	private final CertificationRepository certificationRepository;
	private final AwardRepository awardRepository;

	// ===== 전체 조회 =====
	public ApiResponse<CertificationsDto> getAllCertifications() {
		List<CertificationDto> certifications = certificationRepository.findAllByOrderByOrderIndexAsc()
				.stream()
				.map(CertificationDto::from)
				.collect(Collectors.toList());

		List<AwardDto> awards = awardRepository.findAllByOrderByOrderIndexAsc()
				.stream()
				.map(AwardDto::from)
				.collect(Collectors.toList());

		CertificationsDto certificationsDto = CertificationsDto.builder()
				.certifications(certifications)
				.awards(awards)
				.build();

		return new ApiResponse<>(certificationsDto);
	}

	// ===== Certification CRUD =====
	@Transactional
	public ApiResponse<CertificationDto> createCertification(CertificationRequestDto dto) {
		Certification certification = Certification.builder()
				.name(dto.getName())
				.date(dto.getDate())
				.organization(dto.getOrganization())
				.tier(dto.getTier())
				.orderIndex(dto.getOrderIndex())
				.build();

		Certification saved = certificationRepository.save(certification);
		return new ApiResponse<>(CertificationDto.from(saved));
	}

	public ApiResponse<CertificationDto> getCertificationById(UUID id) {
		Certification certification = certificationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Certification not found: " + id));
		return new ApiResponse<>(CertificationDto.from(certification));
	}

	@Transactional
	public ApiResponse<CertificationDto> updateCertification(UUID id, CertificationRequestDto dto) {
		Certification certification = certificationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Certification not found: " + id));

		certification.updateName(dto.getName());
		certification.updateDate(dto.getDate());
		certification.updateOrganization(dto.getOrganization());
		certification.updateTier(dto.getTier());
		certification.updateOrderIndex(dto.getOrderIndex());

		return new ApiResponse<>(CertificationDto.from(certification));
	}

	@Transactional
	public void deleteCertification(UUID id) {
		certificationRepository.deleteById(id);
	}

	// ===== Award CRUD =====
	@Transactional
	public ApiResponse<AwardDto> createAward(AwardRequestDto dto) {
		Award award = Award.builder()
				.name(dto.getName())
				.date(dto.getDate())
				.organization(dto.getOrganization())
				.tier(dto.getTier())
				.orderIndex(dto.getOrderIndex())
				.build();

		Award saved = awardRepository.save(award);
		return new ApiResponse<>(AwardDto.from(saved));
	}

	public ApiResponse<AwardDto> getAwardById(UUID id) {
		Award award = awardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Award not found: " + id));
		return new ApiResponse<>(AwardDto.from(award));
	}

	@Transactional
	public ApiResponse<AwardDto> updateAward(UUID id, AwardRequestDto dto) {
		Award award = awardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Award not found: " + id));

		award.updateName(dto.getName());
		award.updateDate(dto.getDate());
		award.updateOrganization(dto.getOrganization());
		award.updateTier(dto.getTier());
		award.updateOrderIndex(dto.getOrderIndex());

		return new ApiResponse<>(AwardDto.from(award));
	}

	@Transactional
	public void deleteAward(UUID id) {
		awardRepository.deleteById(id);
	}
}

