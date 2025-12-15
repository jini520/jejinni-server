package site.jejinni.server.dto.skill;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SkillsDto {

	private List<CategoryDto> categories;
	private List<SkillDto> skills;
}

