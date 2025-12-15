package site.jejinni.server.domain.entity.skill;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.jejinni.server.domain.entity.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "category_id", columnDefinition = "UUID")
  private UUID id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "order_index", nullable = false)
  private Integer order;

  @Builder
  public Category(String name, Integer order) {
    this.name = name;
    this.order = order != null ? order : 0;
  }

  public void updateName(String name) {
    this.name = name;
  }

  public void updateOrder(Integer order) {
    this.order = order != null ? order : 0;
  }
}
