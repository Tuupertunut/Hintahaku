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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.Tuote;

/**
 *
 * @author Tuupertunut
 */
public class VaihtoehtoRivi implements OstoskoriRivi {

    private final Tuote tuote;
    private final OstosRivi isantaRivi;

    public VaihtoehtoRivi(Tuote tuote, OstosRivi isantaRivi) {
        this.tuote = tuote;
        this.isantaRivi = isantaRivi;
    }

    public OstosRivi getIsantaRivi() {
        return isantaRivi;
    }

    @Override
    public String getTuotteenNimi() {
        return tuote.getNimi();
    }

    @Override
    public String getKaupanNimi() {
        return "";
    }

    @Override
    public String getToimitusaika() {
        return "";
    }

    @Override
    public Hinta getKappalehinta() {
        return null;
    }

    @Override
    public Integer getMaara() {
        return null;
    }

    @Override
    public void setMaara(int maara) {
    }

    @Override
    public Optional<Tuote> getEnsisijainenTuote() {
        return Optional.of(tuote);
    }

    @Override
    public Ostos getOstos() {
        return isantaRivi.getOstos();
    }

    @Override
    public List<OstoskoriRivi> getPath() {
        return Arrays.asList(isantaRivi, this);
    }
}
