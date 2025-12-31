package site.jejinni.server.domain.entity.career;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "businesses")
@DiscriminatorValue("BUSINESS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Business extends Career {

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(name = "details", columnDefinition = "text[]")
	private String[] details;

	@Builder
	public Business(String startDate, String endDate, String company,
	                String department, String position, String[] skills,
	                Integer orderIndex, String[] details) {
		super(startDate, endDate, company, department, position, skills, orderIndex);
		this.details = details != null ? details : new String[0];
	}

	public void updateDetails(String[] details) {
		this.details = details != null ? details : new String[0];
	}
}

