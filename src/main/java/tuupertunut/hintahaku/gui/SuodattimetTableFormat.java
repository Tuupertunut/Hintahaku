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
import tuupertunut.hintahaku.suodattimet.Kauppa;

/**
 *
 * @author Tuupertunut
 */
public class SuodattimetTableFormat implements AdvancedTableFormat<Kauppa>, WritableTableFormat<Kauppa> {

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
            case 2:
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
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Kauppa";
            case 1:
                return "Voi noutaa";
            case 2:
                return "Suodata pois";
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Object getColumnValue(Kauppa baseObject, int column) {
        switch (column) {
            case 0:
                return baseObject.getKaupanNimi();
            case 1:
                return baseObject.isVoiNoutaa();
            case 2:
                return baseObject.isSuodataPois();
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public boolean isEditable(Kauppa baseObject, int column) {
        switch (column) {
            case 0:
                return false;
            case 1:
            case 2:
                return true;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Kauppa setColumnValue(Kauppa baseObject, Object editedValue, int column) {
        switch (column) {
            case 1:
                baseObject.setVoiNoutaa((boolean) editedValue);
                break;
            case 2:
                baseObject.setSuodataPois((boolean) editedValue);
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        return null;
    }
}
