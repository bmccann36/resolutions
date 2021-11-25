package io.jzheaux.springsecurity.resolutions;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests(a -> a
				.mvcMatchers("/resolutions").hasAuthority("SCOPE_resolution:read")
				.mvcMatchers("/resolution/{id}/share").hasAuthority("resolution:share")
					//? EXAMPLE rule : this makes it so you need basic_start_auth to hit /test route
				.mvcMatchers("/test").hasAuthority("basic_start_authorization")
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(o -> o.opaqueToken());
	}

	/**
	 * by publishing this as a bean it will override the default one that comes with spring
	 */
	@Bean
	public OpaqueTokenIntrospector tokenIntrospector(
			OAuth2ResourceServerProperties properties, UserRepository users) {
		String url = properties.getOpaquetoken().getIntrospectionUri();
		String clientId = properties.getOpaquetoken().getClientId();
		String clientSecret = properties.getOpaquetoken().getClientSecret();
		// delegate not needed for basic mbusa use case
		OpaqueTokenIntrospector delegate = new NimbusOpaqueTokenIntrospector
				(url, clientId, clientSecret);
//		return new ResolutionOpaqueTokenIntrospector(delegate, users);
		return new GasOidcTokenIntrospector();
	}
}
