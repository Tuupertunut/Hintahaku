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
package tuupertunut.hintahaku.ostoskori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import tuupertunut.hintahaku.core.HintaFiUrl;

/**
 *
 * @author Tuupertunut
 */
public class RaakaOstos {

    private final List<HintaFiUrl> vaihtoehdot;
    private final int maara;

    public RaakaOstos(HintaFiUrl url, int maara) {
        this(Collections.singleton(url), maara);
    }

    public RaakaOstos(Collection<HintaFiUrl> vaihtoehdot, int maara) {
        this.vaihtoehdot = new ArrayList<>(vaihtoehdot);
        this.maara = maara;
    }

    public List<HintaFiUrl> getVaihtoehdot() {
        return vaihtoehdot;
    }

    public int getMaara() {
        return maara;
    }
}
