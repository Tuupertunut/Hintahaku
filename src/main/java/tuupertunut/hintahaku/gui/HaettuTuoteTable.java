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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
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
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.gui.renderer.HuutomerkkiRenderer;
import tuupertunut.hintahaku.gui.util.LocationPopupMenu;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.haku.HaettuTuoteRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class HaettuTuoteTable extends JTable {

    public HaettuTuoteTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        EventList<Tuote> lista = HaettuTuoteRajapinta.getEventList();

        setModel(GlazedListsSwing.eventTableModel(lista, new HaettuTuoteTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(lista));

        new TableColumnAdjuster(this).setDynamicAdjustment(true);

        getColumnModel().getColumn(1).setCellRenderer(new HuutomerkkiRenderer());

        /* Luodaan oikeaklikkausvalikko. */
        LocationPopupMenu oikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem avaaSelaimessaValinta = new JMenuItem("Avaa selaimessa");
        avaaSelaimessaValinta.addActionListener((ActionEvent e) -> {

            avaaSelaimessa(tuoteKohdassa(oikeaKlikkausValikko.getTriggerLocation()));
        });
        oikeaKlikkausValikko.add(avaaSelaimessaValinta);

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

    private Tuote tuoteKohdassa(Point kohta) {
        return ((AdvancedTableModel<Tuote>) getModel()).getElementAt(rowAtPoint(kohta));
    }

    private void avaaSelaimessa(Tuote tuote) {
        try {
            Desktop.getDesktop().browse(URI.create(tuote.getUrl().toString()));
        } catch (IOException | UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(this, "Tuotteen avaaminen selaimessa epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
