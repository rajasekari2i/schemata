package com.opsbeach.sharedlib.service;

import com.opsbeach.sharedlib.repository.CacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Cache service is key value storage.
 * </p>
 */
@Service
public class CacheService {

    /*private final CacheRepository cacheRepository;

    public CacheService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    public Boolean save(String hashKey, String key, Object value) {
        return cacheRepository.save(hashKey, key, value);
    }

    public Object get(String hashKey, String key) {
        return cacheRepository.get(hashKey, key);
    }

    public Boolean delete(String hashKey) {
        return cacheRepository.delete(hashKey);
    }

    public List<Object> getAll(String hashKey) {
        return cacheRepository.getAll(hashKey);
    }*/
}