package com.qa.user_app.service;

import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qa.user_app.data.entity.User;
import com.qa.user_app.data.repository.UserRepository;
import com.qa.user_app.execptions.UserNotFoundException;

//Need to register it as a class to Spring to annotate it as a bean
// @Component
// Or we can annotate as a Service which is a type of component
@Service
public class UserService {
	
	private UserRepository userRepository;
	// importing data from UserRepo class
	
	
	@Autowired // dependency injection by using the constructor
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getAll(){
		return userRepository.findAll();
	}
	

	public User getById(Integer id) {
//		return userRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		
		return userRepository.findById(id).orElseThrow(() -> {
			return new UserNotFoundException("User with id " + id + " does not exist");
		});
		
//		if (userRepository.existsById(id)) {
//			return userRepository.findById(id).get();
//		}
//		throw new EntityNotFoundException("User with id " + id + " does not exist.");
	}
	
	public User create(User user) {
		User savedUser = userRepository.save(user);
		return savedUser;
	}
	
	public User update(Integer id, User user) {
		// repository.save() will overwrite entities that already exist in the db
		// 1. Check if user exists
		if (userRepository.existsById(id)) {
			// 2. get user is db
			User updatedUser = userRepository.getById(id);
			// 3. update with user fields
			updatedUser.setAge(user.getAge());
			updatedUser.setForename(user.getForename());
			updatedUser.setSurname(user.getSurname());
			return userRepository.save(updatedUser);
		}else {
			throw new EntityNotFoundException("User with id " + id + " does not exist.");
		}
		
	}
	
	public void delete(Integer id) {
		if (userRepository.existsById(id)) {
			userRepository.deleteById(id);
		} else {
			throw new EntityNotFoundException("User with id " + id + " does not exist.");
		}

	}
	
}
