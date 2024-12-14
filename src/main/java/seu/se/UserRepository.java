package seu.se;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<User, Long>, ListCrudRepository<User, Long> {
    List<User> findByName(String name);
    User findById(long id);
}
