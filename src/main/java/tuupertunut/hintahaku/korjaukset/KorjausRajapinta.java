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

import ca.odell.glazedlists.EventList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import org.jdom2.JDOMException;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.XML;

/**
 *
 * @author Tuupertunut
 */
public class KorjausRajapinta {

    private static final ChangeSupport CS = new ChangeSupport(KorjausRajapinta.class);

    private static final Path KORJAUSTIEDOSTO = Paths.get("korjaukset.xml");

    private static Korjaukset korjaukset;

    static {
        /* Avataan korjaukset. */
        korjaukset = new Korjaukset();

        /* Ohitetaan tiedoston avaus, jos tiedostoa ei ole olemassa. Silloin on
         * todennäköisesti kyse ensimmäisestä avauskerrasta, eikä käyttäjälle
         * haluta ensimmäisenä näyttää virheilmoitusta. */
        if (Files.isRegularFile(KORJAUSTIEDOSTO)) {

            try {
                korjaukset = Korjaukset.lueXML(XML.avaa(KORJAUSTIEDOSTO));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Korjaustiedoston avaaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            } catch (JDOMException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(null, "Korjaustiedosto on virheellinen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        }

        /* Tallennetaan tiedosto aina, kun korjaukset muuttuvat. */
        addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("korjaukset")) {

                try {
                    tallenna();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Korjaustiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        korjaukset.addListener((ChangeEvent evt) -> CS.fireProxyChange(evt, "korjaukset"));
    }

    public static Korjaus haeKorjaus(Tuote tuote, String kaupanNimi, KorjauksenKohde kohde) {
        return korjaukset.getKorjaukset().get(tuote.getUrl(), kaupanNimi, kohde);
    }

    public static boolean onkoKorjausta(Tuote tuote, String kaupanNimi, KorjauksenKohde kohde) {
        return korjaukset.getKorjaukset().containsKeys(tuote.getUrl(), kaupanNimi, kohde);
    }

    public static void lisaaKorjaus(Korjaus korjaus) {
        korjaukset.getKorjaukset().put(korjaus.getTuoteUrl(), korjaus.getKaupanNimi(), korjaus.getKohde(), korjaus);
    }

    public static void poistaKorjaus(Korjaus korjaus) {
        korjaukset.getKorjaukset().remove(korjaus.getTuoteUrl(), korjaus.getKaupanNimi(), korjaus.getKohde());
    }

    public static EventList<Korjaus> getEventList() {
        return korjaukset.getKorjaukset().getEventList();
    }

    private static void tallenna() throws IOException {
        XML.tallenna(korjaukset.kirjoitaXML(), KORJAUSTIEDOSTO);
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
