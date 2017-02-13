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
package tuupertunut.hintahaku.core;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tuupertunut
 */
public class HintaFiUrl {

    private static final Pattern PATTERN = Pattern.compile("(http(s)?://)?(www\\.)?hinta\\.fi/(?<numerot>\\d+)(/.*)?");

    /* Hinta.fi:n URL:ssa oleva numerosarja, joka yksilöi tuotteen. */
    private final String numerot;

    public HintaFiUrl(String numerot) {
        this.numerot = numerot;
    }

    @Override
    public String toString() {
        return "http://hinta.fi/" + numerot;
    }

    public static HintaFiUrl parse(String url) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(url);
        if (matcher.matches()) {
            return new HintaFiUrl(matcher.group("numerot"));
        } else {
            throw new IllegalArgumentException("Ei kelvollinen hinta.fi-url");
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.numerot);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HintaFiUrl other = (HintaFiUrl) obj;
        if (!Objects.equals(this.numerot, other.numerot)) {
            return false;
        }
        return true;
    }
}
