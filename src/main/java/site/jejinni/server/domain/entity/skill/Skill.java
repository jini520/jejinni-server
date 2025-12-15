package site.jejinni.server.domain.entity.skill;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import site.jejinni.server.domain.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Skill extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "skill_id", columnDefinition = "UUID")
  private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @OnDelete(action = OnDeleteAction.RESTRICT)
  private Category category;

  @Column(name = "order_index", nullable = false)
  private Integer order;

  @Builder
  public Skill(String name, Category category, Integer order) {
    this.name = name;
    this.category = category;
    this.order = order != null ? order : 0;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateCategory(Category category) {
    this.category = category;
  }

  public void updateOrder(Integer order) {
    this.order = order != null ? order : 0;
  }
}
