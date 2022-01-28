package com.qa.user_app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.user_app.data.entity.User;
import com.qa.user_app.data.repository.UserRepository;

// Setting a random port is good practice in preparation for parallel testing
// - running multiple tests at the same time
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) // Start the ApplicationContext on a random port
@AutoConfigureMockMvc // Configure the MockMvc object
@Transactional // Roll back the state of the database after every test so each test has a fresh
				// slate
public class UserControllerSystemIntegrationTest {

	// MockMvc is used for sending HTTP requests
	@Autowired
	private MockMvc mockMvc;

	// ObjectMappers are used for serializing and deserializing objects
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	private List<User> usersInDatabase;
	private int nextNewElementsId;
	
	// initialise the database for our tests
	// before each test initialise the database with the information below
	@BeforeEach
	public void init() {
		List<User> users = List.of(new User(1, "bob", "lee", 22), new User(2, "fred", "see", 25),
				new User(3, "sarah", "fee", 28));
		usersInDatabase = new ArrayList<>();
		usersInDatabase.addAll(userRepository.saveAll(users));
		int size = usersInDatabase.size();
		nextNewElementsId = usersInDatabase.get(size - 1).getId() + 1;
	}

	@Test
	public void getAllUsersTest() throws Exception {
		// Create a mock http request builder
		MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.request(HttpMethod.GET, "/user");

		// Specify the Accept header for the expected returned content type (MediaType)
		mockRequest.accept(MediaType.APPLICATION_JSON);

		// Create expected JSON String from usersInDatabase using the ObjectMapper
		// instance
		String users = objectMapper.writeValueAsString(usersInDatabase);

		// Setup ResultMatchers
		// - used for comparing the result of the mockRequest with our own specified
		// values
		ResultMatcher statusMatcher = MockMvcResultMatchers.status().isOk();
		ResultMatcher contentMatcher = MockMvcResultMatchers.content().json(users);

		// Send the request and assert the results where as expected
		mockMvc.perform(mockRequest).andExpect(statusMatcher).andExpect(contentMatcher);
	}


	@Test
	public void getUserByIdTest() throws Exception {
		int id = 1;
		User expectedUser = userRepository.getById(id);
		MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
				.request(HttpMethod.GET, "/user/" + id);

		// Specify the Accept header for the expected returned content type (MediaType)
		mockRequest.accept(MediaType.APPLICATION_JSON);

		// Create expected JSON String from usersInDatabase using the ObjectMapper
		// instance
		// This is serialisation of our object so we can plug this into what we expect on our contentMatcher
		String user = objectMapper.writeValueAsString(expectedUser);

		// Setup ResultMatchers
		// - used for comparing the result of the mockRequest with our own specified
		// values
		ResultMatcher statusMatcher = MockMvcResultMatchers.status().isOk();
		ResultMatcher contentMatcher = MockMvcResultMatchers.content().json(user);

		// Send the request and assert the results where as expected
		mockMvc.perform(mockRequest).andExpect(statusMatcher).andExpect(contentMatcher);
	}

	@Test
	public void createUserTest() throws Exception {
		User userToSave = new User("Janet", "Carlisle", 32);
		User expectedUser = new User(nextNewElementsId, userToSave.getForename(), userToSave.getSurname(), userToSave.getAge());
		
		// Configure mock request
		MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.request(HttpMethod.POST, "/user");
		
		mockRequest.contentType(MediaType.APPLICATION_JSON); // set the Content-Type header for the body data
		mockRequest.content(objectMapper.writeValueAsString(userToSave)); // set the body of the request to a JSON string
		// .content() adds { forename 
		mockRequest.accept(MediaType.APPLICATION_JSON);

		// Configure ResultMatchers
		ResultMatcher statusMatcher = MockMvcResultMatchers.status().isCreated();
		ResultMatcher contentMatcher = MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expectedUser));

		// Send the request and assert the results where as expected
		mockMvc.perform(mockRequest).andExpect(statusMatcher).andExpect(contentMatcher);
	}
	
	@Test
	public void updateUserTest() throws Exception {
		int id = 1;
		User updatedUser = new User(id, "bob", "lee", 23);
		
		MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
				.request(HttpMethod.PUT, "/user/" + id);
		
		// media type of content allowed
		mockRequest.contentType(MediaType.APPLICATION_JSON);
		// what we put into our request/update
		mockRequest.content(objectMapper.writeValueAsString(updatedUser)); 
		mockRequest.accept(MediaType.APPLICATION_JSON);
		// the output we expect back
		String expected = objectMapper.writeValueAsString(updatedUser);
		
		
		ResultMatcher statusMatcher = MockMvcResultMatchers.status().isAccepted();
		ResultMatcher contentMatcher = MockMvcResultMatchers.content().json(expected);
		
		mockMvc.perform(mockRequest).andExpect(statusMatcher).andExpect(contentMatcher);
		
	}

	@Test
	public void deleteUserTest () throws Exception {
		int id = 1;
		
		MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders
				.request(HttpMethod.DELETE, "/user/" + id);


		// Setup ResultMatchers
		// - used for comparing the result of the mockRequest with our own specified
		// values
		ResultMatcher statusMatcher = MockMvcResultMatchers.status().isAccepted();

		// Send the request and assert the results where as expected
		mockMvc.perform(mockRequest).andExpect(statusMatcher);
		assertEquals(Optional.empty(), userRepository.findById(id));
	}
}