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

import java.util.Collections;
import java.util.List;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;

/**
 *
 * @author Tuupertunut
 */
public class OstosRivi implements OstoskoriRivi {

    private final Ostos ostos;

    public OstosRivi(Ostos ostos) {
        this.ostos = ostos;
    }

    @Override
    public String getTuotteenNimi() {
        Tuote ensisijainenTuote = getEnsisijainenTuote();

        if (ensisijainenTuote != null) {
            return ensisijainenTuote.getNimi();
        } else {

            /* Jos ensisijainen tuote on null, se tarkoittaa, että
             * vaihtoehdoista ei voitu valita ensisijaista vaihtoehtoa
             * näytettäväksi ostoksen riville. */
            return "Useita vaihtoehtoja";
        }
    }

    @Override
    public String getKaupanNimi() {
        Hintatieto valittuHintatieto = ostos.getValittuHintatieto();
        if (valittuHintatieto != null) {
            return valittuHintatieto.getKaupanNimi();
        } else {
            return "Ei saatavilla";
        }
    }

    @Override
    public String getToimitusaika() {
        Hintatieto valittuHintatieto = ostos.getValittuHintatieto();
        if (valittuHintatieto != null) {
            return valittuHintatieto.getToimitusaika();
        } else {
            return "";
        }
    }

    @Override
    public Hinta getKappalehinta() {
        Hintatieto valittuHintatieto = ostos.getValittuHintatieto();
        if (valittuHintatieto != null) {
            return valittuHintatieto.getHinta();
        } else {
            return null;
        }
    }

    @Override
    public Integer getMaara() {
        return ostos.getMaara();
    }

    @Override
    public void setMaara(int maara) {
        ostos.setMaara(maara);
    }

    @Override
    public Tuote getEnsisijainenTuote() {
        Hintatieto valittuHintatieto = ostos.getValittuHintatieto();

        /* Onko tuotteella valittua hintatietoa? */
        if (valittuHintatieto != null) {

            /* Palautetaan se tuotevaihtoehto, jossa valittu hintatieto on. */
            return valittuHintatieto.getTuote();

        } else {
            List<Tuote> vaihtoehdot = ostos.getVaihtoehdot();

            /* Jos on vain yksi vaihtoehto, palautetaan se. Muutoin ei ole
             * mitään keinoa päättää, mikä vaihtoehdoista olisi ensisijainen,
             * joten palautetaan null. */
            if (vaihtoehdot.size() == 1) {
                return vaihtoehdot.get(0);
            } else {
                return null;
            }
        }
    }

    @Override
    public Ostos getOstos() {
        return ostos;
    }

    @Override
    public List<OstoskoriRivi> getPath() {
        return Collections.singletonList(this);
    }
}
