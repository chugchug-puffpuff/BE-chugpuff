package chugpuff.chugpuff.repository;

import chugpuff.chugpuff.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
