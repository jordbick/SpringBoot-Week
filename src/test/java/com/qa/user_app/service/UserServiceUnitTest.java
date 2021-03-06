package com.qa.user_app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.qa.user_app.data.entity.User;
import com.qa.user_app.data.repository.UserRepository;
import com.qa.user_app.execptions.UserNotFoundException;

// No need to use the spring boot context, just create stubs using pure Mockito rather than Springs variant of Mockito
@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

	@Mock // equivalent to MockBean
	private UserRepository userRepository;

	@InjectMocks // equivalent to @Autowired
	private UserService userService;

	private List<User> users;
	private User expectedUserWithId;
	private User expectedUserWithoutId;
	private User foundUser;
	private User toUpdate;
	private User updatedUser;

	@BeforeEach // junit5 (jupiter) annotation to run this method before every test
	public void init() {
		users = new ArrayList<>();
		users.addAll(List.of(new User(1, "bob", "lee", 22), new User(2, "fred", "see", 25),
				new User(3, "sarah", "fee", 28)));
		expectedUserWithoutId = new User("bob", "lee", 22);
		expectedUserWithId = new User(1, "bob", "lee", 22);
		foundUser = new User(1, "bob", "lee", 22);
		toUpdate = new User("bob", "lee", 23);
		updatedUser = new User(1, "bob", "lee", 23);
	}

	@Test
	public void getAllUsersTest() {
		when(userRepository.findAll()).thenReturn(users);
		assertThat(userService.getAll()).isEqualTo(users);
		verify(userRepository).findAll();
	}

	@Test
	public void createUserTest() {
		when(userRepository.save(expectedUserWithoutId)).thenReturn(expectedUserWithId);
		assertThat(userService.create(expectedUserWithoutId)).isEqualTo(expectedUserWithId);
		verify(userRepository).save(expectedUserWithoutId);
	}

	@Test
	public void getUserByIdTest() {
		int id = foundUser.getId();
		when(userRepository.findById(id)).thenReturn(Optional.of(foundUser));
		assertThat(userService.getById(id)).isEqualTo(foundUser);
		verify(userRepository).findById(id);
	}
	
	@Test
	public void getUserByInvalidIdTest() {
		// Arrange-Act-Assert testing structure
		// - simplifies testing
		
		// Arrange (the data and components under test)
		int id = 34;
		when(userRepository.findById(id)).thenReturn(Optional.empty());
		
		// Act (perform the action under test)
		// assert that the code in the lambda (second param) throws the exception specified in
		// the first param
		UserNotFoundException e = Assertions.assertThrows(UserNotFoundException.class, () -> {
			userService.getById(id);
		});
		
		// Assert (the action was successful)
		String expected = "User with id " + id + " does not exist";
		assertThat(e.getMessage()).isEqualTo(expected);
		verify(userRepository).findById(id);
	}

	@Test
	public void updateUserTest() {
		int id = foundUser.getId();
		when(userRepository.existsById(id)).thenReturn(true);
		when(userRepository.getById(id)).thenReturn(foundUser);
		when(userRepository.save(foundUser)).thenReturn(updatedUser);

		assertThat(userService.update(id, toUpdate)).isEqualTo(updatedUser);

		verify(userRepository).getById(id);
		verify(userRepository).getById(id);
		verify(userRepository).save(foundUser);
	}

	@Test
	public void deleteUserTest() {
		int id = foundUser.getId();
		when(userRepository.existsById(id)).thenReturn(true);
		userService.delete(id);
		verify(userRepository).existsById(id);
		verify(userRepository).deleteById(id);
		assertThat(Optional.empty()).isEqualTo(userRepository.findById(id));
	}
}