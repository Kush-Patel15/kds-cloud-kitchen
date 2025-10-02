// package com.cloudkitchen.service;

// import com.cloudkitchen.model.User;
// import com.cloudkitchen.model.User.Role;
// import com.cloudkitchen.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Service
// public class UserService {

//     @Autowired
//     private UserRepository userRepository;

//     public User createUser(User user) {
//         return userRepository.save(user);
//     }

//     public User getUserById(Long id) {
//         return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
//     }

//     public List<User> getAllUsers() {
//         return userRepository.findAll();
//     }

//     public List<User> getActiveUsers() {
//         return userRepository.findByIsActiveTrueOrderByFirstNameAscLastNameAsc();
//     }

//     public List<User> getUsersByRole(Role role) {
//         return userRepository.findByRoleAndIsActiveTrueOrderByFirstNameAscLastNameAsc(role);
//     }

//     public User updateUser(Long id, User userDetails) {
//         User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
//         user.setFirstName(userDetails.getFirstName());
//         user.setLastName(userDetails.getLastName());
//         user.setEmail(userDetails.getEmail());
//         user.setUsername(userDetails.getUsername());
//         user.setRole(userDetails.getRole());
//         user.setPhoneNumber(userDetails.getPhoneNumber());
//         user.setIsActive(userDetails.getIsActive());
//         return userRepository.save(user);
//     }

//     public void deleteUser(Long id) {
//         userRepository.deleteById(id);
//     }

//     public User findByEmail(String email) {
//         return userRepository.findByEmail(email).orElse(null);
//     }
// }

package com.cloudkitchen.service;

import com.cloudkitchen.model.User;
import com.cloudkitchen.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository repo;
  public UserService(UserRepository repo) { this.repo = repo; }

  public User findByEmail(String email) { return repo.findByEmail(email).orElse(null); }
  public User findByUsername(String username) { return repo.findByUsername(username).orElse(null); }
  public User create(User u) { return repo.save(u); }
  public boolean validateLogin(String email, String rawPassword) {
    User u = findByEmail(email);
    return u != null && u.getPassword().equals(rawPassword);
  }
}