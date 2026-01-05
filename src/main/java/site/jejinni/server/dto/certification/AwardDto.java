package site.jejinni.server.dto.certification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.jejinni.server.domain.entity.certification.Award;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardDto {
	private UUID id;
	private String name;
	private String date;  // "YY.MM." 형식
	private String organization;
	private String tier;

	public static AwardDto from(Award award) {
		return AwardDto.builder()
				.id(award.getId())
				.name(award.getName())
				.date(award.getDate())
				.organization(award.getOrganization())
				.tier(award.getTier())
				.build();
	}
}

