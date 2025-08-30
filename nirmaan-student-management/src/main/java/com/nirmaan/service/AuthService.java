package com.nirmaan.service;

import com.nirmaan.dto.LoginRequest;
import com.nirmaan.dto.LoginResponse;
import com.nirmaan.entity.User;
import com.nirmaan.exception.UnauthorizedException;
import com.nirmaan.repository.UserRepository;
import com.nirmaan.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider tokenProvider;
	private final AuthenticationManager authenticationManager;

	public LoginResponse login(LoginRequest loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			String token = tokenProvider.generateToken(authentication);
			User user = userRepository.findByUsername(loginRequest.getUsername())
					.orElseThrow(() -> new UnauthorizedException("User not found"));

			return new LoginResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getFirstName(),
					user.getLastName());
		} catch (AuthenticationException e) {
			throw new UnauthorizedException("Invalid username or password");
		}
	}
}
