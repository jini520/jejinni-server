package site.jejinni.server.dto.certification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AwardRequestDto {
	private String name;
	private String date;
	private String organization;
	private String tier;
	private Integer orderIndex;
}

