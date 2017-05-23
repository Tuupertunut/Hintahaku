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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import tuupertunut.hintahaku.util.RaakaHintatieto;

/**
 *
 * @author Tuupertunut
 */
public class HintaFiHakija {

    private static final String USER_AGENT = "Hintahaku-ohjelma";

    public static Tuote haeTuote(HintaFiUrl url) throws IOException {
        Document dok = haeHTML(url);

        String nimi = dok.select(".hv-content-box-head-title").text();

        List<RaakaHintatieto> hintatiedot = new ArrayList<>();

        for (Element hintatietoEl : dok.select(".hv-table-list-tr")) {

            Elements nimiEl = hintatietoEl.select(".hv--store");
            String kaupanNimi = nimiEl.hasText() ? nimiEl.text() : nimiEl.select("img.hv-store-logo").attr("alt");

            Hinta hinta = Hinta.parse(hintatietoEl.select(".hv--price").text());

            Elements kuluEl = hintatietoEl.select(".hv--delivery-fee");
            Optional<Hinta> postikulut = kuluEl.hasClass("hv--na") ? Optional.empty() : Optional.of(kuluEl.hasClass("hv--free") ? new Hinta(0) : Hinta.parse(kuluEl.text().replace("(", "").replace(")", "")));

            String toimitusaika = hintatietoEl.select(".hv--delivery-time").text().replace("Toimitusaika: ", "");

            hintatiedot.add(new RaakaHintatieto(kaupanNimi, hinta, postikulut, toimitusaika));
        }

        return new Tuote(url, nimi, hintatiedot);
    }

    private static Document haeHTML(HintaFiUrl url) throws IOException {
        return Jsoup.connect(url.toString()).userAgent(USER_AGENT).get();
    }
}
