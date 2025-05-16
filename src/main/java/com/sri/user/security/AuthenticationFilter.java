package com.sri.user.security;


import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sri.user.SpringApplicationContext;
import com.sri.user.pojo.LoginRequestModel;
import com.sri.user.pojo.UserDto;
import com.sri.user.service.UserService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	public AuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException{
		try {
		LoginRequestModel creds =new ObjectMapper().readValue(req.getInputStream(),LoginRequestModel.class);
		return getAuthenticationManager().authenticate(
				new UsernamePasswordAuthenticationToken(creds.getEmail(),creds.getPassword(),new ArrayList<>()));
		}
		catch(IOException e) {
			throw new RuntimeException();
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		String UserEmail=((UserPrinciple)authResult.getPrincipal()).getUsername();
		UserDto userDto=((UserService)SpringApplicationContext.getBean("userServiceImpl")).getUserDetailsByEmail(UserEmail);
		AppProperties appProperties=(AppProperties)SpringApplicationContext.getBean("AppProperties");
		String commonKey=appProperties.getTokenSecret();
		
		SecretKey secretKey=Keys.hmacShaKeyFor(commonKey.getBytes());
		
		
		String token=Jwts.builder()
				.subject(userDto.getUserId())
				.expiration(Date.from(Instant.now().plusMillis(Long.parseLong(appProperties.getTokenExpirationTime()))))
				.issuedAt(Date.from(Instant.now()))
				.signWith(secretKey)
				.compact();
		
		response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX+token);
		response.addHeader(SecurityConstants.USER_ID,userDto.getUserId());
	}
}
