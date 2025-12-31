package site.jejinni.server.dto.career;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import site.jejinni.server.domain.entity.career.CareerProject;

@Getter
@SuperBuilder
@NoArgsConstructor
public class CareerProjectDto extends CareerDto {

	public static CareerProjectDto from(CareerProject project) {
		return CareerProjectDto.builder()
				.id(project.getId())
				.startDate(project.getStartDate())
				.endDate(project.getEndDate())
				.company(project.getCompany())
				.department(project.getDepartment())
				.position(project.getPosition())
				.skills(project.getSkills())
				.build();
	}
}

