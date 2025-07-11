package com.sri.user.controller;

import java.util.Arrays;
import java.util.HashSet;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sri.user.pojo.CreateUserRequestModel;
import com.sri.user.pojo.CreateUserResponseModel;
import com.sri.user.pojo.UserDto;
import com.sri.user.pojo.UserResponseModel;
import com.sri.user.service.UserService;
import com.sri.user.shared.Role;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	ModelMapper mapper;
	
//	@PostAuthorize("hasRole('ADMIN') or returnObject.userId==principal.userId")
	@PostMapping(value="/createuser",
				consumes= {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
				produces= {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE}
				)
	@ResponseBody
	public ResponseEntity<CreateUserResponseModel> createuser(@Validated @RequestBody CreateUserRequestModel createUserRequestModel) {
//		ModelMapper mapper=new ModelMapper();
//		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDto=mapper.map(createUserRequestModel, UserDto.class);
		userDto.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_USER.name())));
		UserDto createdUser=userService.createUser(userDto);
		
		CreateUserResponseModel response=mapper.map(createdUser, CreateUserResponseModel.class);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
	}

	@GetMapping(value="/getAll/{userId}",produces= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<UserResponseModel>getUser(@PathVariable("userId")String userId){
		UserDto userDetails=userService.getUserByUserId(userId);
		UserResponseModel response=mapper.map(userDetails,UserResponseModel.class);
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
	}
	
	@PreAuthorize("hasRole('ADMIN') or #id==principal.userId")
//	@Secured("ROLE_ADMIN")
	@DeleteMapping(path="/{id}")
	public String deleteUser(@PathVariable String id) {
		return "Delete is successful";
	}
}
