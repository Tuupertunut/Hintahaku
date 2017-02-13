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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import tuupertunut.hintahaku.core.HintaFiUrl;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeListenableConnector;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Ostoskori {

    private final ChangeListenableConnector connector = new ChangeListenableConnector();

    private final EventList<Ostos> ostokset;

    public Ostoskori() {
        this.ostokset = new ObservableElementList<>(new BasicEventList<>(OstoskoriEventListUtils.PUBLISHER, OstoskoriEventListUtils.LOCK), connector);

        this.ostokset.addListEventListener((ListEvent<Ostos> listChanges) -> cs.fireProxyChange(connector.wrapListEvent(listChanges), "ostokset"));
    }

    public EventList<Ostos> getOstokset() {
        return ostokset;
    }

    public void lisaaOstos(Ostos ostos) {
        if (ostos.getOstoskori().equals(this)) {
            ostokset.add(ostos);
        }
    }

    public void poistaOstos(Ostos ostos) {
        ostokset.remove(ostos);
    }

    public Map<Ostos, Hintatieto> getValitutHintatiedot() {
        return JoukkotilausAlgoritmi.haeParhaatHinnat(this);
    }

    public Document kirjoitaXML() {
        Element juuri = new Element("ostoskori");
        juuri.setAttribute("info", "Tallennettu Hintahaku-ostoskori").setAttribute("versio", "2");

        for (Ostos ostos : ostokset) {
            Element ktElem = new Element("ostos");

            Element vaihtoehdotElem = new Element("vaihtoehdot");
            for (Tuote vaihtoehto : ostos.getVaihtoehdot()) {
                vaihtoehdotElem.addContent(new Element("url").setText(vaihtoehto.getUrl().toString()));
            }

            ktElem.addContent(vaihtoehdotElem);

            ktElem.addContent(new Element("määrä").setText(Integer.toString(ostos.getMaara())));

            juuri.addContent(ktElem);
        }

        return new Document(juuri);
    }

    /* Luetaan XML-dokumentin sijaan RaakaOstoskori, koska siinä on valmiiksi
     * netistä haetut tuotteet. Tuotteiden hakeminen on siis ulkoistettu
     * RaakaOstoskorille. */
    public static Ostoskori lueRaakaOstoskori(RaakaOstoskori raakaOstoskori) {
        Map<HintaFiUrl, Tuote> haetutTuotteet = raakaOstoskori.getHaetutTuotteet();

        Ostoskori ostoskori = new Ostoskori();

        for (RaakaOstos raakaOstos : raakaOstoskori.getOstokset()) {

            /* Etsitään valmiiksi haetut tuotteet Mapista URL:n perusteella. */
            List<Tuote> haetutVaihtoehdot = new ArrayList<>();
            for (HintaFiUrl url : raakaOstos.getVaihtoehdot()) {
                haetutVaihtoehdot.add(haetutTuotteet.get(url));
            }

            ostoskori.lisaaOstos(new Ostos(ostoskori, haetutVaihtoehdot, raakaOstos.getMaara()));
        }

        return ostoskori;
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
