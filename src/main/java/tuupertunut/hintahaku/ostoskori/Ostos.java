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
package tuupertunut.hintahaku.ostoskori;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeListenable;
import tuupertunut.hintahaku.event.ChangeListenableConnector;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Ostos implements ChangeListenable {

    private final ChangeListenableConnector connector = new ChangeListenableConnector();

    private final Ostoskori ostoskori;
    private final EventList<Tuote> vaihtoehdot;
    private int maara;

    public Ostos(Ostoskori ostoskori, Tuote tuote, int maara) {
        this(ostoskori, Collections.singleton(tuote), maara);
    }

    public Ostos(Ostoskori ostoskori, Collection<Tuote> vaihtoehdot, int maara) {
        this.ostoskori = ostoskori;
        this.vaihtoehdot = new ObservableElementList<>(GlazedLists.eventList(vaihtoehdot), connector);
        this.maara = maara;

        this.vaihtoehdot.addListEventListener((ListEvent<Tuote> listChanges) -> cs.fireProxyChange(connector.wrapListEvent(listChanges), "vaihtoehdot"));
    }

    public Ostoskori getOstoskori() {
        return ostoskori;
    }

    public EventList<Tuote> getVaihtoehdot() {
        return vaihtoehdot;
    }

    public int getMaara() {
        return maara;
    }

    public void setMaara(int maara) {
        int oldMaara = this.maara;
        this.maara = maara;
        cs.fireRootChange("maara", oldMaara, maara);
    }

    public Optional<Hintatieto> getHalvinSuodatettuHintatieto() {
        Optional<Hintatieto> halvin = Optional.empty();
        for (Tuote tuote : vaihtoehdot) {
            Optional<Hintatieto> tuotteenHalvin = tuote.getHalvinSuodatettuHintatieto();
            Optional<Hinta> tuotteenHalvinHinta = tuotteenHalvin.flatMap(Hintatieto::getSuodatettuHinta);
            Optional<Hinta> halvinHinta = halvin.flatMap(Hintatieto::getSuodatettuHinta);
            if (tuotteenHalvinHinta.isPresent() && (!halvinHinta.isPresent() || tuotteenHalvinHinta.get().onVahemmanKuin(halvinHinta.get()))) {
                halvin = tuotteenHalvin;
            }
        }
        return halvin;
    }

    public Optional<Hintatieto> getValittuHintatieto() {
        return Optional.ofNullable(ostoskori.getValitutHintatiedot().get(this));
    }

    public boolean onkoSamatVaihtoehdot(Collection<Tuote> vaihtoehdot) {
        return new HashSet<>(this.getVaihtoehdot()).equals(new HashSet<>(vaihtoehdot));
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
