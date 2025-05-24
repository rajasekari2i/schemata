package com.opsbeach.sharedlib.repository;

import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.RedisConnectionFailureException;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class CacheRepository {

   /* private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, String, Object> hashOperations;

    public CacheRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    public Boolean save(String hashKey, String key, Object value) {
        hashOperations.put(hashKey, key, value);
        closeConnection(redisTemplate);
        return Boolean.TRUE;
    }

    public Object get(String hashKey, String key) {
        Object object = hashOperations.get(hashKey, key);
        closeConnection(redisTemplate);
        return object;
    }

    public Boolean delete(String hashKey) {
        var deletionFlag = redisTemplate.delete(hashKey);
        closeConnection(redisTemplate);
        return deletionFlag;
    }

    public List<Object> getAll(String hashKey) {
        List<Object> cacheValues = hashOperations.values(hashKey);
        closeConnection(redisTemplate);
        return cacheValues;
    }

    private void closeConnection(RedisTemplate<String, Object> stringRedisTemplate) {
        try {
            JedisConnectionFactory connectionFactory = (JedisConnectionFactory) stringRedisTemplate.getConnectionFactory();
            Objects.requireNonNull(connectionFactory).getConnection().close();
            connectionFactory.destroy();
            log.info("Redis Connection has been closed");
        } catch (RedisConnectionFailureException e) {
            log.info("Connection closed already");
        } finally {
            closeClients(redisTemplate);
        }
    }

    private void closeClients(RedisTemplate<String, Object> stringRedisTemplate) {
        try {
            if (!CollectionUtils.isEmpty(stringRedisTemplate.getClientList())) {
                stringRedisTemplate.getClientList().remove(0);
                stringRedisTemplate.getClientList().remove(1);
                stringRedisTemplate.getClientList().forEach(redisClientInfo -> {
                    String address = redisClientInfo.getAddressPort();
                    if (StringUtil.isEmpty(address).equals(Boolean.FALSE)) {
                        String[] addressList = address.split(Constants.COMMA);
                        stringRedisTemplate.killClient(addressList[0], Integer.parseInt(addressList[1]));
                    }
                });
            }
        } catch (Exception e) {
            log.error("Unable to close redis cache client connection");
        }
    }*/
}