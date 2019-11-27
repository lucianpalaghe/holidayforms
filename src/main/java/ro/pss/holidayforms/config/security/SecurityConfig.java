package ro.pss.holidayforms.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import ro.pss.holidayforms.config.security.azure.AzureOidcUserService;

/**
 * Configures spring security, doing the following:
 * <li>Bypass security checks for static resources,</li>
 * <li>Restrict access to the application, allowing only logged in users,</li>
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static final String LOGIN_PROCESSING_URL = "/login";
	private static final String LOGIN_FAILURE_URL = "/login";
	private static final String LOGIN_URL = "/login";
	private static final String LOGOUT_SUCCESS_URL = "/login";

//	@Autowired
//	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private AzureOidcUserService oidcUserService;

	/**
	 * Require login to access internal pages and configure login form.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Not using Spring CSRF here to be able to use plain HTML for the login page
		http.csrf().disable()

				// Register our CustomRequestCache, that saves unauthorized access attempts, so
				// the user is redirected after login.
				.requestCache().requestCache(new CustomRequestCache())

				// Restrict access to our application.
				.and()
				.authorizeRequests()

				// Allow all flow internal requests.
				.requestMatchers(SecurityUtils::isFrameworkInternalRequest)
				.permitAll()

				// Allow all requests by logged in users.
				.anyRequest()
				.authenticated()
				.and()
				.oauth2Login()
				.redirectionEndpoint()
//					.baseUri("/login/oauth/client/*")
				.and()
				.userInfoEndpoint()
//					.oidcUserService(oidcUserService)
				.oidcUserService(oidcUserService)
				.and()
				.defaultSuccessUrl("/")
				.failureUrl(LOGIN_FAILURE_URL)
				.and().oauth2Login().loginPage("/login").permitAll()
				.and()
				.logout()
//					.logoutSuccessUrl("https://login.windows.net/common/oauth2/logout?post_logout_redirect_uri=http://localhost")
				.logoutSuccessUrl("https://login.windows.net/common/oauth2/logout")
				.and().sessionManagement().maximumSessions(1).sessionRegistry(sessionRegistry()).expiredUrl("/login?expire");
	}

	/**
	 * Allows access to static resources, bypassing Spring security.
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(
//				"/login/oauth/client/azure/**",
				// Vaadin Flow static resources
				"/VAADIN/**",

				// the standard favicon URI
				"/favicon.ico",

				// the robots exclusion standard
				"/robots.txt",

				// icons and images
				"/icons/**",
				"/images/**",

				// (development mode) static resources
				"/frontend/**",

				// (development mode) webjars
				"/webjars/**",

				// (development mode) H2 debugging console
				"/h2-console/**",

				// (production mode) static resources
				"/frontend-es5/**", "/frontend-es6/**");
	}
	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}
	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
	}

}