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

import ca.odell.glazedlists.ObservableElementChangeHandler;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import java.util.EventListener;

/**
 *
 * @author Tuupertunut
 */
public class ChangeListenableConnector implements ObservableElementList.Connector<ChangeListenable> {

    private ObservableElementChangeHandler<? extends ChangeListenable> list;

    /* A hack to pass the event that caused a list change to the list change
     * listeners. ListChangeEvent doesn't support passing events through, so
     * instead the latest causing event is always stored here into this
     * variable, and cleared out when a list event listener has consumed it. */
    private ChangeEvent passedEvent = null;

    @Override
    public EventListener installListener(ChangeListenable element) {
        ChangeListener listener = (ChangeEvent evt) -> {

            /* Storing the last event into a variable. */
            passedEvent = evt;

            list.elementChanged(element);
        };
        element.addListener(listener);
        return listener;
    }

    @Override
    public void uninstallListener(ChangeListenable element, EventListener listener) {
        element.removeListener((ChangeListener) listener);
    }

    @Override
    public void setObservableElementList(ObservableElementChangeHandler<? extends ChangeListenable> list) {
        this.list = list;
    }

    private boolean hasPassedEvent() {
        return passedEvent != null;
    }

    /* Retrieving the stored event and clearing the variable. */
    private ChangeEvent consumePassedEvent() {
        ChangeEvent event = passedEvent;
        passedEvent = null;
        return event;
    }

    /* This is meant to be called from a list event listener only once per
     * event. This converts the ListEvent to a ChangeEvent. */
    public ChangeEvent wrapListEvent(ListEvent<?> listEvent) {

        /* If this connector has a passed event stored, it means that the
         * ListEvent was caused by a ChangeEvent in one of the elements of the
         * list. In that case, a proxy event is returned with the passed event
         * as the cause. Otherwise, it means the list was structurally changed
         * and a root event is returned. */
        if (hasPassedEvent()) {
            return new ChangeEvent(listEvent.getSource(), consumePassedEvent(), "list update");
        } else {
            listEvent.next();

            /* Testing, if the list event has more than one change. */
            if (!listEvent.hasNext()) {
                switch (listEvent.getType()) {
                    case ListEvent.UPDATE:
                        return new ChangeEvent(listEvent.getSource(), "list update", "unknown value", "unknown value");
                    case ListEvent.DELETE:
                        return new ChangeEvent(listEvent.getSource(), "list delete", "unknown value", null);
                    case ListEvent.INSERT:
                        return new ChangeEvent(listEvent.getSource(), "list insert", null, "unknown value");
                    default:
                        throw new IllegalArgumentException();
                }
            } else {
                return new ChangeEvent(listEvent.getSource(), "list update", "multiple unknown values", "multiple unknown values");
            }
        }
    }
}
