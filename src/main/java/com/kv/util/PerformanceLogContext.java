package com.kv.util;

import java.util.HashMap;

public class PerformanceLogContext {
    private static final ThreadLocal<HashMap<String, Object>> mapThreadLocal;

    static {
        mapThreadLocal = ThreadLocal.withInitial(HashMap::new);
    }

    public static HashMap<String, Object> getPerformanceLogContext() {
        return mapThreadLocal.get();
    }
    public static void setPerformanceLogContext(String key, Object value) {
        mapThreadLocal.get().put(key, value);
    }
}
