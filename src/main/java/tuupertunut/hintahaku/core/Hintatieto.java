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

import java.util.Objects;
import java.util.Optional;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListenable;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.korjaukset.KorjauksenKohde;
import tuupertunut.hintahaku.korjaukset.KorjausRajapinta;
import tuupertunut.hintahaku.suodattimet.Kauppa;
import tuupertunut.hintahaku.suodattimet.SuodatinRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class Hintatieto implements ChangeListenable {

    private final Tuote tuote;
    private final String kaupanNimi;
    private final Hinta hinta;
    private final Optional<Hinta> postikulut;
    private final String toimitusaika;

    public Hintatieto(Tuote tuote, String kaupanNimi, Hinta hinta, Optional<Hinta> postikulut, String toimitusaika) {
        this.tuote = tuote;
        this.kaupanNimi = kaupanNimi;
        this.hinta = hinta;
        this.postikulut = postikulut;
        this.toimitusaika = toimitusaika;

        /* Välitetään muutos eteenpäin, kun suodattimet tai korjaukset
         * muuttuvat. */
        SuodatinRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("suodattimet")) {
                cs.fireProxyChange(evt, "hinta ja postikulut");
            }
        });

        KorjausRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("korjaukset")) {
                cs.fireProxyChange(evt, "hinta ja postikulut");
            }
        });
    }

    public Tuote getTuote() {
        return tuote;
    }

    public String getKaupanNimi() {
        return kaupanNimi;
    }

    public Hinta getHinta() {
        /* Jos korjauksista löytyy korjattu hinta tälle hintatiedolle,
         * palautetaan se alkuperäisen sijaan. */
        if (KorjausRajapinta.onkoKorjausta(tuote, kaupanNimi, KorjauksenKohde.HINTA)) {
            return KorjausRajapinta.haeKorjaus(tuote, kaupanNimi, KorjauksenKohde.HINTA).getKorjattuHinta();
        } else {
            return hinta;
        }
    }

    public Optional<Hinta> getPostikulut() {
        /* Jos korjauksista löytyy korjatut postikulut tälle hintatiedolle,
         * palautetaan ne alkuperäisten sijaan. */
        if (KorjausRajapinta.onkoKorjausta(tuote, kaupanNimi, KorjauksenKohde.POSTIKULUT)) {
            return Optional.of(KorjausRajapinta.haeKorjaus(tuote, kaupanNimi, KorjauksenKohde.POSTIKULUT).getKorjattuHinta());
        } else {
            return postikulut;
        }
    }

    public String getToimitusaika() {
        return toimitusaika;
    }

    /* Hinta, kun se on suodatettu kaupalle asetetuilla suodattimilla */
    public Optional<Hinta> getSuodatettuHinta() {
        return getSuodatetutPostikulut().map(getHinta()::plus);
    }

    /* Postikulut, kun ne on suodatettu kaupalle asetetuilla suodattimilla */
    public Optional<Hinta> getSuodatetutPostikulut() {
        if (isSuodataPois()) {
            return Optional.empty();
        } else if (isVoiNoutaa()) {
            return Optional.of(new Hinta(0));
        } else {
            return getPostikulut();
        }
    }

    public Optional<Hinta> getHintaKuluineen() {
        return getPostikulut().map(getHinta()::plus);
    }

    public Kauppa getKauppa() {
        return SuodatinRajapinta.haeKauppa(kaupanNimi);
    }

    public boolean isVoiNoutaa() {
        return getKauppa().isVoiNoutaa();
    }

    public boolean isSuodataPois() {
        return getKauppa().isSuodataPois();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.tuote);
        hash = 53 * hash + Objects.hashCode(this.kaupanNimi);
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
        final Hintatieto other = (Hintatieto) obj;
        if (!Objects.equals(this.kaupanNimi, other.kaupanNimi)) {
            return false;
        }
        if (!Objects.equals(this.tuote, other.tuote)) {
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
