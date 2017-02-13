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

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import javax.swing.JTable;
import tuupertunut.hintahaku.gui.renderer.BooleanRenderer;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.suodattimet.Kauppa;
import tuupertunut.hintahaku.suodattimet.SuodatinRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class SuodattimetTable extends JTable {

    public SuodattimetTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        SortedList<Kauppa> jarjestetty = new SortedList<>(SuodatinRajapinta.getEventList(), (Kauppa o1, Kauppa o2) -> 0);
        jarjestetty.setMode(SortedList.AVOID_MOVING_ELEMENTS);

        setModel(GlazedListsSwing.eventTableModel(jarjestetty, new SuodattimetTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(jarjestetty));

        TableComparatorChooser.install(this, jarjestetty, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        new TableColumnAdjuster(this).setDynamicAdjustment(true);

        getColumnModel().getColumn(1).setCellRenderer(new BooleanRenderer());
        getColumnModel().getColumn(2).setCellRenderer(new BooleanRenderer());
    }
}
