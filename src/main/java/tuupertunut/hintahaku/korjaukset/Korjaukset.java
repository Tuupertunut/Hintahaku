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

import ca.odell.glazedlists.event.ListEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.HintaFiUrl;
import tuupertunut.hintahaku.event.ChangeListenableConnector;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.ObservableElementEventThreeKeyHashMap;

/**
 *
 * @author Tuupertunut
 */
public class Korjaukset {

    private final ChangeListenableConnector connector = new ChangeListenableConnector();

    private final ObservableElementEventThreeKeyHashMap<HintaFiUrl, String, KorjauksenKohde, Korjaus> korjaukset;

    public Korjaukset() {
        this(null);
    }

    public Korjaukset(Collection<Korjaus> korjaukset) {
        this.korjaukset = new ObservableElementEventThreeKeyHashMap<>(korjaukset, Korjaus::getTuoteUrl, Korjaus::getKaupanNimi, Korjaus::getKohde, connector);

        this.korjaukset.addListEventListener((ListEvent<Korjaus> listChanges) -> cs.fireProxyChange(connector.wrapListEvent(listChanges), "korjaukset"));
    }

    public ObservableElementEventThreeKeyHashMap<HintaFiUrl, String, KorjauksenKohde, Korjaus> getKorjaukset() {
        return korjaukset;
    }

    public Document kirjoitaXML() {
        Element juuri = new Element("korjaukset");
        juuri.setAttribute("info", "Hintahaku-korjaukset");

        for (Korjaus korjaus : korjaukset.values()) {
            Element korjausElem = new Element("korjaus");
            korjausElem.addContent(new Element("tuoteUrl").setText(korjaus.getTuoteUrl().toString()));
            korjausElem.addContent(new Element("tuoteNimi").setText(korjaus.getTuoteNimi()));
            korjausElem.addContent(new Element("kaupanNimi").setText(korjaus.getKaupanNimi()));
            korjausElem.addContent(new Element("kohde").setText(korjaus.getKohde().toString()));
            korjausElem.addContent(new Element("korjattuHinta").setText(korjaus.getKorjattuHinta().toString()));

            juuri.addContent(korjausElem);
        }

        return new Document(juuri);
    }

    public static Korjaukset lueXML(Document dok) throws IllegalArgumentException {
        Element juuri = dok.getRootElement();

        if (!"Hintahaku-korjaukset".equals(juuri.getAttributeValue("info"))) {
            throw new IllegalArgumentException("Ei ole korjaustiedosto");
        }

        List<Korjaus> korjaukset = new ArrayList<>();

        try {
            for (Element korjausElem : juuri.getChildren()) {
                HintaFiUrl tuoteUrl = HintaFiUrl.parse(korjausElem.getChildText("tuoteUrl"));
                String tuoteNimi = korjausElem.getChildText("tuoteNimi");
                String kaupanNimi = korjausElem.getChildText("kaupanNimi");
                KorjauksenKohde kohde = KorjauksenKohde.parse(korjausElem.getChildText("kohde"));
                Hinta korjattuHinta = Hinta.parse(korjausElem.getChildText("korjattuHinta"));

                korjaukset.add(new Korjaus(tuoteUrl, tuoteNimi, kaupanNimi, kohde, korjattuHinta));
            }

            return new Korjaukset(korjaukset);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private final ChangeSupport cs = new ChangeSupport(this);

    public void addListener(ChangeListener listener) {
        cs.addListener(listener);
    }

    public void addListener(int priority, ChangeListener listener) {
        cs.addListener(priority, listener);
    }

    public void removeListener(ChangeListener listener) {
        cs.removeListener(listener);
    }
}
