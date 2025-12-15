package site.jejinni.server.repository.skill;

import org.springframework.data.jpa.repository.JpaRepository;
import site.jejinni.server.domain.entity.skill.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

	List<Category> findAllByOrderByOrderAsc();
}

