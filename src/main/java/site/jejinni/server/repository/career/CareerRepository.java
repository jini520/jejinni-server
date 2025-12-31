package site.jejinni.server.repository.career;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.jejinni.server.domain.entity.career.Career;

import java.util.List;
import java.util.UUID;

@Repository
public interface CareerRepository extends JpaRepository<Career, UUID> {
	List<Career> findAllByOrderByOrderIndexAsc();
}

