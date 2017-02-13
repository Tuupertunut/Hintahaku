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
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import tuupertunut.hintahaku.gui.renderer.HintaSpinnerEditor;
import tuupertunut.hintahaku.gui.renderer.HintaSpinnerRenderer;
import tuupertunut.hintahaku.gui.util.LocationPopupMenu;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.korjaukset.Korjaus;
import tuupertunut.hintahaku.korjaukset.KorjausRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class KorjauksetTable extends JTable {

    public KorjauksetTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        /* 24 on hyvä korkeus, jotta JSpinner mahtuu kokonaan taulukon riville */
        setRowHeight(24);

        SortedList<Korjaus> jarjestetty = new SortedList<>(KorjausRajapinta.getEventList(), (Korjaus o1, Korjaus o2) -> 0);
        jarjestetty.setMode(SortedList.AVOID_MOVING_ELEMENTS);

        setModel(GlazedListsSwing.eventTableModel(jarjestetty, new KorjauksetTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(jarjestetty));

        TableComparatorChooser.install(this, jarjestetty, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        new TableColumnAdjuster(this).setDynamicAdjustment(true);

        getColumnModel().getColumn(3).setCellRenderer(new HintaSpinnerRenderer());
        getColumnModel().getColumn(3).setCellEditor(new HintaSpinnerEditor());

        /* Luodaan oikeaklikkausvalikko. */
        LocationPopupMenu oikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem avaaSelaimessaValinta = new JMenuItem("Avaa tuote selaimessa");
        avaaSelaimessaValinta.addActionListener((ActionEvent e) -> {

            avaaSelaimessa(korjausKohdassa(oikeaKlikkausValikko.getTriggerLocation()));
        });
        oikeaKlikkausValikko.add(avaaSelaimessaValinta);

        JMenuItem poistaValinta = new JMenuItem("Poista");
        poistaValinta.addActionListener((ActionEvent e) -> {

            KorjausRajapinta.poistaKorjaus(korjausKohdassa(oikeaKlikkausValikko.getTriggerLocation()));
        });
        oikeaKlikkausValikko.add(poistaValinta);

        /* Hiirikäsittelijä oikeaklikkausvalikolle */
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                mousePressedOrReleased(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePressedOrReleased(e);
            }

            private void mousePressedOrReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent().contains(e.getPoint())) {
                    oikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private Korjaus korjausKohdassa(Point kohta) {
        return ((AdvancedTableModel<Korjaus>) getModel()).getElementAt(rowAtPoint(kohta));
    }

    private void avaaSelaimessa(Korjaus korjaus) {
        try {
            Desktop.getDesktop().browse(URI.create(korjaus.getTuoteUrl().toString()));
        } catch (IOException | UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(this, "Tuotteen avaaminen selaimessa epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
