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
package tuupertunut.hintahaku.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import tuupertunut.hintahaku.gui.HintatiedotTable;

/**
 *
 * @author Tuupertunut
 */
public class VaritettyHintatietoDefaultTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color ALTERNATE_COLOR = UIManager.getColor("Table.alternateRowColor");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        /* Lisätään mahdollinen erikoisväri komponenttiin.
         * DefaultTableCellRenderer ei osaa palauttaa oletusväriä kunnolla,
         * joten joudutaan myös palauttamaan se manuaalisesti, jos erikoisväriä
         * ei ole. */
        if (!isSelected) {
            Color rivinErikoisvari = ((HintatiedotTable) table).getRivinErikoisvari(row);
            if (rivinErikoisvari != null) {
                setBackground(rivinErikoisvari);
            } else {
                setBackground(ALTERNATE_COLOR != null && row % 2 == 1 ? ALTERNATE_COLOR : new Color(table.getBackground().getRGB()));
            }
        }

        return this;
    }
}
