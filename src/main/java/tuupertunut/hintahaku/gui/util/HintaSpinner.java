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
package tuupertunut.hintahaku.gui.util;

import java.text.ParseException;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.text.DefaultFormatterFactory;
import tuupertunut.hintahaku.core.Hinta;

/**
 *
 * @author Tuupertunut
 */
public class HintaSpinner extends JSpinner {

    public HintaSpinner() {
        setModel(new SpinnerHintaModel());
        setEditor(new HintaEditor(this));
    }

    private class HintaEditor extends JSpinner.DefaultEditor {

        private HintaEditor(JSpinner spinner) {
            super(spinner);
            getTextField().setFormatterFactory(new DefaultFormatterFactory(new HintaFormatter()));
            getTextField().setEditable(true);
        }

        private class HintaFormatter extends JFormattedTextField.AbstractFormatter {

            @Override
            public Object stringToValue(String text) throws ParseException {
                try {
                    return Hinta.parse(text);
                } catch (IllegalArgumentException ex) {
                    throw new ParseException(ex.getMessage(), 0);
                }
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                return value != null ? value.toString() : null;
            }
        }
    }

    private class SpinnerHintaModel extends AbstractSpinnerModel {

        private Hinta hinta;

        @Override
        public Object getValue() {
            return hinta;
        }

        @Override
        public void setValue(Object value) {
            if ((value == null) || !(value instanceof Hinta)) {
                throw new IllegalArgumentException("Syöte ei ole Hinta");
            }
            if (!value.equals(hinta)) {
                hinta = (Hinta) value;
                fireStateChanged();
            }
        }

        @Override
        public Object getNextValue() {
            return hinta.sentinEnemman();
        }

        @Override
        public Object getPreviousValue() {
            return hinta.sentinVahemman();
        }
    }
}
