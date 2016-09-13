/* 
 * Copyright 2016 François Billioud.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fbillioud.jmath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Bidirectional Map.
 * @author François Billioud
 * @param <K> type of the keys contained in this map
 * @param <V> type of the values contained in this map
 */
public class BidiMap<K, V> implements Map<K, V> {

    /** Keys of the map **/
    private LinkedList<K> keys = new LinkedList<K>();
    /** Values of the map **/
    private LinkedList<V> values = new LinkedList<V>();
    
    /**
     * Create a bidirectional map.
     * To avoid unexpected behaviour, make sure that the arguments have the
     * same size and that keys and values are both unique.
     * @param keys array of map keys
     * @param values array of map values. The order should match the keys
     */
    public BidiMap(K[] keys, V[] values) {
        this.keys.addAll(Arrays.asList(keys));
        this.values.addAll(Arrays.asList(values));
    }
    /**
     * Create a bidirectional map.
     * @param map a classic Map, like a HashMap for instance.
     */
    public BidiMap(Map<K, V> map) {
        putAllKeys(map);
    }
    /**
     * Create an empty bidirectional map.
     */
    public BidiMap() {}

    /**
     * Key count of the map.
     * @return the number of keys in this map.
     */
    @Override
    public int size() {
        return keys.size();
    }

    /**
     * Does this map contains keys?
     * @return true if it does contains a key, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * Check if this map contains the specified key.
     * @param key the key to look for.
     * @return true if the key is found in this map, false otherwise.
     */
    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    /**
     * Check if this map contains the specified value.
     * @param value the value to look for.
     * @return false if the value is found in this map, false otherwise.
     */
    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    /**
     * Get the value associated with this key
     * @param key the key to look for.
     * @return the object associated with the key, or null
     * @see #getKey(K key, V value)
     */
    @Override
    public V get(Object key) {
        return getValue(key);
    }

    /**
     * Get the value associated with this key
     * @param key the key to look for.
     * @return the object associated with the key, or null
     */
    public V getValue(Object key) {
        int index = keys.indexOf(key);
        return index==-1 ? null : values.get(keys.indexOf(key));
    }

    /**
     * Get the key associated with this value
     * @param value the value to look for.
     * @return the object associated with this value, or null
     */
    public K getKey(Object value) {
        int index = values.indexOf(value);
        return index==-1 ? null : keys.get(values.indexOf(value));
    }

    /**
     * Link a key and a value
     * @param key the key for this entry.
     * @param value the value for this entry.
     * @return the previous value associated with the key, or null.
     * @see #putKey(K key, V value)
     */
    @Override
    public V put(K key, V value) {
        return putKey(key, value);
    }

    /**
     * Link a key and a value
     * @param key the key for this entry.
     * @param value the value for this entry.
     * @return the previous value associated with the key, or null.
     */
    public V putKey(K key, V value) {
        int i = keys.indexOf(key);
        if(i!=-1) {
            V oldValue = values.get(i);
            values.set(i, value);
            return oldValue;
        } else {
            //XXX Maybe we should check if the value is present?
            keys.add(key);
            values.add(value);
            return null;
        }
    }

    /**
     * Link a value and a key
     * @param value the value for this entry.
     * @param key the key for this entry.
     * @return the previous key associated with the value, or null.
     * @see #putKey(K key, V value)
     */
    public K putValue(V value, K key) {
        int i = values.indexOf(value);
        if(i!=-1) {
            K oldKey = keys.get(i);
            keys.set(i, key);
            return oldKey;
        } else {
            //XXX Maybe we should check if the key is present?
            keys.add(key);
            values.add(value);
            return null;
        }
    }

    /**
     * Removes an entry from the map.
     * @param key the key to remove
     * @return the object previously associated with this key
     * @see #removeKey(Object key)
     */
    @Override
    public V remove(Object key) {
        return removeKey(key);
    }

    /**
     * Removes an entry from the map.
     * @param key the key to remove
     * @return the object previously associated with this key
     */
    public V removeKey(Object key) {
        int i = keys.indexOf(key);
        if(i==-1) {return null;}
        keys.remove(i);
        return values.remove(i);
    }

    /**
     * Removes an entry from the map.
     * @param value the key to remove
     * @return the key previously associated with this value
     * @see #removeKey(Object key)
     */
    public K removeValue(Object value) {
        int i = values.indexOf(value);
        if(i==-1) {return null;}
        values.remove(i);
        return keys.remove(i);
    }

    /**
     * Put all of the key-value entries in this BidiMap.
     * @param m the map to copy the entries from
     * @see #putAllKeys(Map<? extends K, ? extends V> m)
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAllKeys(m);
    }

    /**
     * Put by key all of the key-value entries in this BidiMap.
     * @param m the map to copy the entries from
     */
    public void putAllKeys(Map<? extends K, ? extends V> m) {
        for(Entry<? extends K, ? extends V> entry : m.entrySet()) {
            putKey(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Put by value all of the value-key entries in this BidiMap.
     * @param m the map to copy the entries from
     * @see #putAllKeys(Map<? extends K, ? extends V> m)
     */
    public void putAllValues(Map<? extends V, ? extends K> m) {
        for(Entry<? extends V, ? extends K> entry : m.entrySet()) {
            putValue(entry.getKey(), entry.getValue());
        }
    }

    /** Clear this map. **/
    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    /**
     * Get all keys as a set.
     * @return a set of all keys
     */
    @Override
    public Set<K> keySet() {
        return new HashSet<K>(keys);
    }

    /**
     * Get all values as a set.
     * @return a set of all values
     */
    @Override
    public Set<V> values() {
        return new HashSet<V>(values);
    }

    /**
     * Get all entries as a set.
     * @return a set of all entries
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = new HashSet<Entry<K, V>>();
        ListIterator<K> iterKey = keys.listIterator();
        ListIterator<V> iterValue = values.listIterator();
        while(iterKey.hasNext()) {
            set.add(new EntryImpl(iterKey.next(), iterValue.next()));
        }
        return set;
    }
    
    /**
     * A BidiMap entry.
     * @param <K> type of the key of this entry
     * @param <V> type of the value of this entry
     */
    private static class EntryImpl<K, V> implements Entry<K, V> {
        private K key;
        private V value;
        public EntryImpl(K key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }
}
