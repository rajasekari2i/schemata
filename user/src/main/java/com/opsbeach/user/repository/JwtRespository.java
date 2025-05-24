package com.opsbeach.user.repository;

import com.opsbeach.user.base.BaseRepository;
import com.opsbeach.user.entity.Jwt;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtRespository extends BaseRepository<Jwt> {

}
