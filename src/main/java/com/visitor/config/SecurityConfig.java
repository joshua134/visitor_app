package com.visitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.visitor.handler.CustomAuthenticationSuccessHandler;
import com.visitor.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomUserDetailsService userDetailsService;
	private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/register-success", "/login", "/logout", "/register", "/accept-invitation", "/accept-invitation/**",
						"/forgot-password", "/reset-password", "/reset-password/**","/test-data/**",
						"/captcha-image", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
				.requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER", "VIEWER")
				.requestMatchers("/manager/**").hasAnyRole("ADMIN", "MANAGER")
				.requestMatchers("/viewer/**").hasRole("VIEWER")
				.anyRequest().authenticated()
				)
		.sessionManagement(session -> session
				.maximumSessions(-1)
				.sessionRegistry(sessionRegistry())
				.expiredUrl("/login?expired=true")
				)
		.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
//				.defaultSuccessUrl("/dashboard", true)
				.successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
				.permitAll())
		.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
				)
		.rememberMe(remember -> remember
				.key("uniqueAndSecret")
				.tokenValiditySeconds(1209600)
				.rememberMeParameter("remember-me")
				)
		.userDetailsService(userDetailsService);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}
	
	@Bean
	HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}
}
