/*
 * The MIT License
 *
 * Copyright 2016 Tuupertunut.
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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Tuupertunut
 */
public class ObservableElementEventThreeKeyHashMap<K1, K2, K3, V> {

    private final EventList<V> eventList;
    private final Map<ThreeKey<K1, K2, K3>, V> map;

    public ObservableElementEventThreeKeyHashMap(Function<V, K1> key1Maker, Function<V, K2> key2Maker, Function<V, K3> key3Maker, ObservableElementList.Connector<? super V> connector) {
        this(null, key1Maker, key2Maker, key3Maker, connector);
    }

    public ObservableElementEventThreeKeyHashMap(Collection<V> c, Function<V, K1> key1Maker, Function<V, K2> key2Maker, Function<V, K3> key3Maker, ObservableElementList.Connector<? super V> connector) {
        EventList<V> basicList = GlazedLists.eventList(c);
        eventList = new ObservableElementList<>(basicList, connector);
        map = GlazedLists.syncEventListToMap(basicList, (V value) -> new ThreeKey<>(key1Maker.apply(value), key2Maker.apply(value), key3Maker.apply(value)));
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

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKeys(K1 key1, K2 key2, K3 key3) {
        return map.containsKey(new ThreeKey<>(key1, key2, key3));
    }

    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    public V get(K1 key1, K2 key2, K3 key3) {
        return map.get(new ThreeKey<>(key1, key2, key3));
    }

    public V put(K1 key1, K2 key2, K3 key3, V value) {
        return map.put(new ThreeKey<>(key1, key2, key3), value);
    }

    public V remove(K1 key1, K2 key2, K3 key3) {
        return map.remove(new ThreeKey<>(key1, key2, key3));
    }

    public void clear() {
        map.clear();
    }

    public Collection<V> values() {
        return map.values();
    }

    public V getOrDefault(K1 key1, K2 key2, K3 key3, V defaultValue) {
        return map.getOrDefault(new ThreeKey<>(key1, key2, key3), defaultValue);
    }

    public V putIfAbsent(K1 key1, K2 key2, K3 key3, V value) {
        return map.putIfAbsent(new ThreeKey<>(key1, key2, key3), value);
    }

    public boolean remove(K1 key1, K2 key2, K3 key3, Object value) {
        return map.remove(new ThreeKey<>(key1, key2, key3), value);
    }

    public boolean replace(K1 key1, K2 key2, K3 key3, V oldValue, V newValue) {
        return map.replace(new ThreeKey<>(key1, key2, key3), oldValue, newValue);
    }

    public V replace(K1 key1, K2 key2, K3 key3, V value) {
        return map.replace(new ThreeKey<>(key1, key2, key3), value);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.map);
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
        final ObservableElementEventThreeKeyHashMap<?, ?, ?, ?> other = (ObservableElementEventThreeKeyHashMap<?, ?, ?, ?>) obj;
        if (!Objects.equals(this.map, other.map)) {
            return false;
        }
        return true;
    }
}
