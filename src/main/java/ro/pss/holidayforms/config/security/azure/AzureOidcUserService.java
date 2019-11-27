package ro.pss.holidayforms.config.security.azure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import ro.pss.holidayforms.config.security.CustomUserPrincipal;
import ro.pss.holidayforms.config.security.jira.JiraOAuth2UserInfo;
import ro.pss.holidayforms.domain.User;
import ro.pss.holidayforms.domain.repo.UserRepository;

import java.util.Optional;

@Service
public class AzureOidcUserService extends OidcUserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public OidcUser loadUser(OidcUserRequest oAuth2UserRequest) {
		OidcUser oAuth2User = super.loadUser(oAuth2UserRequest);
		JiraOAuth2UserInfo oAuth2UserInfo = new JiraOAuth2UserInfo(oAuth2User.getAttributes());
//		if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
//			throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
//		}

		Optional<User> userOptional = userRepository.findById(oAuth2UserInfo.getUniqueName().replace("@pss.ro", "").toLowerCase());
		if (!userOptional.isPresent()) {
			throw new OAuth2AuthenticationException(new OAuth2Error("User does not exist in application!"));
		}

		return new CustomUserPrincipal(userOptional.get());
	}
}
