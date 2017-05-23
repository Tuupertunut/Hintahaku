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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListener;

/**
 *
 * @author Tuupertunut
 */
public class JoukkotilausAlgoritmi {

    private static final ChangeListener OSTOSKORI_LISTENER = (ChangeEvent evt) -> {
        if (evt.getChangedProperty().equals("ostokset")) {
            parhaatHinnatValimuisti = null;
        }
    };

    private static Ostoskori viimeisinOstoskori = null;
    private static Map<Ostos, Hintatieto> parhaatHinnatValimuisti = null;

    /* Parhaiden hintojen laskemisalgoritmi voi olla erittäin raskas tietyissä
     * tilanteissa. Tämän vuoksi mieluummin pidetään viimeisintä valmista
     * tulosta välimuistissa, ja lasketaan se uudelleen vain, kun on pakko.
     * Välimuisti tyhjennetään, kun ostoskori on muuttunut toiseksi olioksi, tai
     * sen ostokset ovat muuttuneet. */
    public static Map<Ostos, Hintatieto> haeParhaatHinnat(Ostoskori ostoskori) {
        if (!ostoskori.equals(viimeisinOstoskori)) {
            if (viimeisinOstoskori != null) {
                viimeisinOstoskori.removeListener(OSTOSKORI_LISTENER);
            }

            /* Lisätään listener suurimmalla mahdollisella prioriteetilla, jotta
             * se varmasti ehtii tyhjentää välimuistin, ennen kuin ostoskori
             * alkaa kyselemään parhaita hintoja. */
            ostoskori.addListener(Integer.MAX_VALUE, OSTOSKORI_LISTENER);

            viimeisinOstoskori = ostoskori;
            parhaatHinnatValimuisti = null;
        }

        if (parhaatHinnatValimuisti == null) {
            parhaatHinnatValimuisti = laskeParhaatHinnat(ostoskori);
        }
        return parhaatHinnatValimuisti;
    }

    private static Map<Ostos, Hintatieto> laskeParhaatHinnat(Ostoskori ostoskori) {

        Map<Ostos, List<Hintatieto>> ostoksenHintatiedot = new LinkedHashMap<>();

        /* Lisätään ostoksille niiden hintatiedot. */
        for (Ostos ostos : ostoskori.getOstokset()) {

            /* Lisätään kaikkien ostoksen tuotevaihtoehtojen hintatiedot yhteen
             * yhdeksi listaksi. */
            List<Hintatieto> yhdistetytHintatiedot = new ArrayList<>();
            for (Tuote vaihtoehto : ostos.getVaihtoehdot()) {
                yhdistetytHintatiedot.addAll(vaihtoehto.getHintatiedot());
            }

            /* Poistetaan hintatiedot, joiden suodatettua hintaa ei ole
             * määritetty (kauppa on suodatettu pois tai postikuluja ei
             * tiedetä). */
            yhdistetytHintatiedot.removeIf((Hintatieto ht) -> !ht.getSuodatettuHinta().isPresent());

            /* Optimointi: Poistetaan ostoksesta ne hintatiedot, joiden
             * postikuluton hinta on suurempi kuin halvimman hintatiedon
             * suodatettu (postikulullinen) hinta. */
            Optional<Hinta> halvinSuodatettu = ostos.getHalvinSuodatettuHintatieto().flatMap(Hintatieto::getSuodatettuHinta);
            if (halvinSuodatettu.isPresent()) {
                yhdistetytHintatiedot.removeIf((Hintatieto ht) -> ht.getHinta().onEnemmanKuin(halvinSuodatettu.get()));
            }

            /* Jos hintatietolistassa on hintatietoja jäljellä, lisätään ne
             * mappiin. */
            if (!yhdistetytHintatiedot.isEmpty()) {
                ostoksenHintatiedot.put(ostos, yhdistetytHintatiedot);
            }
        }

        Map<Ostos, Hintatieto> halvinYhdistelma = null;

        List<Ostos> ostokset = new ArrayList<>(ostoksenHintatiedot.keySet());
        List<List<Hintatieto>> hintatietoListat = new ArrayList<>(ostoksenHintatiedot.values());

        /* Käydään läpi kaikki mahdolliset hintatietoyhdistelmät ja valitaan
         * niistä halvin. Tämä prosessi on erittäin raskas suurilla tuote- ja
         * hintatietomäärillä, joten aiemmassa vaiheessa tehty optimointi on
         * hyvin tärkeä. Esimerkkinä 15 kpl 10:n hintatiedon tuotteita vaatisi
         * ilman optimointia jopa tuhat biljoonaa kokeilua. */
        for (List<Hintatieto> yhdistelmanHintatiedot : Lists.cartesianProduct(hintatietoListat)) {

            Map<Ostos, Hintatieto> yhdistelma = new HashMap<>();

            /* Muodostetaan yhdistelmä hintatietoja. Tiedetään, että
             * yhdistelmanHintatiedot- ja ostokset-listojen indexit vastaavat
             * toisiaan. */
            for (int i = 0; i < ostokset.size(); i++) {
                yhdistelma.put(ostokset.get(i), yhdistelmanHintatiedot.get(i));
            }

            /* Testataan, onko muodostettu yhdistelmä halvempi kuin tähän asti
             * halvin yhdistelmä. */
            if (halvinYhdistelma == null || kokonaisHinta(yhdistelma).onVahemmanKuin(kokonaisHinta(halvinYhdistelma))) {
                halvinYhdistelma = yhdistelma;
            }
        }

        return halvinYhdistelma;
    }

    private static Hinta kokonaisHinta(Map<Ostos, Hintatieto> yhdistelma) {
        Hinta kokonaisHinta = new Hinta(0);

        Map<String, Hinta> kalleimmatPostikulut = new HashMap<>();

        for (Map.Entry<Ostos, Hintatieto> e : yhdistelma.entrySet()) {
            Ostos ostos = e.getKey();
            Hintatieto ht = e.getValue();

            /* Lisätään hinta kerrottuna tuotteiden määrällä kokonaishintaan. */
            kokonaisHinta = kokonaisHinta.plus(ht.getHinta().kertaa(ostos.getMaara()));

            /* Tässä vaiheessa tiedetään, että Optional ei voi olla tyhjä. */
            Hinta postikulut = ht.getSuodatetutPostikulut().get();
            String kaupanNimi = ht.getKaupanNimi();

            /* Laitetaan muistiin kaupan postikulut. Jos kaupan postikulut on jo
             * muistissa, säilytetään vain kalleimmat postikulut. */
            if (!kalleimmatPostikulut.containsKey(kaupanNimi) || postikulut.onEnemmanKuin(kalleimmatPostikulut.get(kaupanNimi))) {
                kalleimmatPostikulut.put(kaupanNimi, postikulut);
            }
        }

        /* Lisätään kaikkien kauppojen postikulut kokonaishintaan. */
        for (Hinta postikulut : kalleimmatPostikulut.values()) {
            kokonaisHinta = kokonaisHinta.plus(postikulut);
        }

        return kokonaisHinta;
    }
}
