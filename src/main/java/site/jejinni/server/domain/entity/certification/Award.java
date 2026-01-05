package site.jejinni.server.domain.entity.certification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.jejinni.server.domain.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "awards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Award extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "award_id", columnDefinition = "UUID")
	private UUID id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "date", nullable = false, length = 10)
	private String date;

	@Column(name = "organization", nullable = false, length = 200)
	private String organization;

	@Column(name = "tier", length = 100)
	private String tier;

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex;

	@Builder
	public Award(String name, String date, String organization, String tier, Integer orderIndex) {
		this.name = name;
		this.date = date;
		this.organization = organization;
		this.tier = tier;
		this.orderIndex = orderIndex != null ? orderIndex : 0;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateDate(String date) {
		this.date = date;
	}

	public void updateOrganization(String organization) {
		this.organization = organization;
	}

	public void updateTier(String tier) {
		this.tier = tier;
	}

	public void updateOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex != null ? orderIndex : 0;
	}
}

