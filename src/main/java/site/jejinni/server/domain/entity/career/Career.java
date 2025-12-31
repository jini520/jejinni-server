package site.jejinni.server.domain.entity.career;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import site.jejinni.server.domain.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "careers")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "career_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Career extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "career_id", columnDefinition = "UUID")
	private UUID id;

	@Column(name = "start_date", nullable = false, length = 10)
	private String startDate;

	@Column(name = "end_date", length = 10)
	private String endDate;

	@Column(name = "company", nullable = false, length = 200)
	private String company;

	@Column(name = "department", length = 200)
	private String department;

	@Column(name = "position", length = 100)
	private String position;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "skills", columnDefinition = "text[]")
	private String[] skills;

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex;

	protected Career(String startDate, String endDate, String company,
	                 String department, String position, String[] skills, Integer orderIndex) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.company = company;
		this.department = department;
		this.position = position;
		this.skills = skills != null ? skills : new String[0];
		this.orderIndex = orderIndex != null ? orderIndex : 0;
	}

	// 업데이트 메서드들
	public void updateDates(String startDate, String endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void updateCompanyInfo(String company, String department, String position) {
		this.company = company;
		this.department = department;
		this.position = position;
	}

	public void updateSkills(String[] skills) {
		this.skills = skills != null ? skills : new String[0];
	}

	public void updateOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex != null ? orderIndex : 0;
	}
}

