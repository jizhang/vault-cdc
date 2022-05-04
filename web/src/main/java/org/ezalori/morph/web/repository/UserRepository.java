package org.ezalori.morph.web.repository;

import java.util.Optional;
import org.ezalori.morph.web.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
  Optional<User> findByUsername(String username);
}
