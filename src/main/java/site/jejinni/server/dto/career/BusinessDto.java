package site.jejinni.server.dto.career;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.jejinni.server.domain.entity.career.Business;

@Getter
@SuperBuilder
@NoArgsConstructor
public class BusinessDto extends CareerDto {
	private String[] details;

	public static BusinessDto from(Business business) {
		return BusinessDto.builder()
				.id(business.getId())
				.startDate(business.getStartDate())
				.endDate(business.getEndDate())
				.company(business.getCompany())
				.department(business.getDepartment())
				.position(business.getPosition())
				.skills(business.getSkills())
				.details(business.getDetails())
				.build();
	}
}

