package site.jejinni.server.dto.education;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.jejinni.server.domain.entity.education.Education;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDto {
	private UUID id;
	private String education;
	private String startDate;  // "YY.MM." 형식
	private String endDate;    // "YY.MM." 형식
	private String status;
	private String description;

	public static EducationDto from(Education education) {
		return EducationDto.builder()
				.id(education.getId())
				.education(education.getEducation())
				.startDate(education.getStartDate())
				.endDate(education.getEndDate())
				.status(education.getStatus())
				.description(education.getDescription())
				.build();
	}
}
