package site.jejinni.server.dto.skill;

import lombok.Getter;

import java.util.UUID;

@Getter
public class SkillRequestDto {

	private String name;
	private UUID categoryId;
	private Integer order;
}

