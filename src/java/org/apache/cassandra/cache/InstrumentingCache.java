package org.apache.cassandra.cache;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wraps an ICache in requests + hits tracking.
 */
public class InstrumentingCache<K, V>
{
    private final AtomicLong requests = new AtomicLong(0);
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong lastRequests = new AtomicLong(0);
    private final AtomicLong lastHits = new AtomicLong(0);
    private volatile boolean capacitySetManually;
    private final ICache<K, V> map;

    public InstrumentingCache(ICache<K, V> map)
    {
        this.map = map;
    }

    public void put(K key, V value)
    {
        map.put(key, value);
    }

    public V get(K key)
    {
        V v = map.get(key);
        requests.incrementAndGet();
        if (v != null)
            hits.incrementAndGet();
        return v;
    }

    public V getInternal(K key)
    {
        return map.get(key);
    }

    public void remove(K key)
    {
        map.remove(key);
    }

    public int getCapacity()
    {
        return map.capacity();
    }

    public boolean isCapacitySetManually()
    {
        return capacitySetManually;
    }

    public void updateCapacity(int capacity)
    {
        map.setCapacity(capacity);
    }

    public void setCapacity(int capacity)
    {
        updateCapacity(capacity);
        capacitySetManually = true;
    }

    public int size()
    {
        return map.size();
    }

    public int weightedSize()
    {
        return map.weightedSize();
    }

    public long getHits()
    {
        return hits.get();
    }

    public long getRequests()
    {
        return requests.get();
    }

    public double getRecentHitRate()
    {
        long r = requests.get();
        long h = hits.get();
        try
        {
            return ((double)(h - lastHits.get())) / (r - lastRequests.get());
        }
        finally
        {
            lastRequests.set(r);
            lastHits.set(h);
        }
    }

    public void clear()
    {
        map.clear();
        requests.set(0);
        hits.set(0);
    }

    public Set<K> getKeySet()
    {
        return map.keySet();
    }

    public Set<K> hotKeySet(int n)
    {
        return map.hotKeySet(n);
    }

    public boolean containsKey(K key)
    {
        return map.containsKey(key);
    }

    public boolean isPutCopying()
    {
        return map.isPutCopying();
    }
}
