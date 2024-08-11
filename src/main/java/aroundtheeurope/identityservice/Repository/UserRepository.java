package aroundtheeurope.identityservice.Repository;

import aroundtheeurope.identityservice.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}
