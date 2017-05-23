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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JTable;
import tuupertunut.hintahaku.gui.util.TableColumnAdjuster;
import tuupertunut.hintahaku.ostoskori.Ostos;
import tuupertunut.hintahaku.ostoskori.OstoskoriRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class KaupoittainTable extends JTable {

    public KaupoittainTable() {
        getTableHeader().setResizingAllowed(false);
        getTableHeader().setReorderingAllowed(false);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);

        FilterList<Ostos> saatavillaOlevat = new FilterList<>(OstoskoriRajapinta.getEventList(), (Ostos o) -> o.getValittuHintatieto().isPresent());

//        GroupingList<Ostos> kaupoittain = new GroupingList<>(saatavillaOlevat, (Ostos o1, Ostos o2) -> o1.getValittuHintatieto().getKaupanNimi().compareTo(o2.getValittuHintatieto().getKaupanNimi()));

        /* GroupingListissä on bugi, jonka vuoksi samaan ryhmään kuuluvat
         * elementit jakautuvat välillä useaksi ryhmäksi. Tämän vuoksi joudutaan
         * pitämään yllä omaa ryhmiteltyä listaa. */
        EventList<List<Ostos>> kaupoittain = new BasicEventList<>();
        saatavillaOlevat.addListEventListener((ListEvent<Ostos> listChanges) -> {
            kaupoittain.clear();
            kaupoittain.addAll(saatavillaOlevat.stream().collect(Collectors.groupingBy((Ostos o) -> o.getValittuHintatieto().get().getKaupanNimi())).values());
        });

        SortedList<List<Ostos>> jarjestetty = new SortedList<>(kaupoittain, (List<Ostos> o1, List<Ostos> o2) -> 0);

        setModel(GlazedListsSwing.eventTableModel(jarjestetty, new KaupoittainTableFormat()));
        setSelectionModel(GlazedListsSwing.eventSelectionModel(jarjestetty));

        TableComparatorChooser.install(this, jarjestetty, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);

        new TableColumnAdjuster(this).setDynamicAdjustment(true);
    }
}
