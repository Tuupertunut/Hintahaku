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
package tuupertunut.hintahaku.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author Tuupertunut
 */
public class ChangeSupport {

    private final Object source;

    private final Map<Integer, List<ChangeListener>> listeners = new TreeMap<>(Comparator.reverseOrder());

    private ChangeEvent lastEvent = null;

    public ChangeSupport(Object source) {
        this.source = source;
    }

    public void addListener(ChangeListener listener) {
        addListener(0, listener);
    }

    /* 0 is the default priority. Higher values get executed first. */
    public void addListener(int priority, ChangeListener listener) {
        if (!listeners.containsKey(priority)) {
            listeners.put(priority, new ArrayList<>());
        }
        listeners.get(priority).add(listener);
    }

    public void removeListener(ChangeListener listener) {
        for (List<ChangeListener> list : listeners.values()) {
            boolean foundAndRemoved = list.remove(listener);
            if (foundAndRemoved) {
                break;
            }
        }
    }

    public void fireRootChange(String changedProperty, Object oldValue, Object newValue) {
        if (!Objects.equals(newValue, oldValue)) {
            fireChange(new ChangeEvent(source, changedProperty, oldValue, newValue));
        }
    }

    public void fireProxyChange(ChangeEvent cause, String changedProperty) {
        fireChange(new ChangeEvent(source, cause, changedProperty));
    }

    private void fireChange(ChangeEvent event) {

        /* If multiple consecutive events have the same root event (they are all
         * originating from the same event), only fire the first one. This is to
         * avoid event spam from the same source. */
        if (lastEvent == null || !event.getRootEvent().equals(lastEvent.getRootEvent())) {

            for (List<ChangeListener> list : listeners.values()) {
                for (ChangeListener listener : list) {
                    listener.changeHappened(event);
                }
            }

            lastEvent = event;
        }
    }
}
