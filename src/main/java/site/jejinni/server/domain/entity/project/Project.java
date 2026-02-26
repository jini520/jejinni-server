package site.jejinni.server.domain.entity.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.jejinni.server.domain.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "project_id", columnDefinition = "UUID")
	private UUID id;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "skills", columnDefinition = "text[]")
	private String[] skills;

	@Column(name = "participants")
	private Integer participants;

	@Column(name = "period", length = 50)
	private String period;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "content_image_urls", columnDefinition = "text[]")
	private String[] contentImageUrls;

	@Column(name = "contents", columnDefinition = "TEXT")
	private String contents;

	@Column(name = "order_index", nullable = false)
	private Integer order;

	@Builder
	public Project(String title, String description, String[] skills, Integer participants, String period,
			String[] contentImageUrls, String contents, Integer order) {
		this.title = title;
		this.description = description;
		this.skills = skills != null ? skills : new String[0];
		this.participants = participants != null ? participants : 0;
		this.period = period;
		this.contentImageUrls = contentImageUrls != null ? contentImageUrls : new String[0];
		this.contents = contents;
		this.order = order != null ? order : 0;
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateSkills(String[] skills) {
		this.skills = skills != null ? skills : new String[0];
	}

	public void updateParticipants(Integer participants) {
		this.participants = participants != null ? participants : 0;
	}

	public void updatePeriod(String period) {
		this.period = period;
	}

	public void updateContentImageUrls(String[] contentImageUrls) {
		this.contentImageUrls = contentImageUrls != null ? contentImageUrls : new String[0];
	}

	public void updateContents(String contents) {
		this.contents = contents;
	}

	public void updateOrder(Integer order) {
		this.order = order != null ? order : 0;
	}
}
