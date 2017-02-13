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

import ca.odell.glazedlists.event.ListEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import tuupertunut.hintahaku.event.ChangeListenableConnector;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;
import tuupertunut.hintahaku.util.ObservableElementEventHashMap;

/**
 *
 * @author Tuupertunut
 */
public class Suodattimet {

    private final ChangeListenableConnector connector = new ChangeListenableConnector();

    private final ObservableElementEventHashMap<String, Kauppa> kaupat;

    public Suodattimet() {
        this(null);
    }

    public Suodattimet(Collection<Kauppa> kaupat) {
        this.kaupat = new ObservableElementEventHashMap<>(kaupat, Kauppa::getKaupanNimi, connector, SuodattimetEventListUtils.PUBLISHER, SuodattimetEventListUtils.LOCK);

        this.kaupat.addListEventListener((ListEvent<Kauppa> listChanges) -> cs.fireProxyChange(connector.wrapListEvent(listChanges), "kaupat"));
    }

    public ObservableElementEventHashMap<String, Kauppa> getKaupat() {
        return kaupat;
    }

    public Document kirjoitaXML() {
        Element juuri = new Element("suodattimet");
        juuri.setAttribute("info", "Hintahaku-suodattimet");

        for (Kauppa kauppa : kaupat.values()) {
            Element kauppaElem = new Element("kauppa");
            kauppaElem.addContent(new Element("nimi").setText(kauppa.getKaupanNimi()));
            kauppaElem.addContent(new Element("voiNoutaa").setText(Boolean.toString(kauppa.isVoiNoutaa())));
            kauppaElem.addContent(new Element("suodataPois").setText(Boolean.toString(kauppa.isSuodataPois())));

            juuri.addContent(kauppaElem);
        }

        return new Document(juuri);
    }

    public static Suodattimet lueXML(Document dok) throws IllegalArgumentException {
        Element juuri = dok.getRootElement();

        if (!"Hintahaku-suodattimet".equals(juuri.getAttributeValue("info"))) {
            throw new IllegalArgumentException("Ei ole suodatintiedosto");
        }

        List<Kauppa> kaupat = new ArrayList<>();

        try {
            for (Element kauppaElem : juuri.getChildren()) {
                String kaupanNimi = kauppaElem.getChildText("nimi");
                boolean voiNoutaa = Boolean.parseBoolean(kauppaElem.getChildText("voiNoutaa"));
                boolean suodataPois = Boolean.parseBoolean(kauppaElem.getChildText("suodataPois"));

                kaupat.add(new Kauppa(kaupanNimi, voiNoutaa, suodataPois));
            }

            return new Suodattimet(kaupat);
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
