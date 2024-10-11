package com.kv.config;

import com.kv.matchdetails.dto.MatchDetailsDto;
import com.kv.service.CacheEventListenerImpl;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import org.ehcache.event.EventType;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.Set;

import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.event.EventType.*;

@Configuration
@EnableCaching
@Log4j2
@AllArgsConstructor
public class EhCacheConfig {
    @Autowired private final CacheEventListenerImpl cacheEventListener;

    @Bean
    public CacheManager ehCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        cacheManager.createCache("steam.web.api.match-details", getCacheConfiguration(String.class, MatchDetailsDto.class, 86400)); //1 Day

        return cacheManager;
    }

    private <K,V> javax.cache.configuration.Configuration<K, V> getCacheConfiguration(Class<K> keyType, Class<V> valueType, int timeToLiveInSeconds) {
        var configBuilder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(
                        keyType,
                        valueType,
                        ResourcePoolsBuilder
                                .newResourcePoolsBuilder().offheap(200, MemoryUnit.MB))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timeToLiveInSeconds)))
                .withService(getCacheEventListener())
                .build();

        return Eh107Configuration.fromEhcacheCacheConfiguration(configBuilder);
    }

    private CacheEventListenerConfigurationBuilder getCacheEventListener() {
        CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration = newEventListenerConfiguration(cacheEventListener, Set.of(CREATED, EventType.UPDATED))
                .unordered().asynchronous();
        return cacheEventListenerConfiguration;
    }
}