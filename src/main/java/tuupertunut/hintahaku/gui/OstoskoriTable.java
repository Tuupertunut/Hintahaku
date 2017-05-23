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

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.DefaultExternalExpansionModel;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TreeTableSupport;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.gui.renderer.ColorFixDefaultTableCellRenderer;
import tuupertunut.hintahaku.gui.renderer.HuutomerkkiRenderer;
import tuupertunut.hintahaku.gui.renderer.SpinnerEditor;
import tuupertunut.hintahaku.gui.renderer.SpinnerRenderer;
import tuupertunut.hintahaku.gui.util.LocationPopupMenu;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.ostoskori.Ostos;
import tuupertunut.hintahaku.ostoskori.OstosRivi;
import tuupertunut.hintahaku.ostoskori.OstoskoriRajapinta;
import tuupertunut.hintahaku.ostoskori.OstoskoriRivi;
import tuupertunut.hintahaku.ostoskori.VaihtoehtoRivi;

/**
 *
 * @author Tuupertunut
 */
public class OstoskoriTable extends JTable {

    public OstoskoriTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        /* 24 on hyvä korkeus, jotta JSpinner mahtuu kokonaan taulukon riville. */
        setRowHeight(24);

        CollectionList<Ostos, OstoskoriRivi> rivit = new CollectionList<>(OstoskoriRajapinta.getEventList(), new CollectionList.Model<Ostos, OstoskoriRivi>() {

            @Override
            public List<OstoskoriRivi> getChildren(Ostos parent) {
                List<OstoskoriRivi> rivit = new ArrayList<>();

                OstosRivi isantaRivi = new OstosRivi(parent);
                rivit.add(isantaRivi);

                List<Tuote> vaihtoehdot = parent.getVaihtoehdot();
                if (vaihtoehdot.size() > 1) {
                    for (Tuote vaihtoehto : vaihtoehdot) {
                        rivit.add(new VaihtoehtoRivi(vaihtoehto, isantaRivi));
                    }
                }

                return rivit;
            }
        });

        SortedList<OstoskoriRivi> jarjestetty = new SortedList<>(rivit, (OstoskoriRivi o1, OstoskoriRivi o2) -> 0);
        jarjestetty.setMode(SortedList.AVOID_MOVING_ELEMENTS);

        TreeList<OstoskoriRivi> rivitPuu = new TreeList<>(jarjestetty, new TreeList.Format<OstoskoriRivi>() {

            @Override
            public void getPath(List<OstoskoriRivi> path, OstoskoriRivi element) {
                path.addAll(element.getPath());
            }

            @Override
            public boolean allowsChildren(OstoskoriRivi element) {
                return element instanceof OstosRivi;
            }

            @Override
            public Comparator<? super OstoskoriRivi> getComparator(int depth) {
                return null;
            }
        }, new DefaultExternalExpansionModel<OstoskoriRivi>());

        setModel(GlazedListsSwing.eventTableModel(rivitPuu, new OstoskoriTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(jarjestetty));

        TableComparatorChooser<OstoskoriRivi> comparatorChooser = TableComparatorChooser.install(this, jarjestetty, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        /* Kääritään kaikki comparatorit TreeTablelle sopivaan muotoon, joka
         * vertaa vain ostosrivejä. */
        for (int i = 0; i < 5; i++) {
            List<Comparator> comps = comparatorChooser.getComparatorsForColumn(i);
            Comparator<OstoskoriRivi> alkuperainenComp = comps.get(0);

            Comparator<OstoskoriRivi> kaarittyComp = Comparator.comparing((OstoskoriRivi ostoskoriRivi) -> ostoskoriRivi.getPath().get(0), alkuperainenComp);

            comps.set(0, kaarittyComp);
        }

        new TableColumnAdjuster(this).setDynamicAdjustment(true);

        getColumnModel().getColumn(0).setCellRenderer(new ColorFixDefaultTableCellRenderer());
        getColumnModel().getColumn(1).setCellRenderer(new HuutomerkkiRenderer());
        getColumnModel().getColumn(4).setCellRenderer(new SpinnerRenderer());
        getColumnModel().getColumn(4).setCellEditor(new SpinnerEditor());

        TreeTableSupport.install(this, rivitPuu, 0);

        /* Tuotteen tuplaklikkaaminen avaa sen selaimessa. */
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Point kohta = e.getPoint();

                /* Onko tuplaklikattu riviä? Määrä-sarakkeen JSpinnereiden
                 * tuplaklikkaamista ei lasketa. */
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2 && (columnAtPoint(kohta) != 4 || getModel().getValueAt(rowAtPoint(kohta), 4) == null)) {

                    Optional<Tuote> klikatunRivinTuote = tuoteKohdassa(kohta);
                    if (klikatunRivinTuote.isPresent()) {
                        avaaSelaimessa(klikatunRivinTuote.get());
                    } else {
                        JOptionPane.showMessageDialog(OstoskoriTable.this, "Kyseisellä rivillä ei ole tuotetta.", "Ilmoitus", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        /* Luodaan kaksi oikeaklikkausvalikkoa. Toinen riveille, joilla on
         * tuote. Toinen riveille, joilla ei. */
        LocationPopupMenu tuoteRivinOikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem avaaSelaimessaValinta = new JMenuItem("Avaa selaimessa");
        avaaSelaimessaValinta.addActionListener((ActionEvent e) -> {

            avaaSelaimessa(tuoteKohdassa(tuoteRivinOikeaKlikkausValikko.getTriggerLocation()).get());
        });
        tuoteRivinOikeaKlikkausValikko.add(avaaSelaimessaValinta);

        JMenuItem poistaValinta1 = new JMenuItem("Poista");
        poistaValinta1.addActionListener((ActionEvent e) -> {

            OstoskoriRajapinta.poistaRivi(riviKohdassa(tuoteRivinOikeaKlikkausValikko.getTriggerLocation()));
        });
        tuoteRivinOikeaKlikkausValikko.add(poistaValinta1);

        LocationPopupMenu tuotteettomanRivinOikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem poistaValinta2 = new JMenuItem("Poista");
        poistaValinta2.addActionListener((ActionEvent e) -> {

            OstoskoriRajapinta.poistaRivi(riviKohdassa(tuotteettomanRivinOikeaKlikkausValikko.getTriggerLocation()));
        });
        tuotteettomanRivinOikeaKlikkausValikko.add(poistaValinta2);

        /* Hiirikäsittelijä oikeaklikkausvalikoille */
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
                    if (tuoteKohdassa(e.getPoint()).isPresent()) {
                        tuoteRivinOikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                    } else {
                        tuotteettomanRivinOikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private OstoskoriRivi riviKohdassa(Point kohta) {
        return ((AdvancedTableModel<OstoskoriRivi>) getModel()).getElementAt(rowAtPoint(kohta));
    }

    private Optional<Tuote> tuoteKohdassa(Point kohta) {
        return riviKohdassa(kohta).getEnsisijainenTuote();
    }

    private void avaaSelaimessa(Tuote tuote) {
        try {
            Desktop.getDesktop().browse(URI.create(tuote.getUrl().toString()));
        } catch (IOException | UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(this, "Tuotteen avaaminen selaimessa epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
