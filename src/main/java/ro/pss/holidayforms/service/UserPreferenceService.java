package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.domain.repo.UserPreferencesRepository;

@Service
public class UserPreferenceService {
    @Autowired
    UserPreferencesRepository userPreferencesRepository;

	/**
	 * Return the associated UserPreference object of the User identified by the parameter or {@link UserPreferences}.defaultPreferences()
	 *
	 * @param email of user whose preferences should be returned
	 * @return preferences from database or default user preferences
	 */
	public UserPreferences findByEmployeeEmail(String email) {
		return userPreferencesRepository.findByEmployeeEmail(email)
				.orElse(UserPreferences.defaultPreferences());
    }

    public UserPreferences savePreferences(UserPreferences userPreferences) {
        return userPreferencesRepository.save(userPreferences);
    }
}
