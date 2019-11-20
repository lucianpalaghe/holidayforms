package ro.pss.holidayforms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ro.pss.holidayforms.domain.HolidayRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRequestRepository extends JpaRepository<HolidayRequest, Long> {

	@Query("SELECT DISTINCT r FROM HolidayRequest r " +
			"LEFT JOIN FETCH r.substitutionRequests s " +
			"LEFT JOIN FETCH r.approvalRequests a " +
			"WHERE r.requester.email = :requesterEmail")
	List<HolidayRequest> findAllByRequesterEmail(@Param("requesterEmail") String requesterEmail);

	@Query("SELECT DISTINCT r FROM HolidayRequest r " +
			"LEFT JOIN FETCH r.substitutionRequests s " +
			"LEFT JOIN FETCH r.approvalRequests a " +
			"WHERE r.id = :id")
	Optional<HolidayRequest> findById(@Param("id") Long id);

	List<HolidayRequest> findAllByRequesterEmailAndDateFromBetween(String email, LocalDate from, LocalDate to);
}