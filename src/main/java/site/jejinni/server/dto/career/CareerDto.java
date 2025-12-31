package site.jejinni.server.dto.career;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CareerDto {
	private UUID id;
	private String startDate;  // "YY.MM." 형식
	private String endDate;    // "YY.MM." 형식
	private String company;
	private String department;
	private String position;
	private String[] skills;
}

