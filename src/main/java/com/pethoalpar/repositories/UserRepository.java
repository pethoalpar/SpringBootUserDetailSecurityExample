package com.pethoalpar.repositories;

import com.pethoalpar.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long>{

    User findByUserName(String userName);
}
