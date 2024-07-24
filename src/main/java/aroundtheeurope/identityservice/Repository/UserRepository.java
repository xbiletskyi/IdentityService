package aroundtheeurope.identityservice.Repository;

import aroundtheeurope.identityservice.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
