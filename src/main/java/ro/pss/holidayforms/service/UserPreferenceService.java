package ro.pss.holidayforms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.domain.UserPreferences;
import ro.pss.holidayforms.domain.repo.UserPreferencesRepository;

import java.util.Optional;

@Service
public class UserPreferenceService {
    @Autowired
    UserPreferencesRepository userPreferencesRepository;

    public Optional<UserPreferences> findByEmployeeEmail(String email) {
        return userPreferencesRepository.findByEmployeeEmail(email);
    }

    public UserPreferences savePreferences(UserPreferences userPreferences) {
        return userPreferencesRepository.save(userPreferences);
    }


}
