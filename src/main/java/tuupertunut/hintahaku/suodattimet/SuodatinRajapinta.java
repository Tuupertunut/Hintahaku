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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.PluggableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.JOptionPane;
import org.jdom2.JDOMException;
import tuupertunut.hintahaku.asetukset.AsetusRajapinta;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.XML;

/**
 *
 * @author Tuupertunut
 */
public class SuodatinRajapinta {

    private static final ChangeSupport CS = new ChangeSupport(SuodatinRajapinta.class);

    private static final PluggableList<Kauppa> PLUGGABLE;
    private static final ChangeListener PROXY_LISTENER;

    private static Suodattimet suodattimet;

    static {
        /* Avataan suodattimet. */
        suodattimet = new Suodattimet();

        Path tiedosto = AsetusRajapinta.getSuodatintiedosto();

        /* Ohitetaan tiedoston avaus, jos oletustiedosto on käytössä, eikä sitä
         * ole olemassa. Silloin on todennäköisesti kyse ensimmäisestä
         * avauskerrasta, eikä käyttäjälle haluta ensimmäisenä näyttää
         * virheilmoitusta. */
        if (!tiedosto.equals(AsetusRajapinta.OLETUS_SUODATINTIEDOSTO) || Files.isRegularFile(tiedosto)) {

            try {
                suodattimet = Suodattimet.lueXML(XML.avaa(tiedosto));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Nykyisen suodatintiedoston avaaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            } catch (JDOMException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Nykyinen suodatintiedosto on virheellinen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        }

        /* Tallennetaan tiedosto aina, kun suodattimet muuttuvat. */
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("suodattimet")) {

                try {
                    tallenna();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Nykyisen suodatintiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        /* Kun suodattimet-olio vaihtuu, poistetaan Listener vanhoista
         * suodattimista ja lisätään se uusiin. */
        PROXY_LISTENER = (ChangeEvent evt) -> CS.fireProxyChange(evt, "suodattimet");
        suodattimet.addListener(PROXY_LISTENER);
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("suodattimet") && evt.isRootEvent()) {

                Suodattimet oldSuodattimet = (Suodattimet) evt.getOldValue();
                oldSuodattimet.removeListener(PROXY_LISTENER);

                suodattimet.addListener(PROXY_LISTENER);
            }
        });

        /* Muodostetaan EventList, jonka sisältö vastaa aina senhetkisten
         * suodattimien EventListiä. */
        PLUGGABLE = new PluggableList<>(SuodattimetEventListUtils.PUBLISHER, SuodattimetEventListUtils.LOCK);
        PLUGGABLE.setSource(suodattimet.getKaupat().getEventList());
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("suodattimet") && evt.isRootEvent()) {

                PLUGGABLE.setSource(suodattimet.getKaupat().getEventList());
            }
        });
    }

    public static Kauppa haeKauppa(String nimi) {
        Map<String, Kauppa> kaupat = suodattimet.getKaupat();

        /* Jos kauppaa ei vielä ole Mapissa, laitetaan se sinne. */
        if (!kaupat.containsKey(nimi)) {
            kaupat.put(nimi, new Kauppa(nimi, false, false));
        }
        return kaupat.get(nimi);
    }

    public static EventList<Kauppa> getEventList() {
        return PLUGGABLE;
    }

    private static void setSuodattimet(Suodattimet suodattimet) {
        Suodattimet oldSuodattimet = SuodatinRajapinta.suodattimet;
        SuodatinRajapinta.suodattimet = suodattimet;
        CS.fireRootChange("suodattimet", oldSuodattimet, suodattimet);
    }

    private static void tallenna() throws IOException {
        XML.tallenna(suodattimet.kirjoitaXML(), AsetusRajapinta.getSuodatintiedosto());
    }

    public static void uusi(Path tiedosto) throws IOException {
        Suodattimet uudetSuodattimet = new Suodattimet();

        /* Vanhoissa suodattimissa mukana olleet kaupat säilytetään. Niiden
         * suodattimet kuitenkin nollataan oletusarvoille. */
        for (String kaupanNimi : suodattimet.getKaupat().keySet()) {
            uudetSuodattimet.getKaupat().put(kaupanNimi, new Kauppa(kaupanNimi, false, false));
        }

        /* Uusi tiedosto tulisi kyllä muutenkin tallennettua, mutta kun se
         * tallennetaan manuaalisesti tässä, mahdollinen virhe voidaan heittää
         * metodin kutsujalle. */
        XML.tallenna(uudetSuodattimet.kirjoitaXML(), tiedosto);

        AsetusRajapinta.setSuodatintiedosto(tiedosto);
        setSuodattimet(uudetSuodattimet);
    }

    public static void vaihda(Path tiedosto) throws IOException, JDOMException, IllegalArgumentException {
        Suodattimet uudetSuodattimet = Suodattimet.lueXML(XML.avaa(tiedosto));

        /* Vanhoissa suodattimissa mukana olleet kaupat, joita ei löytynyt
         * uudesta tiedostosta, säilytetään. Niiden suodattimet kuitenkin
         * nollataan oletusarvoille. */
        for (String kaupanNimi : suodattimet.getKaupat().keySet()) {
            uudetSuodattimet.getKaupat().putIfAbsent(kaupanNimi, new Kauppa(kaupanNimi, false, false));
        }

        AsetusRajapinta.setSuodatintiedosto(tiedosto);
        setSuodattimet(uudetSuodattimet);
    }

    public static void monista(Path tiedosto) throws IOException {
        XML.tallenna(suodattimet.kirjoitaXML(), tiedosto);
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
