package site.jejinni.server.repository.education;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.jejinni.server.domain.entity.education.Education;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationRepository extends JpaRepository<Education, UUID> {
	List<Education> findAllByOrderByOrderIndexAsc();
}
