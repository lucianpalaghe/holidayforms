package ro.pss.holidayforms.config.security.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.util.Optional;

@Service
public class JiraOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws org.springframework.security.oauth2.core.OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
		String email = String.valueOf(oAuth2User.getAttributes().get("email"));

		Optional<User> userOptional = userRepository.findById(email.replace("@pss.ro", "")); // TODO: fix dummy implementation
		if (!userOptional.isPresent()) {
			throw new OAuth2AuthenticationException(new OAuth2Error("123"));
		}

		return new CustomUserPrincipal(userOptional.get());
	}
}
