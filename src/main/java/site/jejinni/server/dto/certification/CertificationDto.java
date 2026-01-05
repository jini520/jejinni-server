package site.jejinni.server.dto.certification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.jejinni.server.domain.entity.certification.Certification;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDto {
	private UUID id;
	private String name;
	private String date;  // "YY.MM." 형식
	private String organization;
	private String tier;

	public static CertificationDto from(Certification certification) {
		return CertificationDto.builder()
				.id(certification.getId())
				.name(certification.getName())
				.date(certification.getDate())
				.organization(certification.getOrganization())
				.tier(certification.getTier())
				.build();
	}
}

