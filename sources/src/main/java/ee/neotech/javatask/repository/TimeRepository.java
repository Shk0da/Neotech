package ee.neotech.javatask.repository;

import ee.neotech.javatask.domain.Time;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * Репозиторий сущности {@link Time}
 */
@Repository
public interface TimeRepository extends JpaRepository<Time, Timestamp> {
}
