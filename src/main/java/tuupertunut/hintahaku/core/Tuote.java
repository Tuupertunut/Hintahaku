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
package tuupertunut.hintahaku.core;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import tuupertunut.hintahaku.event.ChangeListenable;
import tuupertunut.hintahaku.event.ChangeListenableConnector;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.RaakaHintatieto;

/**
 *
 * @author Tuupertunut
 */
public class Tuote implements ChangeListenable {

    private final ChangeListenableConnector connector = new ChangeListenableConnector();

    private final HintaFiUrl url;
    private final String nimi;
    private final EventList<Hintatieto> hintatiedot;

    public Tuote(HintaFiUrl url, String nimi, Collection<RaakaHintatieto> hintatiedot) {
        this.url = url;
        this.nimi = nimi;

        /* Jos samasta kaupasta on useita hintatietoja, jätetään vain halvin. */
        Map<String, RaakaHintatieto> kaupoittainMap = new LinkedHashMap<>();
        for (RaakaHintatieto r : hintatiedot) {
            kaupoittainMap.merge(r.kaupanNimi, r, (RaakaHintatieto a, RaakaHintatieto b) -> {
                return (a.hinta.onVahemmanKuin(b.hinta)) ? a : b;
            });
        }

        /* Muunnetaan RaakaHintatiedot Hintatiedoiksi. */
        this.hintatiedot = new ObservableElementList<>(new BasicEventList<>(), connector);
        for (RaakaHintatieto r : kaupoittainMap.values()) {
            this.hintatiedot.add(new Hintatieto(this, r.kaupanNimi, r.hinta, r.postikulut, r.toimitusaika));
        }

        this.hintatiedot.addListEventListener((ListEvent<Hintatieto> listChanges) -> cs.fireProxyChange(connector.wrapListEvent(listChanges), "hintatiedot"));
    }

    public HintaFiUrl getUrl() {
        return url;
    }

    public String getNimi() {
        return nimi;
    }

    public EventList<Hintatieto> getHintatiedot() {
        return hintatiedot;
    }

    public Optional<Hintatieto> getHalvinSuodatettuHintatieto() {
        Optional<Hintatieto> halvin = Optional.empty();
        for (Hintatieto ht : hintatiedot) {
            Optional<Hinta> htHinta = ht.getSuodatettuHinta();
            Optional<Hinta> halvinHinta = halvin.flatMap(Hintatieto::getSuodatettuHinta);
            if (htHinta.isPresent() && (!halvinHinta.isPresent() || htHinta.get().onVahemmanKuin(halvinHinta.get()))) {
                halvin = Optional.of(ht);
            }
        }
        return halvin;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.url);
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
        final Tuote other = (Tuote) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    private final ChangeSupport cs = new ChangeSupport(this);

    @Override
    public void addListener(ChangeListener listener) {
        cs.addListener(listener);
    }

    public void addListener(int priority, ChangeListener listener) {
        cs.addListener(priority, listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        cs.removeListener(listener);
    }
}
