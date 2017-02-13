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

/**
 *
 * @author Tuupertunut
 */
public class Hinta implements Comparable<Hinta> {

    private static final int DESIMAALIEN_MAARA = 2;

    /* Hinta sentteinä, ei euroina. */
    private final long hinta;

    public Hinta(long hinta) {
        if (hinta < 0) {
            throw new IllegalArgumentException("Negatiivinen hinta");
        }
        this.hinta = hinta;
    }

    public Hinta plus(Hinta lisattava) {
        if (lisattava == null) {
            throw new NullPointerException("Lisättävä on null");
        }
        return new Hinta(this.hinta + lisattava.hinta);
    }

    public Hinta kertaa(int kerroin) {
        if (kerroin < 0) {
            throw new IllegalArgumentException("Kerroin on negatiivinen");
        }
        return new Hinta(this.hinta * kerroin);
    }

    public boolean onEnemmanKuin(Hinta verrattava) {
        if (verrattava == null) {
            throw new NullPointerException("Verrattava on null");
        }
        return this.hinta > verrattava.hinta;
    }

    public boolean onVahemmanKuin(Hinta verrattava) {
        if (verrattava == null) {
            throw new NullPointerException("Verrattava on null");
        }
        return this.hinta < verrattava.hinta;
    }

    public Hinta sentinEnemman() {
        return new Hinta(this.hinta + 1);
    }

    public Hinta sentinVahemman() {
        if (this.hinta > 0) {
            return new Hinta(this.hinta - 1);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {

        /* Muunnetaan luku tekstiksi. */
        StringBuilder luku = new StringBuilder(Long.toString(hinta));

        /* Lisätään luvun eteen nollia, kunnes luku on vähintään 3-numeroinen. */
        while (luku.length() < DESIMAALIEN_MAARA + 1) {
            luku.insert(0, "0");
        }

        /* Lisätään lukuun pilkku ja euronmerkki. */
        return luku.insert(luku.length() - DESIMAALIEN_MAARA, ",").append(" €").toString();
    }

    public static Hinta parse(String hinta) {
        String luku = hinta;

        /* Poistetaan kaikki euronmerkit ja välilyönnit luvusta. */
        luku = luku.replaceAll("[ €]", "");

        /* Tutkitaan, kuinka monta desimaalia luvussa on pilkun (tai pisteen)
         * jälkeen. */
        int viimeinenPilkunPaikka = Math.max(luku.lastIndexOf(','), luku.lastIndexOf('.'));
        boolean sisaltaaPilkun = viimeinenPilkunPaikka != -1;
        int numeroitaPilkunJalkeen = (luku.length() - 1) - viimeinenPilkunPaikka;

        /* Lisätään nollia pilkun perään, kunnes luvussa on kaksi desimaalia.
         * Jos pilkkua ei ole, luku tulkitaan kokonaisluvuksi, eli lisätään
         * kaksi desimaalia. Jos viimeisimmän pilkun jälkeen on 3 tai enemmän
         * numeroita, pilkku tulkitaan tuhaterottimeksi, ja lukua kohdellaan
         * kokonaislukuna. */
        int lisattavatNollat;
        if (!sisaltaaPilkun || numeroitaPilkunJalkeen > DESIMAALIEN_MAARA) {
            lisattavatNollat = DESIMAALIEN_MAARA;
        } else {
            lisattavatNollat = DESIMAALIEN_MAARA - numeroitaPilkunJalkeen;
        }

        for (int i = 0; i < lisattavatNollat; i++) {
            luku += '0';
        }

        /* Poistetaan pilkut. */
        luku = luku.replaceAll("[,.]", "");

        /* Muunnetaan teksti luvuksi. */
        try {
            return new Hinta(Long.parseLong(luku));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Syöte ei ole kelvollinen Hinta", ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.hinta ^ (this.hinta >>> 32));
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
        final Hinta other = (Hinta) obj;
        if (this.hinta != other.hinta) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Hinta o) {
        return Long.compare(this.hinta, o.hinta);
    }
}
