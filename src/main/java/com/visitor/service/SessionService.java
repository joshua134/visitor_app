package com.visitor.service;

import java.util.List;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
	private final SessionRegistry sessionRegistry;
	
	public int getActiveSessions() {
		List<Object> principals= sessionRegistry.getAllPrincipals();
		int activeSessions = 0;
		for(Object principal: principals) {
			activeSessions += sessionRegistry.getAllSessions(principal, false).size();
		}
		return activeSessions;
	}
	
	public void expiredUserSessions(String username) {
		Object principal = sessionRegistry.getAllPrincipals().stream()
				.filter(p -> p.toString().equals(username))
				.findFirst().orElse(null);
		if(principal != null) {
			for(SessionInformation session: sessionRegistry.getAllSessions(principal, true)) {
				session.expireNow();
				log.info("Expired session for user: {}", username);
			}
		}
	}
}
