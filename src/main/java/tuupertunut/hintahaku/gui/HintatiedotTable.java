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
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import java.awt.Color;
import java.awt.Point;
import javax.swing.JTable;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.gui.renderer.VaritettyHintatietoBooleanRenderer;
import tuupertunut.hintahaku.gui.renderer.VaritettyHintatietoDefaultTableCellRenderer;
import tuupertunut.hintahaku.gui.renderer.VaritettyHintatietoPostikuluRenderer;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.hintatiedot.HintatiedotRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class HintatiedotTable extends JTable {

    public HintatiedotTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        SortedList<Hintatieto> jarjestetty = new SortedList<>(HintatiedotRajapinta.getEventList(), (Hintatieto o1, Hintatieto o2) -> 0);

        setModel(GlazedListsSwing.eventTableModel(jarjestetty, new HintatiedotTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(jarjestetty));

        TableComparatorChooser.install(this, jarjestetty, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        new TableColumnAdjuster(this).setDynamicAdjustment(true);

        getColumnModel().getColumn(0).setCellRenderer(new VaritettyHintatietoDefaultTableCellRenderer());
        getColumnModel().getColumn(1).setCellRenderer(new VaritettyHintatietoDefaultTableCellRenderer());
        getColumnModel().getColumn(2).setCellRenderer(new VaritettyHintatietoDefaultTableCellRenderer());
        getColumnModel().getColumn(3).setCellRenderer(new VaritettyHintatietoPostikuluRenderer());
        getColumnModel().getColumn(4).setCellRenderer(new VaritettyHintatietoDefaultTableCellRenderer());
        getColumnModel().getColumn(5).setCellRenderer(new VaritettyHintatietoBooleanRenderer());
        getColumnModel().getColumn(6).setCellRenderer(new VaritettyHintatietoBooleanRenderer());

        /* Piirretään taulukko uudestaan aina, kun valittu hintatieto muuttuu
         * rajapinnassa. Uuden valitun hintatiedon erikoisväri ei piirry
         * automaattisesti, koska tieto siitä ei sijaitse taulukon
         * TableModelissa, vaan pelkästään käyttöliittymän
         * TableCellRenderereissä. */
        HintatiedotRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("valittuHintatieto") && evt.isRootEvent()) {
                repaint();
            }
        });
    }

    public Hintatieto hintatietoKohdassa(Point kohta) {
        return ((AdvancedTableModel<Hintatieto>) getModel()).getElementAt(rowAtPoint(kohta));
    }

    /* Tarkoitettu kutsuttavaksi TableCellRendereristä, jotta renderer pystyy
     * värjäämään taustan oikealla värillä. */
    public Color getRivinErikoisvari(int rivi) {
        Hintatieto rivinHintatieto = ((AdvancedTableModel<Hintatieto>) getModel()).getElementAt(rivi);

        if (HintatiedotRajapinta.onkoHalvinHintatieto(rivinHintatieto)) {
            return new Color(141, 213, 144); // vihreä
        } else if (HintatiedotRajapinta.onkoValittuHintatieto(rivinHintatieto)) {
            return new Color(224, 154, 95); // ruskea
        } else {
            return null;
        }
    }
}
