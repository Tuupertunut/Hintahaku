/*
 * The MIT License
 *
 * Copyright 2017 Tuupertunut.
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
package tuupertunut.hintahaku.hintatiedot;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import java.util.Optional;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class HintatiedotRajapinta {

    private static final ChangeSupport CS = new ChangeSupport(HintatiedotRajapinta.class);

    private static final EventList<Hintatieto> LISTA;
    private static final ChangeListener TUOTE_PROXY_LISTENER;

    private static Optional<Tuote> tuote;
    private static Optional<Hintatieto> valittuHintatieto;

    static {
        tuote = Optional.empty();
        valittuHintatieto = Optional.empty();

        TUOTE_PROXY_LISTENER = (ChangeEvent evt) -> CS.fireProxyChange(evt, "tuote");
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("tuote") && evt.isRootEvent()) {

                Optional<Tuote> oldTuote = (Optional<Tuote>) evt.getOldValue();
                if (oldTuote.isPresent()) {
                    oldTuote.get().removeListener(TUOTE_PROXY_LISTENER);
                }

                if (tuote.isPresent()) {
                    tuote.get().addListener(TUOTE_PROXY_LISTENER);
                }
            }
        });

        LISTA = new BasicEventList<>();
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("tuote")) {

                LISTA.clear();
                if (tuote.isPresent()) {
                    LISTA.addAll(tuote.get().getHintatiedot());
                }
            }
        });
    }

    public static void naytaTuote(Optional<Tuote> tuote) {
        naytaTuote(tuote, Optional.empty());
    }

    public static void naytaTuote(Optional<Tuote> tuote, Optional<Hintatieto> valittuHintatieto) {
        setTuote(tuote);
        setValittuHintatieto(valittuHintatieto);
    }

    public static Optional<Tuote> getTuote() {
        return tuote;
    }

    public static boolean onkoTuotetta() {
        return tuote.isPresent();
    }

    private static void setTuote(Optional<Tuote> tuote) {
        Optional<Tuote> oldTuote = HintatiedotRajapinta.tuote;
        HintatiedotRajapinta.tuote = tuote;
        CS.fireRootChange("tuote", oldTuote, tuote);
    }

    private static void setValittuHintatieto(Optional<Hintatieto> valittuHintatieto) {
        Optional<Hintatieto> oldValittuHintatieto = HintatiedotRajapinta.valittuHintatieto;
        HintatiedotRajapinta.valittuHintatieto = valittuHintatieto;
        CS.fireRootChange("valittuHintatieto", oldValittuHintatieto, valittuHintatieto);
    }

    public static boolean onkoValittuHintatieto(Hintatieto hintatieto) {
        return valittuHintatieto.isPresent() && hintatieto.equals(valittuHintatieto.get());
    }

    public static boolean onkoHalvinHintatieto(Hintatieto hintatieto) {
        Optional<Hintatieto> halvinHintatieto = tuote.flatMap(Tuote::getHalvinSuodatettuHintatieto);
        return halvinHintatieto.isPresent() && hintatieto.equals(halvinHintatieto.get());
    }

    public static EventList<Hintatieto> getEventList() {
        return LISTA;
    }

    public static void addListener(ChangeListener listener) {
        CS.addListener(listener);
    }

    public static void addListener(int priority, ChangeListener listener) {
        CS.addListener(priority, listener);
    }

    public static void removeListener(ChangeListener listener) {
        CS.removeListener(listener);
    }
}
