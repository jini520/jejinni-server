package site.jejinni.server.dto.education;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequestDto {
	private String education;
	private String startDate;
	private String endDate;
	private String status;
	private String description;
	private Integer orderIndex;
}
