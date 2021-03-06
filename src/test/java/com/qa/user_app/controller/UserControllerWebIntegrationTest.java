package com.qa.user_app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.qa.user_app.data.entity.User;
import com.qa.user_app.service.UserService;

// @SpringBootTest // this will start a full application context
// Start an application context with only beans required for the controller layer
// - the specified controller is loaded into the context
// - some other web layer components, such as filter, will be initialised too
@WebMvcTest(UserController.class)
public class UserControllerWebIntegrationTest {

	@Autowired // field injection as an example of dependency injection
	private UserController controller;

	// we need a fake UserService
	// - we use Mockito to create a mock object
	@MockBean // we are using our defined UserService, but the methods will be mocked (we have
				// to specify what is returned from them)
	private UserService userService;

	// we need some data for our tests
	private List<User> users;
	private User userToCreate;
	private User validUser;
	private User userToFind;
	private User deletedUser;

	@BeforeEach // junit5 (jupiter) annotation to run this method before every test
	// injects into every single test, ensuring we have a fresh slate for every test
	public void init() {
		users = new ArrayList<>();
		users.addAll(
				List.of(new User(1, "bob", "lee", 22), new User(2, "fred", "lee", 28), new User(3, "beth", "lee", 32)));
		userToCreate = new User("bob", "lee", 22);
		validUser = new User(1, "bob", "lee", 22);
		userToFind = users.get(2); // id ==3
	}

	@Test // junit annotation
	public void getAllUsersTest() {
		ResponseEntity<List<User>> expected = new ResponseEntity<List<User>>(users, HttpStatus.OK);
		// given (some initial data/conditions)
		// this is being performed by init()

		// when (the action does occur)
		when(userService.getAll()).thenReturn(users);

		// then (assert this happened)
		ResponseEntity<List<User>> actual = controller.getUsers();
		assertThat(expected).isEqualTo(actual);

		// we also need to verify that the service was called by the controller
		verify(userService, times(1)).getAll();
		// userService is the mock bean
		// equivalent to verify(userService.getAll());
	}

	public void getUserByIdTest() {
		ResponseEntity<User> expected = ResponseEntity.of(Optional.of(validUser));

		when(userService.getById(1)).thenReturn(validUser);

		ResponseEntity<User> actual = controller.getUserById(1);

		assertThat(expected).isEqualTo(actual);

		verify(userService, times(1)).getById(1);
	}

	@Test
	public void createUserTest() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", "/user/" + String.valueOf(validUser.getId()));
		// body, httpHeaders, responseStatusCode
		ResponseEntity<User> expected = new ResponseEntity<User>(validUser, headers, HttpStatus.CREATED);

		// when we create a new user (userToCreate, without ID), returns a validUser
		// (with an ID)
		when(userService.create(userToCreate)).thenReturn(validUser);

		// then
		ResponseEntity<User> actual = controller.createUser(userToCreate);
		assertThat(expected).isEqualTo(actual);

		// verify that this mock object did in fact have its method called by
		// controllers
		// did the create method run with the userToCreate user
		verify(userService).create(userToCreate);
	}

	@Test
	public void updateUserTest() {
		User updatedUser = new User(1, "bob", "lee-swagger", 22);
		User toUpdateWith = new User("bob", "lee-swagger", 22);
		int userId = updatedUser.getId();

//		HttpHeaders headers = new HttpHeaders();
//		headers.add("Location", "/user/" + String.valueOf(userId));
		// if we wanted the above we would have to add headers to our UserController class
		// - and add header to the expected statement below
		ResponseEntity<User> expected = new ResponseEntity<User>(updatedUser, HttpStatus.ACCEPTED);

		when(userService.update(userId, toUpdateWith)).thenReturn(updatedUser);

		ResponseEntity<User> actual = controller.updateUser(userId, toUpdateWith);

		assertThat(expected).isEqualTo(actual);
		verify(userService).update(userId, toUpdateWith);
	}

	@Test
	public void deleteUserTest() {
		// SET UP
		int userId = 1;
		ResponseEntity<?> expected = ResponseEntity.accepted().build();
		ResponseEntity<?> actual = controller.deleteUser(userId);

		assertThat(expected).isEqualTo(actual);

		// VERIFY
		verify(userService).delete(1);

	}
}