package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserLoginDTO login(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    User user = userService.getUserByUsername(userInput.getUsername());
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username doesn't exist.");
    }
    if (!Objects.equals(userInput.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong password.");
    }
    userService.setUserStatus(user, UserStatus.ONLINE); // update online status
    return DTOMapper.INSTANCE.convertEntityToUserLoginDTO(user);
  }

  @PutMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
    User user = userService.getUserByToken(token);
    userService.setUserStatus(user, UserStatus.OFFLINE);
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
    userService.validateToken(token);
    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserLoginDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    if(Objects.equals(userInput.getUsername(), "") || userInput.getUsername() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty.");
    }
    if(Objects.equals(userInput.getPassword(), "") || userInput.getPassword() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty.");
    }
    User createdUser = userService.createUser(userInput);
    return DTOMapper.INSTANCE.convertEntityToUserLoginDTO(createdUser);
  }

  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO getUser(@PathVariable("id") int id, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
    userService.validateToken(token);
    Optional<User> opt = userService.getUserById(id);
    if(opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with " + id + " not found.");
    }
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(opt.get());
  }

  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUser(
          @PathVariable("id") int id,
          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
          @RequestBody UserPutDTO userPutDTO
  ) {
    userService.validateToken(token);
    Optional<User> opt = userService.getUserById(id);
    if(opt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found.");
    }
    User user = opt.get();
    if(!Objects.equals(user.getToken(), token)) {   // tokens don't match --> user tried to change another user's profile
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change attributes of other user.");
    }
    userService.updateUser(user, userPutDTO.getUsername(), userPutDTO.getBirthday());
  }

  @GetMapping("/setup")
  public void setup() {
    User u1 = new User();
    u1.setUsername("michel");
    u1.setPassword("123456");
    u1.setToken("xx1");
    userService.createUser(u1);
    User u2 = new User();
    u2.setUsername("michel2");
    u2.setPassword("123456");
    u2.setToken("xx2");
    userService.createUser(u2);
    User u3 = new User();
    u3.setUsername("michel3");
    u3.setPassword("123456");
    u3.setToken("xx3");
    userService.createUser(u3);
  }
}
