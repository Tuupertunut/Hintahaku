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
package tuupertunut.hintahaku.gui;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import java.util.Comparator;
import java.util.List;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.ostoskori.Ostos;

/**
 *
 * @author Tuupertunut
 */
public class KaupoittainTableFormat implements AdvancedTableFormat<List<Ostos>> {

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
            case 2:
                return Hinta.class;
            case 3:
                return Integer.class;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Comparator getColumnComparator(int column) {
        return Comparator.nullsLast(Comparator.naturalOrder());
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Kauppa";
            case 1:
                return "Hinta";
            case 2:
                return "Postikulut";
            case 3:
                return "Määrä";
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object getColumnValue(List<Ostos> baseObject, int column) {

        /* baseObject-listan jokaisella ostoksella on varmasti olemassa valittu
         * hintatieto. */
        switch (column) {
            case 0:

                /* Kaikilla listan ostoksilla on sama valitun hintatiedon
                 * kauppa, joten haetaan se listan ensimmäisestä elementistä. */
                return baseObject.get(0).getValittuHintatieto().get().getKaupanNimi();

            case 1:
                Hinta kokonaisHinta = new Hinta(0);

                for (Ostos ostos : baseObject) {
                    Hintatieto ht = ostos.getValittuHintatieto().get();

                    /* Lisätään hinta kerrottuna tuotteiden määrällä
                     * kokonaishintaan. */
                    kokonaisHinta = kokonaisHinta.plus(ht.getHinta().kertaa(ostos.getMaara()));
                }

                return kokonaisHinta;

            case 2:
                Hinta kalleimmatPostikulut = new Hinta(0);

                for (Ostos ostos : baseObject) {
                    Hintatieto ht = ostos.getValittuHintatieto().get();

                    /* Tässä vaiheessa tiedetään, että Optional ei voi olla
                     * tyhjä. */
                    Hinta postikulut = ht.getSuodatetutPostikulut().get();

                    /* Säilytetään vain kalleimmat postikulut. */
                    if (postikulut.onEnemmanKuin(kalleimmatPostikulut)) {
                        kalleimmatPostikulut = postikulut;
                    }
                }

                return kalleimmatPostikulut;

            case 3:
                int kokonaisMaara = 0;

                for (Ostos ostos : baseObject) {

                    /* Lisätään määrä kokonaismäärään. */
                    kokonaisMaara += ostos.getMaara();
                }

                return kokonaisMaara;

            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
