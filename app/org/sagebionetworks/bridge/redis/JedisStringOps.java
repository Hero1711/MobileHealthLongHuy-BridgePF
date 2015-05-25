package org.sagebionetworks.bridge.redis;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class JedisStringOps {

    private JedisPool jedisPool;
    
    @Autowired
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * The specified key will expire after seconds.
     *
     * @param key
     *            target key.
     * @param seconds
     *            number of seconds until expiration.
     * @return success code
     *          1 if successful, 0 if key doesn't exist or timeout could not be set
     */
    public Long expire(final String key, final int seconds) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        }.execute();
    }

    /**
     * Sets the value of the key and makes it expire after the specified
     * seconds.
     *
     * @param key
     *            key of the key-value pair.
     * @param seconds
     *            number of seconds until expiration.
     * @param value
     *            value of the key-value pair.
     */
    public String setex(final String key, final int seconds, final String value) {
        return new AbstractJedisTemplate<String>() {
            @Override
            String execute(Jedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        }.execute();
    }

    /**
     * Sets the value of the key if and only if the key does not already have a
     * value.
     *
     * @param key
     *            key of the key-value pair.
     * @param value
     *            value of the key-value pair.
     * @return success code
     *          1 if the key was set, 0 if not
     */
    public Long setnx(final String key, final String value) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.setnx(key, value);
            }
        }.execute();
    }

    /**
     * Gets the value of the specified key. If the key does not exist null is
     * returned.
     */
    public String get(final String key) {
        return new AbstractJedisTemplate<String>() {
            @Override
            String execute(Jedis jedis) {
                return jedis.get(key);
            }
        }.execute();
    }

    /**
     * Deletes the value of the specified key.
     *
     * @param key
     *            key of the key-value pair
     * @return numKeysDeleted
     *          the number of keys deleted
     */
    public Long delete(final String key) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.del(key);
            }
        }.execute();
    }

    /**
     * Determines the time until expiration for a key (time-to-live).
     *
     * @param key
     *            key of the key-value pair.
     * @return ttl
     *      positive value if ttl is set, zero if not, negative if there was an error
     */
    public Long ttl(final String key) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.ttl(key);
            }
        }.execute();
    }

    public Long clearRedis(final String keyPattern) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                Set<String> keys = jedis.keys(keyPattern);
                for (String key : keys) {
                    jedis.del(key);
                }
                return new Long(keys.size());
            }
        }.execute();
    }

    /**
     * Increment the value by one
     * @param key
     * @return the new value of the key (after incrementing).
     */
    public Long increment(final String key) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.incr(key);
            }
        }.execute();
    }

    /**
     * Decrement the value by one.
     * @param key
     * @return the new value of the key (after decrementing).
     */
    public Long decrement(final String key) {
        return new AbstractJedisTemplate<Long>() {
            @Override
            Long execute(Jedis jedis) {
                return jedis.decr(key);
            }
        }.execute();
    }

    private abstract class AbstractJedisTemplate<T> {
        public T execute() {
            Jedis jedis = jedisPool.getResource();
            try {
                return execute(jedis);
            } catch (JedisConnectionException e) {
                if (jedis != null) {
                    jedisPool.returnBrokenResource(jedis);
                    jedis = null;
                }
                return null;
            } finally {
                if (jedis != null) {
                    jedisPool.returnResource(jedis);
                }
            }
        }

        abstract T execute(Jedis jedis);
    }
}
