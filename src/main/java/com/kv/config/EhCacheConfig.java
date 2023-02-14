package com.kv.config;

import com.kv.service.CacheEventListenerImpl;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
@Log4j2
public class EhCacheConfig {
    private final CacheManager ehCacheManager;
    private final CacheEventListenerImpl cacheEventListenerImpl;

    public EhCacheConfig(@Autowired CacheManager ehCacheManager, @Autowired CacheEventListenerImpl cacheEventListenerImpl) {
        this.ehCacheManager = ehCacheManager;
        this.cacheEventListenerImpl = cacheEventListenerImpl;

        addCacheEventListeners();
    }

    private void addCacheEventListeners() {
        Arrays.asList(ehCacheManager.getCacheNames()).forEach(cacheName ->
                ehCacheManager.getCache(cacheName)
                        .getCacheEventNotificationService().registerListener(cacheEventListenerImpl)
        );

        log.info("Successfully added CacheEventListeners for all Caches");
    }
}