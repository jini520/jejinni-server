package site.jejinni.server.dto.career;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareersDto {
	private List<BusinessDto> businesses;
	private List<CareerProjectDto> projects;
}

