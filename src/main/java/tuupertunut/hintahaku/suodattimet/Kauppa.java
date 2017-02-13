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
package tuupertunut.hintahaku.suodattimet;

import tuupertunut.hintahaku.event.ChangeListenable;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Kauppa implements ChangeListenable {

    private final String kaupanNimi;
    private boolean voiNoutaa;
    private boolean suodataPois;

    public Kauppa(String kaupanNimi) {
        this(kaupanNimi, false, false);
    }

    public Kauppa(String kaupanNimi, boolean voiNoutaa, boolean suodataPois) {
        this.kaupanNimi = kaupanNimi;
        this.voiNoutaa = voiNoutaa;
        this.suodataPois = suodataPois;
    }

    public String getKaupanNimi() {
        return kaupanNimi;
    }

    public boolean isVoiNoutaa() {
        return voiNoutaa;
    }

    public void setVoiNoutaa(boolean voiNoutaa) {
        boolean oldVoiNoutaa = this.voiNoutaa;
        this.voiNoutaa = voiNoutaa;
        cs.fireRootChange("voiNoutaa", oldVoiNoutaa, voiNoutaa);
    }

    public boolean isSuodataPois() {
        return suodataPois;
    }

    public void setSuodataPois(boolean suodataPois) {
        boolean oldSuodataPois = this.suodataPois;
        this.suodataPois = suodataPois;
        cs.fireRootChange("suodataPois", oldSuodataPois, suodataPois);
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
