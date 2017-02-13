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
import ca.odell.glazedlists.gui.WritableTableFormat;
import java.util.Comparator;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.korjaukset.KorjauksenKohde;
import tuupertunut.hintahaku.korjaukset.Korjaus;

/**
 *
 * @author Tuupertunut
 */
public class KorjauksetTableFormat implements AdvancedTableFormat<Korjaus>, WritableTableFormat<Korjaus> {

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
            case 1:
                return String.class;
            case 2:
                return KorjauksenKohde.class;
            case 3:
                return Hinta.class;
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
                return "Tuote";
            case 1:
                return "Kauppa";
            case 2:
                return "Kohde";
            case 3:
                return "Korjattu hinta";
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object getColumnValue(Korjaus baseObject, int column) {
        switch (column) {
            case 0:
                return baseObject.getTuoteNimi();
            case 1:
                return baseObject.getKaupanNimi();
            case 2:
                return baseObject.getKohde();
            case 3:
                return baseObject.getKorjattuHinta();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean isEditable(Korjaus baseObject, int column) {
        switch (column) {
            case 0:
            case 1:
            case 2:
                return false;
            case 3:
                return true;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Korjaus setColumnValue(Korjaus baseObject, Object editedValue, int column) {
        switch (column) {
            case 3:
                baseObject.setKorjattuHinta((Hinta) editedValue);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        return null;
    }
}
