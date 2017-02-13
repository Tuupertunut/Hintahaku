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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import tuupertunut.hintahaku.core.HintaFiHakija;
import tuupertunut.hintahaku.core.HintaFiUrl;
import tuupertunut.hintahaku.core.Tuote;

/**
 *
 * @author Tuupertunut
 */
public class RaakaOstoskori {

    private final List<RaakaOstos> ostokset;

    private final Map<HintaFiUrl, Tuote> haetutTuotteet;

    public RaakaOstoskori(Collection<RaakaOstos> ostokset) {
        this.ostokset = new ArrayList<>(ostokset);

        haetutTuotteet = new HashMap<>();
    }

    public List<RaakaOstos> getOstokset() {
        return ostokset;
    }

    public Map<HintaFiUrl, Tuote> getHaetutTuotteet() {
        return haetutTuotteet;
    }

    public void haeTuotteet() throws IOException, InterruptedException {
        for (RaakaOstos raakaOstos : ostokset) {
            for (HintaFiUrl url : raakaOstos.getVaihtoehdot()) {
                if (!haetutTuotteet.containsKey(url)) {
                    haetutTuotteet.put(url, HintaFiHakija.haeTuote(url));
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
        }
    }

    public static RaakaOstoskori lueXML(Document dok) throws IllegalArgumentException {
        if ("2".equals(dok.getRootElement().getAttributeValue("versio"))) {
            return lueV2XML(dok);
        } else {
            return lueV1XML(dok);
        }
    }

    /* Taaksepäin yhteensopivuus vanhojen kokoonpanotiedostojen kanssa */
    private static RaakaOstoskori lueV1XML(Document dok) throws IllegalArgumentException {
        Element juuri = dok.getRootElement();

        if (!"Tallennettu Hintahaku-kokoonpano".equals(juuri.getAttributeValue("info"))) {
            throw new IllegalArgumentException("Ei ole ostoskoritiedosto");
        }

        List<RaakaOstos> ostokset = new ArrayList<>();

        try {
            for (Element tuoteElem : juuri.getChildren()) {

                HintaFiUrl url = HintaFiUrl.parse(tuoteElem.getChildText("url"));
                int maara = Integer.parseInt(tuoteElem.getChildText("määrä"));

                ostokset.add(new RaakaOstos(url, maara));
            }

            return new RaakaOstoskori(ostokset);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static RaakaOstoskori lueV2XML(Document dok) throws IllegalArgumentException {
        Element juuri = dok.getRootElement();

        if (!"Tallennettu Hintahaku-ostoskori".equals(juuri.getAttributeValue("info"))) {
            throw new IllegalArgumentException("Ei ole ostoskoritiedosto");
        }

        List<RaakaOstos> ostokset = new ArrayList<>();

        try {
            for (Element ostosElem : juuri.getChildren()) {

                List<HintaFiUrl> vaihtoehdot = new ArrayList<>();
                for (Element urlElem : ostosElem.getChild("vaihtoehdot").getChildren()) {
                    vaihtoehdot.add(HintaFiUrl.parse(urlElem.getText()));
                }

                int maara = Integer.parseInt(ostosElem.getChildText("määrä"));

                ostokset.add(new RaakaOstos(vaihtoehdot, maara));
            }

            return new RaakaOstoskori(ostokset);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
