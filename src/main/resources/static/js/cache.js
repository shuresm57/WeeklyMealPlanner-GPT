const CacheService = (() => {
    const CACHE_PREFIX = 'mealplanner_';
    const CACHE_EXPIRY = 5 * 60 * 1000;
    const memoryCache = new Map();

    function getCacheKey(key) {
        return CACHE_PREFIX + key;
    }

    function isExpired(timestamp) {
        return Date.now() - timestamp > CACHE_EXPIRY;
    }

    function get(key) {
        const cacheKey = getCacheKey(key);
        
        if (memoryCache.has(cacheKey)) {
            const { data, timestamp } = memoryCache.get(cacheKey);
            if (!isExpired(timestamp)) {
                return data;
            }
            memoryCache.delete(cacheKey);
        }

        try {
            const stored = localStorage.getItem(cacheKey);
            if (stored) {
                const { data, timestamp } = JSON.parse(stored);
                if (!isExpired(timestamp)) {
                    memoryCache.set(cacheKey, { data, timestamp });
                    return data;
                }
                localStorage.removeItem(cacheKey);
            }
        } catch (e) {
            console.error('Cache read error:', e);
        }

        return null;
    }

    function set(key, data) {
        const cacheKey = getCacheKey(key);
        const cacheEntry = { data, timestamp: Date.now() };

        memoryCache.set(cacheKey, cacheEntry);

        try {
            localStorage.setItem(cacheKey, JSON.stringify(cacheEntry));
        } catch (e) {
            console.error('Cache write error:', e);
        }
    }

    function invalidate(key) {
        const cacheKey = getCacheKey(key);
        memoryCache.delete(cacheKey);
        try {
            localStorage.removeItem(cacheKey);
        } catch (e) {
            console.error('Cache invalidate error:', e);
        }
    }

    function clear() {
        memoryCache.clear();
        try {
            Object.keys(localStorage)
                .filter(key => key.startsWith(CACHE_PREFIX))
                .forEach(key => localStorage.removeItem(key));
        } catch (e) {
            console.error('Cache clear error:', e);
        }
    }

    return { get, set, invalidate, clear };
})();
