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
package tuupertunut.hintahaku.korjaukset;

import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.HintaFiUrl;
import tuupertunut.hintahaku.event.ChangeListenable;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Korjaus implements ChangeListenable {

    private final HintaFiUrl tuoteUrl;
    private final String tuoteNimi;
    private final String kaupanNimi;
    private final KorjauksenKohde kohde;
    private Hinta korjattuHinta;

    public Korjaus(HintaFiUrl tuoteUrl, String tuoteNimi, String kaupanNimi, KorjauksenKohde kohde, Hinta korjattuHinta) {
        this.tuoteUrl = tuoteUrl;
        this.tuoteNimi = tuoteNimi;
        this.kaupanNimi = kaupanNimi;
        this.kohde = kohde;
        this.korjattuHinta = korjattuHinta;
    }

    public HintaFiUrl getTuoteUrl() {
        return tuoteUrl;
    }

    public String getTuoteNimi() {
        return tuoteNimi;
    }

    public String getKaupanNimi() {
        return kaupanNimi;
    }

    public KorjauksenKohde getKohde() {
        return kohde;
    }

    public Hinta getKorjattuHinta() {
        return korjattuHinta;
    }

    public void setKorjattuHinta(Hinta korjattuHinta) {
        Hinta oldKorjattuHinta = this.korjattuHinta;
        this.korjattuHinta = korjattuHinta;
        cs.fireRootChange("korjattuHinta", oldKorjattuHinta, korjattuHinta);
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
