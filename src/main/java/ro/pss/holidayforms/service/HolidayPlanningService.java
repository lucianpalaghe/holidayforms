package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.HolidayPlanning;
import ro.pss.holidayforms.domain.repo.HolidayPlanningRepository;

import java.util.Optional;

@Service
public class HolidayPlanningService {
	@Autowired
	private HolidayPlanningRepository repository;

	public Optional<HolidayPlanning> getHolidayPlanning(String userEmail) {
		return repository.findByEmployeeEmail(userEmail);
	}

	public void savePlanning(HolidayPlanning planning) {
		repository.save(planning);
	}
}
