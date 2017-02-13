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
package tuupertunut.hintahaku.gui.util;

import java.awt.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import tuupertunut.hintahaku.asetukset.AsetusRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class Tiedostovalitsimet {

    public static void tallennaOstoskoriValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> tallentaja) {
        tallennaValitsimella(isantaikkuna, oletuspolku, tyypinKuvaus, paate, tallentaja, AsetusRajapinta::getViimeisinOstoskoriKansio, AsetusRajapinta::setViimeisinOstoskoriKansio);
    }

    public static void tallennaSuodattimetValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> tallentaja) {
        tallennaValitsimella(isantaikkuna, oletuspolku, tyypinKuvaus, paate, tallentaja, AsetusRajapinta::getViimeisinSuodatinKansio, AsetusRajapinta::setViimeisinSuodatinKansio);
    }

    public static void avaaOstoskoriValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> avaaja) {
        avaaValitsimella(isantaikkuna, oletuspolku, tyypinKuvaus, paate, avaaja, AsetusRajapinta::getViimeisinOstoskoriKansio, AsetusRajapinta::setViimeisinOstoskoriKansio);
    }

    public static void avaaSuodattimetValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> avaaja) {
        avaaValitsimella(isantaikkuna, oletuspolku, tyypinKuvaus, paate, avaaja, AsetusRajapinta::getViimeisinSuodatinKansio, AsetusRajapinta::setViimeisinSuodatinKansio);
    }

    private static void tallennaValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> tallentaja, Supplier<Path> viimeisinGetter, Consumer<Path> viimeisinSetter) {

        /* Tiedostovalitsimen määritykset */
        JFileChooser valitsin = new JFileChooser();

        if (oletuspolku != null) {
            if (Files.isDirectory(oletuspolku)) {
                valitsin.setCurrentDirectory(oletuspolku.toFile());
            } else if (Files.isDirectory(oletuspolku.getParent())) {
                valitsin.setSelectedFile(oletuspolku.toFile());
            } else {
                Path viimeisin = viimeisinGetter.get();
                if (viimeisin != null) {
                    valitsin.setCurrentDirectory(viimeisin.toFile());
                }
            }
        } else {
            Path viimeisin = viimeisinGetter.get();
            if (viimeisin != null) {
                valitsin.setCurrentDirectory(viimeisin.toFile());
            }
        }

        if (paate != null && !paate.isEmpty()) {
            valitsin.setFileFilter(new FileNameExtensionFilter(tyypinKuvaus, paate));
        }

        /* Valitaan tiedosto valitsimella */
        int valinta = valitsin.showSaveDialog(isantaikkuna);
        if (valinta == JFileChooser.APPROVE_OPTION) {

            viimeisinSetter.accept(valitsin.getCurrentDirectory().toPath());
            Path tallennettava = valitsin.getSelectedFile().toPath();

            /* Lisätään tarvittaessa pääte tiedostonimen perään */
            if (paate != null && !paate.isEmpty()) {
                String tiedostonimi = tallennettava.getFileName().toString();
                if (!tiedostonimi.endsWith('.' + paate)) {
                    tallennettava = tallennettava.resolveSibling(tiedostonimi + '.' + paate);
                }
            }

            /* Jos tiedosto on jo olemassa, kysytään, halutaanko se korvata */
            if (Files.isRegularFile(tallennettava)) {
                Object[] napit = {"Kyllä", "Peruuta"};
                int korvaaValinta = JOptionPane.showOptionDialog(isantaikkuna, "Kyseinen tiedosto on jo olemassa, haluatko korvata sen?", "Varmistus", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
                if (korvaaValinta != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            /* Suoritetaan tallentajan koodi */
            tallentaja.accept(tallennettava);
        }
    }

    private static void avaaValitsimella(Component isantaikkuna, Path oletuspolku, String tyypinKuvaus, String paate, Consumer<Path> avaaja, Supplier<Path> viimeisinGetter, Consumer<Path> viimeisinSetter) {

        /* Tiedostovalitsimen määritykset */
        JFileChooser valitsin = new JFileChooser();

        if (oletuspolku != null) {
            if (Files.isDirectory(oletuspolku)) {
                valitsin.setCurrentDirectory(oletuspolku.toFile());
            } else if (Files.isDirectory(oletuspolku.getParent())) {
                valitsin.setSelectedFile(oletuspolku.toFile());
            } else {
                Path viimeisin = viimeisinGetter.get();
                if (viimeisin != null) {
                    valitsin.setCurrentDirectory(viimeisin.toFile());
                }
            }
        } else {
            Path viimeisin = viimeisinGetter.get();
            if (viimeisin != null) {
                valitsin.setCurrentDirectory(viimeisin.toFile());
            }
        }

        if (paate != null && !paate.isEmpty()) {
            valitsin.setFileFilter(new FileNameExtensionFilter(tyypinKuvaus, paate));
        }

        /* Valitaan tiedosto valitsimella */
        int valinta = valitsin.showOpenDialog(isantaikkuna);
        if (valinta == JFileChooser.APPROVE_OPTION) {

            viimeisinSetter.accept(valitsin.getCurrentDirectory().toPath());
            Path avattava = valitsin.getSelectedFile().toPath();

            /* Suoritetaan avaajan koodi */
            avaaja.accept(avattava);
        }
    }
}
