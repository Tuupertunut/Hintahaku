/*
 * The MIT License
 *
 * Copyright 2016 ENURMI.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tuupertunut.hintahaku.util;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author ENURMI
 */
public class ObservableElementEventHashMap<K, V> implements Map<K, V> {

    private final EventList<V> eventList;
    private final Map<K, V> map;

    public ObservableElementEventHashMap(Function<V, K> keyMaker, ObservableElementList.Connector<? super V> connector) {
        this(null, keyMaker, connector);
    }

    public ObservableElementEventHashMap(Collection<V> c, Function<V, K> keyMaker, ObservableElementList.Connector<? super V> connector) {
        EventList<V> basicList = GlazedLists.eventList(c);
        eventList = new ObservableElementList<>(basicList, connector);
        map = GlazedLists.syncEventListToMap(basicList, keyMaker::apply);
    }

    public ObservableElementEventHashMap(Function<V, K> keyMaker, ObservableElementList.Connector<? super V> connector, ListEventPublisher publisher, ReadWriteLock lock) {
        this(null, keyMaker, connector, publisher, lock);
    }

    public ObservableElementEventHashMap(Collection<V> c, Function<V, K> keyMaker, ObservableElementList.Connector<? super V> connector, ListEventPublisher publisher, ReadWriteLock lock) {
        EventList<V> basicList = GlazedLists.eventList(publisher, lock, c);
        eventList = new ObservableElementList<>(basicList, connector);
        map = GlazedLists.syncEventListToMap(basicList, keyMaker::apply);
    }

    public EventList<V> getEventList() {
        return eventList;
    }

    public void addListEventListener(ListEventListener<? super V> listChangeListener) {
        eventList.addListEventListener(listChangeListener);
    }

    public void removeListEventListener(ListEventListener<? super V> listChangeListener) {
        eventList.removeListEventListener(listChangeListener);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        map.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.map);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObservableElementEventHashMap<?, ?> other = (ObservableElementEventHashMap<?, ?>) obj;
        if (!Objects.equals(this.map, other.map)) {
            return false;
        }
        return true;
    }
}
