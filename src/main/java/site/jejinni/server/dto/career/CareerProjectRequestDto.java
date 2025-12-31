package site.jejinni.server.dto.career;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CareerProjectRequestDto {
	private String startDate;
	private String endDate;
	private String company;
	private String department;
	private String position;
	private String[] skills;
	private Integer orderIndex;
}

