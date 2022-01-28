package com.qa.user_app.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.qa.user_app.data.entity.User;
import com.qa.user_app.service.UserService;

@RestController // this is a bean that should be stored in the app context
@RequestMapping(path = "/user") // access this controller at localhost:8080/user
public class UserController {
	
	// UserController has-a JpaRepository
	// - How do we get this repository?
	// - To get the repository, we use dependency injection
	private UserService userService;
	
	@Autowired // indicates that the repository must be injected via dependency injection
	public UserController(UserService userService) {
		this.userService = userService;
	}

	// READ ALL
	@GetMapping // localhost:8080/user
	public ResponseEntity<List<User>> getUsers() {
		ResponseEntity<List<User>> users = ResponseEntity.ok(userService.getAll());
		return users;
	}

	// READ BY ID
	// {id} is a path variable
	// we send requests to: localhost:8080/user/{id}
	@RequestMapping(path = "/{id}", method = { RequestMethod.GET })
	// @GetMapping(path = "/{id}")
	public ResponseEntity<User> getUserById(@PathVariable("id") int id) {
		User savedUser = userService.getById(id);
	
		ResponseEntity<User> response = ResponseEntity.status(HttpStatus.OK).body(savedUser);
		return response;
	}

	// CREATE
	// RequestMapping(method = { RequestMethod.POST })
	@PostMapping // accepts requests to: localhost:8080/user using POST
	public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
		User savedUser = userService.create(user);
		HttpHeaders header = new HttpHeaders();
		header.add("Location", "/user/" + String.valueOf(savedUser.getId()));
															// (body, httpHeaders, responseStatusCode)
		ResponseEntity<User> response = new ResponseEntity<User>(savedUser, header, HttpStatus.CREATED);
		return response;
	}

	// UPDATE
	// update everything, aside from the id
	@PutMapping("/{id}") // localhost:8080/user/1
	public ResponseEntity<User> updateUser(@PathVariable("id") int id, @Valid @RequestBody User user) {
		User savedUser = userService.update(id, user);
		// Response entity = status accepted with body of savedUser
		ResponseEntity<User> response = ResponseEntity.status(HttpStatus.ACCEPTED).body(savedUser);
		return response;
	}

	// DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable("id") int id) {
		userService.delete(id);
		return ResponseEntity.accepted().build();
	}

}
