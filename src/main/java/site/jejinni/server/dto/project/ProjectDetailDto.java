package site.jejinni.server.dto.project;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProjectDetailDto {

	private UUID id;
	private String title;
	private String description;
	private String[] skills;
	private Integer participants;
	private String period;
	private String[] contentImageUrls;
	private String contents;
	private Integer order;
}
