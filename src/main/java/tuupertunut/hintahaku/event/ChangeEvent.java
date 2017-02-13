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

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tuupertunut
 */
public class ChangeEvent extends EventObject {

    private final List<ChangeEvent> eventChain;
    private final String changedProperty;
    private final Object oldValue;
    private final Object newValue;

    public ChangeEvent(Object source, String changedProperty, Object oldValue, Object newValue) {
        super(source);
        this.eventChain = new LinkedList<>();
        this.changedProperty = changedProperty;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ChangeEvent(Object source, ChangeEvent cause, String changedProperty) {
        super(source);
        this.eventChain = new LinkedList<>(cause.eventChain);
        this.eventChain.add(cause);
        this.changedProperty = changedProperty;
        this.oldValue = null;
        this.newValue = null;
    }

    public boolean isRootEvent() {
        return eventChain.isEmpty();
    }

    public ChangeEvent getRootEvent() {
        return (!eventChain.isEmpty()) ? eventChain.get(0) : this;
    }

    public List<ChangeEvent> getEventChain() {
        return eventChain;
    }

    public String getChangedProperty() {
        return changedProperty;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
