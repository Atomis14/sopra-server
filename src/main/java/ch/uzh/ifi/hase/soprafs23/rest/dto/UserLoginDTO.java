package ch.uzh.ifi.hase.soprafs23.rest.dto;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class UserLoginDTO {

  private Long id;
  private String username;
  private UserStatus status;
  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date creationDate;
  @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date birthday;
  private String token;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
