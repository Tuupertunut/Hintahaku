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
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Hintatieto;

/**
 *
 * @author Tuupertunut
 */
public class HintatiedotTableFormat implements AdvancedTableFormat<Hintatieto> {

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
            case 2:
            case 3:
                return Hinta.class;
            case 4:
                return String.class;
            case 5:
            case 6:
                return Boolean.class;
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
        return 7;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Kauppa";
            case 1:
                return "Hinta noudolla";
            case 2:
                return "Toimituksella";
            case 3:
                return "Postikulut";
            case 4:
                return "Toimitusaika";
            case 5:
                return "Voi noutaa";
            case 6:
                return "Suodata pois";
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object getColumnValue(Hintatieto baseObject, int column) {
        switch (column) {
            case 0:
                return baseObject.getKaupanNimi();
            case 1:
                return baseObject.getHinta();
            case 2:
                return baseObject.getHintaKuluineen().orElse(null);
            case 3:
                return baseObject.getPostikulut().orElse(null);
            case 4:
                return baseObject.getToimitusaika();
            case 5:
                return baseObject.isVoiNoutaa();
            case 6:
                return baseObject.isSuodataPois();
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
