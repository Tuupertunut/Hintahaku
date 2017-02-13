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
package tuupertunut.hintahaku.asetukset;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.jdom2.Document;
import org.jdom2.Element;
import tuupertunut.hintahaku.event.ChangeListener;
import tuupertunut.hintahaku.event.ChangeSupport;

/**
 *
 * @author Tuupertunut
 */
public class Asetukset {

    private Path suodatintiedosto;
    private Path viimeisinSuodatinKansio;
    private Path viimeisinOstoskoriKansio;

    public Asetukset(Path suodatintiedosto, Path viimeisinSuodatinKansio, Path viimeisinOstoskoriKansio) {
        this.suodatintiedosto = suodatintiedosto;
        this.viimeisinSuodatinKansio = viimeisinSuodatinKansio;
        this.viimeisinOstoskoriKansio = viimeisinOstoskoriKansio;
    }

    public Path getSuodatintiedosto() {
        return suodatintiedosto;
    }

    public void setSuodatintiedosto(Path suodatintiedosto) {
        Path oldSuodatintiedosto = this.suodatintiedosto;
        this.suodatintiedosto = suodatintiedosto;
        cs.fireRootChange("suodatintiedosto", oldSuodatintiedosto, suodatintiedosto);
    }

    public Path getViimeisinSuodatinKansio() {
        return viimeisinSuodatinKansio;
    }

    public void setViimeisinSuodatinKansio(Path viimeisinSuodatinKansio) {
        Path oldViimeisinSuodatinKansio = this.viimeisinSuodatinKansio;
        this.viimeisinSuodatinKansio = viimeisinSuodatinKansio;
        cs.fireRootChange("viimeisinSuodatinKansio", oldViimeisinSuodatinKansio, viimeisinSuodatinKansio);
    }

    public Path getViimeisinOstoskoriKansio() {
        return viimeisinOstoskoriKansio;
    }

    public void setViimeisinOstoskoriKansio(Path viimeisinOstoskoriKansio) {
        Path oldViimeisinOstoskoriKansio = this.viimeisinOstoskoriKansio;
        this.viimeisinOstoskoriKansio = viimeisinOstoskoriKansio;
        cs.fireRootChange("viimeisinOstoskoriKansio", oldViimeisinOstoskoriKansio, viimeisinOstoskoriKansio);
    }

    public Document kirjoitaXML() {
        Element juuri = new Element("asetukset");
        juuri.setAttribute("info", "Hintahaku-asetukset");

        juuri.addContent(new Element("suodatintiedosto").setText(suodatintiedosto.toString()));
        juuri.addContent(new Element("viimeisinSuodatinKansio").setText(viimeisinSuodatinKansio.toString()));
        juuri.addContent(new Element("viimeisinOstoskoriKansio").setText(viimeisinOstoskoriKansio.toString()));

        return new Document(juuri);
    }

    public static Asetukset lueXML(Document dok) throws IllegalArgumentException {
        Element juuri = dok.getRootElement();

        if (!"Hintahaku-asetukset".equals(juuri.getAttributeValue("info"))) {
            throw new IllegalArgumentException("Ei ole asetustiedosto");
        }

        try {
            Path suodatintiedosto = Paths.get(juuri.getChildText("suodatintiedosto"));
            Path viimeisinSuodatinKansio = Paths.get(juuri.getChildText("viimeisinSuodatinKansio"));
            Path viimeisinOstoskoriKansio = Paths.get(juuri.getChildText("viimeisinOstoskoriKansio"));

            return new Asetukset(suodatintiedosto, viimeisinSuodatinKansio, viimeisinOstoskoriKansio);
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
