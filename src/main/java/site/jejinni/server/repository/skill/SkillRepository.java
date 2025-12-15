package site.jejinni.server.repository.skill;

import org.springframework.data.jpa.repository.JpaRepository;
import site.jejinni.server.domain.entity.skill.Category;
import site.jejinni.server.domain.entity.skill.Skill;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

	List<Skill> findByCategoryOrderByOrderAsc(Category category);

	List<Skill> findAllByOrderByOrderAsc();

	boolean existsByCategory(Category category);
}

