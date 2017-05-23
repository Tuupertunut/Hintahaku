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
import java.util.Optional;
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

        /* Jos ensisijaista tuotetta ei ole, se tarkoittaa, että vaihtoehdoista
         * ei voitu valita ensisijaista vaihtoehtoa näytettäväksi ostoksen
         * riville. */
        return getEnsisijainenTuote().map(Tuote::getNimi).orElse("Useita vaihtoehtoja");
    }

    @Override
    public String getKaupanNimi() {
        return ostos.getValittuHintatieto().map(Hintatieto::getKaupanNimi).orElse("Ei saatavilla");
    }

    @Override
    public String getToimitusaika() {
        return ostos.getValittuHintatieto().map(Hintatieto::getToimitusaika).orElse("");
    }

    @Override
    public Hinta getKappalehinta() {
        return ostos.getValittuHintatieto().map(Hintatieto::getHinta).orElse(null);
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
    public Optional<Tuote> getEnsisijainenTuote() {
        Optional<Hintatieto> valittuHintatieto = ostos.getValittuHintatieto();

        /* Onko tuotteella valittua hintatietoa? */
        if (valittuHintatieto.isPresent()) {

            /* Palautetaan se tuotevaihtoehto, jossa valittu hintatieto on. */
            return valittuHintatieto.map(Hintatieto::getTuote);

        } else {
            List<Tuote> vaihtoehdot = ostos.getVaihtoehdot();

            /* Jos on vain yksi vaihtoehto, palautetaan se. Muutoin ei ole
             * mitään keinoa päättää, mikä vaihtoehdoista olisi ensisijainen,
             * joten palautetaan tyhjää. */
            if (vaihtoehdot.size() == 1) {
                return Optional.of(vaihtoehdot.get(0));
            } else {
                return Optional.empty();
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
