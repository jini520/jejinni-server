package site.jejinni.server.domain.entity.career;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "career_projects")
@DiscriminatorValue("PROJECT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareerProject extends Career {

	@Builder
	public CareerProject(String startDate, String endDate, String company,
	                     String department, String position, String[] skills,
	                     Integer orderIndex) {
		super(startDate, endDate, company, department, position, skills, orderIndex);
	}
}

