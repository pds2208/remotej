package org.remotej.server;

import java.util.Date;
import java.util.Hashtable;

public class RemoteJRegistry {
    private static final Hashtable<String, Cache> registry = new Hashtable<String, Cache>();

    public RemoteJRegistry() {
//      if (cleanupThread == null) {
//         cleanupThread = startCleanupThread();
//      }
    }

    public void put(String s, Object o) {
        Cache c = new Cache(o);
        registry.put(s, c);
    }

    public Object get(String key) {
        Cache c = registry.get(key);
        if (c == null) {
            return null;
        }
        return c.getObject();
    }

    public class Cache {
        private Object object;
        private Date lastAccessed;

        Cache(Object object) {
            lastAccessed = new Date();
            this.object = object;
        }

        public Object getObject() {
            lastAccessed = new Date();
            return object;
        }

        public Date getLastAccessTime() {
            return lastAccessed;
        }
    }
}
