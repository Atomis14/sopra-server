package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("userRepository")
// 1st generic argument: entity, 2nd: type of the primary key in DB
public interface UserRepository extends JpaRepository<User, Long> {
  User findByUsername(String username);
  User findByToken(String token);
}