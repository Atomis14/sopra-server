package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  // GET /users 200
  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setUsername("michel");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].creationDate", is(user.getCreationDate())))
        .andExpect(jsonPath("$[0].birthday", is(user.getBirthday())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  // POST /users 201
  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setUsername("michel");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(new Date());

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("michel");
    userPostDTO.setPassword("123456");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toInstant().toString())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  // POST /users 409
  @Test
  public void createUser_invalidInput_usernameAlreadyExists() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("michel");
    userPostDTO.setPassword("123456");

    given(userService.createUser(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

    MockHttpServletRequestBuilder postRequest = post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
            .andExpect(status().isConflict());
  }

  // GET /users/1 200
  @Test
  public void givenUser_whenGetUser_thenReturnJson() throws Exception {
    User user = new User();
    user.setId(1L);
    user.setUsername("michel");
    user.setStatus(UserStatus.ONLINE);
    user.setCreationDate(new Date());
    Optional<User> optUser = Optional.of(user);

    given(userService.getUserById(1)).willReturn(optUser);
    doNothing().when(userService).validateToken(Mockito.anyString());

    MockHttpServletRequestBuilder getRequest = get("/users/1");
    mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(user.getId().intValue())))
            .andExpect(jsonPath("$.username", is(user.getUsername())))
            .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toInstant().toString())))
            .andExpect(jsonPath("$.birthday").doesNotExist())
            .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  // GET /users/1 404
  @Test
  public void noUser_whenGetUser_notFound() throws Exception {
    given(userService.getUserById(1)).willReturn(Optional.empty());
    doNothing().when(userService).validateToken(Mockito.anyString());

    MockHttpServletRequestBuilder getRequest = get("/users/1");
    mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());
  }

  // PUT /users/1 204
  @Test
  public void updateExistingUserProfile() throws Exception {
    // DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    // Date date = dateFormat.parse("01-01-2000");

    User user = new User();
    user.setId(1L);
    user.setUsername("michel");

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("michel2");
    userPutDTO.setBirthday(new Date());

    given(userService.getUserById(1)).willReturn(Optional.of(user));
    doNothing().when(userService).validateToken(Mockito.anyString());

    MockHttpServletRequestBuilder putRequest = put("/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO));

    mockMvc.perform(putRequest)
            .andExpect(status().isNoContent());
  }

  @Test
  public void updateNonExistingUserProfile() throws Exception {
    given(userService.getUserById(1)).willReturn(Optional.empty());
    doNothing().when(userService).validateToken(Mockito.anyString());

    UserPutDTO userPutDTO = new UserPutDTO();
    userPutDTO.setUsername("michel");

    MockHttpServletRequestBuilder putRequest = put("/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPutDTO));

    mockMvc.perform(putRequest)
            .andExpect(status().isNotFound());
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}