package site.jejinni.server.dto.certification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CertificationRequestDto {
	private String name;
	private String date;
	private String organization;
	private String tier;
	private Integer orderIndex;
}

