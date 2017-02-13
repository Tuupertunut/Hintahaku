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
import ca.odell.glazedlists.PluggableList;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.JDOMException;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.XML;

/**
 *
 * @author Tuupertunut
 */
public class OstoskoriRajapinta {

    private static final ChangeSupport CS = new ChangeSupport(OstoskoriRajapinta.class);

    private static final PluggableList<Ostos> PLUGGABLE;
    private static final ChangeListener PROXY_LISTENER;

    private static Path ostoskoritiedosto = null;

    private static Ostoskori ostoskori;

    static {
        ostoskori = new Ostoskori();

        /* Kun ostoskori-olio vaihtuu, poistetaan Listener vanhasta ostoskorista
         * ja lisätään se uuteen. */
        PROXY_LISTENER = (ChangeEvent evt) -> CS.fireProxyChange(evt, "ostoskori");
        ostoskori.addListener(PROXY_LISTENER);
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskori") && evt.isRootEvent()) {

                Ostoskori oldOstoskori = (Ostoskori) evt.getOldValue();
                oldOstoskori.removeListener(PROXY_LISTENER);

                ostoskori.addListener(PROXY_LISTENER);
            }
        });

        /* Muodostetaan EventList, jonka sisältö vastaa aina senhetkisen
         * ostoskorin EventListiä. */
        PLUGGABLE = new PluggableList<>(OstoskoriEventListUtils.PUBLISHER, OstoskoriEventListUtils.LOCK);
        PLUGGABLE.setSource(ostoskori.getOstokset());
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskori") && evt.isRootEvent()) {

                PLUGGABLE.setSource(ostoskori.getOstokset());
            }
        });
    }

    /* Onko ostoskorissa ostosta, joka sisältää ainoana vaihtoehtona syötetyn
     * tuotteen? */
    public static boolean onkoOstosta(Tuote ainoaVaihtoehto) {
        return onkoOstosta(Collections.singleton(ainoaVaihtoehto));
    }

    /* Onko ostoskorissa ostosta, jonka vaihtoehtoina on syötetyt vaihtoehdot? */
    public static boolean onkoOstosta(Collection<Tuote> vaihtoehdot) {
        for (Ostos ostos : ostoskori.getOstokset()) {
            if (ostos.onkoSamatVaihtoehdot(vaihtoehdot)) {
                return true;
            }
        }

        return false;
    }

    /* Onko syötetyssä ostoksessa syötettyä vaihtoehtoa? */
    public static boolean onkoVaihtoehtoa(Ostos ostos, Tuote vaihtoehto) {
        return ostos.getVaihtoehdot().contains(vaihtoehto);
    }

    public static void poistaRivi(OstoskoriRivi rivi) {
        if (rivi instanceof OstosRivi) {
            poistaOstos(rivi.getOstos());
        } else if (rivi instanceof VaihtoehtoRivi) {
            poistaVaihtoehto(rivi.getOstos(), rivi.getEnsisijainenTuote());
        }
    }

    public static void poistaOstos(Ostos ostos) {
        ostoskori.poistaOstos(ostos);
    }

    public static void poistaVaihtoehto(Ostos ostos, Tuote vaihtoehto) {
        List<Tuote> vaihtoehdot = ostos.getVaihtoehdot();

        if (vaihtoehdot.contains(vaihtoehto)) {
            if (vaihtoehdot.size() == 1) {
                poistaOstos(ostos);
            } else {

                /* Ylimääräinen tarkistus: Tulisiko ostoksesta vaihtoehdon
                 * poistamisen jälkeen samanlainen jonkun muun ostoksen kanssa.
                 * Jos tulisi, poistetaan koko ostos. */
                List<Tuote> kokeiluVaihtoehdot = new ArrayList<>(vaihtoehdot);
                kokeiluVaihtoehdot.remove(vaihtoehto);

                if (onkoOstosta(kokeiluVaihtoehdot)) {
                    poistaOstos(ostos);
                } else {
                    vaihtoehdot.remove(vaihtoehto);
                }
            }
        }
    }

    public static void lisaaOstos(Tuote ainoaVaihtoehto) {
        if (!onkoOstosta(ainoaVaihtoehto)) {
            ostoskori.lisaaOstos(new Ostos(ostoskori, ainoaVaihtoehto, 1));
        }
    }

    public static void lisaaVaihtoehto(Ostos ostos, Tuote vaihtoehto) {
        if (!onkoVaihtoehtoa(ostos, vaihtoehto)) {
            ostos.getVaihtoehdot().add(vaihtoehto);
        }
    }

    public static Hinta getKokonaisHinta() {
        Hinta kokonaisHinta = new Hinta(0);

        Map<String, Hinta> kalleimmatPostikulut = new HashMap<>();

        for (Ostos ostos : ostoskori.getOstokset()) {
            Hintatieto ht = ostos.getValittuHintatieto();

            /* Huomioidaan vain ne ostokset, jotka on oikeasti mahdollista
             * ostaa. */
            if (ht != null) {

                /* Lisätään hinta kerrottuna tuotteiden määrällä
                 * kokonaishintaan. */
                kokonaisHinta = kokonaisHinta.plus(ht.getHinta().kertaa(ostos.getMaara()));

                /* Laitetaan muistiin kaupan postikulut. Jos kaupan postikulut
                 * on jo muistissa, säilytetään vain kalleimmat postikulut. */
                String kaupanNimi = ht.getKaupanNimi();
                Hinta postikulut = ht.getSuodatetutPostikulut();
                if (!kalleimmatPostikulut.containsKey(kaupanNimi) || postikulut.onEnemmanKuin(kalleimmatPostikulut.get(kaupanNimi))) {
                    kalleimmatPostikulut.put(kaupanNimi, postikulut);
                }
            }
        }

        /* Lisätään kaikkien kauppojen postikulut kokonaishintaan. */
        for (Hinta postikulut : kalleimmatPostikulut.values()) {
            kokonaisHinta = kokonaisHinta.plus(postikulut);
        }

        return kokonaisHinta;
    }

    public static int getKokonaisMaara() {
        int kokonaisMaara = 0;

        for (Ostos ostos : ostoskori.getOstokset()) {

            /* Huomioidaan vain ne ostokset, jotka on oikeasti mahdollista
             * ostaa. */
            if (ostos.getValittuHintatieto() != null) {

                /* Lisätään määrä kokonaismäärään. */
                kokonaisMaara += ostos.getMaara();
            }
        }

        return kokonaisMaara;
    }

    public static EventList<Ostos> getEventList() {
        return PLUGGABLE;
    }

    public static Path getOstoskoritiedosto() {
        return ostoskoritiedosto;
    }

    public static boolean onkoTiedostoAjanTasalla() {
        if (ostoskoritiedosto != null) {
            try {
                return XML.documentEquals(ostoskori.kirjoitaXML(), XML.avaa(ostoskoritiedosto));
            } catch (IOException | JDOMException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

    private static void setOstoskori(Ostoskori ostoskori) {
        Ostoskori oldOstoskori = OstoskoriRajapinta.ostoskori;
        OstoskoriRajapinta.ostoskori = ostoskori;
        CS.fireRootChange("ostoskori", oldOstoskori, ostoskori);
    }

    private static void setOstoskoritiedosto(Path ostoskoritiedosto) {
        Path oldOstoskoritiedosto = OstoskoriRajapinta.ostoskoritiedosto;
        OstoskoriRajapinta.ostoskoritiedosto = ostoskoritiedosto;
        CS.fireRootChange("ostoskoritiedosto", oldOstoskoritiedosto, ostoskoritiedosto);
    }

    public static void tallenna() throws IOException {
        XML.tallenna(ostoskori.kirjoitaXML(), ostoskoritiedosto);
    }

    public static void uusi() {
        setOstoskoritiedosto(null);
        setOstoskori(new Ostoskori());
    }

    public static RaakaOstoskori muodostaRaakaOstoskori(Path tiedosto) throws IOException, JDOMException, IllegalArgumentException {
        return RaakaOstoskori.lueXML(XML.avaa(tiedosto));
    }

    public static void avaa(RaakaOstoskori raakaOstoskori, Path tiedosto) {
        setOstoskoritiedosto(tiedosto);
        setOstoskori(Ostoskori.lueRaakaOstoskori(raakaOstoskori));
    }

    public static void tallennaNimella(Path tiedosto) throws IOException {
        XML.tallenna(ostoskori.kirjoitaXML(), tiedosto);
        setOstoskoritiedosto(tiedosto);
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
