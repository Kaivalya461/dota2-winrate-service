package com.kv.service;

import lombok.extern.log4j.Log4j2;
//import org.ehcache.event.CacheEvent;
//import org.ehcache.event.CacheEventListener;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheEventListenerImpl implements CacheEventListener {
    @Override
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        log.info("EH Cache notifyElementRemoved Events --> Key: {}, Value: {}, ehCacheName: {}",
                element.getObjectKey(), element.getObjectValue(), ehcache.getName());
    }

    @Override
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        log.info("EH Cache notifyElementPut Events --> Key: {}, Value: {}, ehCacheName: {}",
                element.getObjectKey(), element.getObjectValue(), ehcache.getName());
    }

    @Override
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        log.info("EH Cache notifyElementUpdated Events --> Key: {}, Value: {}, ehCacheName: {}",
                element.getObjectKey(), element.getObjectValue(), ehcache.getName());
    }

    @Override
    public void notifyElementExpired(Ehcache ehcache, Element element) {
        log.info("EH Cache notifyElementExpired Events --> Key: {}, Value: {}, ehCacheName: {}",
                element.getObjectKey(), element.getObjectValue(), ehcache.getName());
    }

    @Override
    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        log.info("EH Cache notifyElementEvicted Events --> Key: {}, Value: {}, ehCacheName: {}",
                element.getObjectKey(), element.getObjectValue(), ehcache.getName());
    }

    @Override
    public void notifyRemoveAll(Ehcache ehcache) {

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public void dispose() {

    }

    // ...
//    @Override
//    public void onEvent(
//            CacheEvent<? extends Object, ? extends Object> cacheEvent) {
//        log.info("EH Cache Events --> Key: {}, OldValue: {}, NewValue: {}",
//                cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
//    }
}
