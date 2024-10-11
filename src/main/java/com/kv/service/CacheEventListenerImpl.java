package com.kv.service;

import lombok.extern.log4j.Log4j2;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheEventListenerImpl implements CacheEventListener<Object, Object> {
    @Override
    public void onEvent(CacheEvent cacheEvent) {
        log.debug("EH Cache Events --> Key: {}, OldValue: {}, NewValue: {}",
                cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
