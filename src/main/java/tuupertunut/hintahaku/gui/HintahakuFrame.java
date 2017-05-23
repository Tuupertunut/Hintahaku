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
import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import org.jdom2.JDOMException;
import org.jsoup.HttpStatusException;
import tuupertunut.hintahaku.asetukset.AsetusRajapinta;
import tuupertunut.hintahaku.core.Hinta;
import tuupertunut.hintahaku.core.HintaFiHakija;
import tuupertunut.hintahaku.core.HintaFiUrl;
import tuupertunut.hintahaku.core.Hintatieto;
import tuupertunut.hintahaku.core.Tuote;
import tuupertunut.hintahaku.event.ChangeEvent;
import tuupertunut.hintahaku.gui.util.LocationPopupMenu;
import tuupertunut.hintahaku.gui.util.Tiedostovalitsimet;
import tuupertunut.hintahaku.haku.HaettuTuoteRajapinta;
import tuupertunut.hintahaku.hintatiedot.HintatiedotRajapinta;
import tuupertunut.hintahaku.korjaukset.KorjauksenKohde;
import tuupertunut.hintahaku.korjaukset.Korjaus;
import tuupertunut.hintahaku.korjaukset.KorjausRajapinta;
import tuupertunut.hintahaku.ostoskori.OstoskoriRajapinta;
import tuupertunut.hintahaku.ostoskori.OstoskoriRivi;
import tuupertunut.hintahaku.ostoskori.RaakaOstoskori;
import tuupertunut.hintahaku.suodattimet.SuodatinRajapinta;

/**
 *
 * @author Tuupertunut
 */
public class HintahakuFrame extends javax.swing.JFrame {

    private final AdvancedListSelectionModel<Tuote> haettuTuoteTaulukkoSelectionModel;
    private final AdvancedListSelectionModel<OstoskoriRivi> ostoskoriTaulukkoSelectionModel;
    private final AdvancedListSelectionModel<Korjaus> korjauksetTaulukkoSelectionModel;

    private boolean haetunTuotteenAiheuttamaMuutos = false;
    private boolean ostoskorinAiheuttamaMuutos = false;

    private SwingWorker<Tuote, Void> kaynnissaOlevaTuotteenHaku = null;
    private String virheHaussa = null;
    private SwingWorker<Void, Void> kaynnissaOlevaOstoskorinAvaus = null;

    /** Creates new form HintahakuFrame */
    public HintahakuFrame() {
        initComponents();

        haettuTuoteTaulukkoSelectionModel = (AdvancedListSelectionModel<Tuote>) haettuTuoteTaulukko.getSelectionModel();
        ostoskoriTaulukkoSelectionModel = (AdvancedListSelectionModel<OstoskoriRivi>) ostoskoriTaulukko.getSelectionModel();
        korjauksetTaulukkoSelectionModel = (AdvancedListSelectionModel<Korjaus>) korjauksetTaulukko.getSelectionModel();

        urlKentta.requestFocusInWindow();

        hakuIlmoitusKentta.setText("");
        suodattimetTiedostoKentta.setText(AsetusRajapinta.getSuodatintiedosto().getFileName().toString());
        ostoskoriTiedostoKentta.setText("");
        kokonaisMaaraKentta.setText("0");
        kokonaisHintaKentta.setText("0,00 €");
        ostoskoriTallennaNappi.setEnabled(false);
        lisaaOstoskoriinNappi.setEnabled(false);
        lisaaVaihtoehdoksiNappi.setEnabled(false);

        luoUrlKentanOikeaKlikkausValikko();
        luoHintatiedotTaulukonOikeaKlikkausValikot();

        OstoskoriRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskoritiedosto")) {
                paivitaOstoskoriTiedostoKentta();
            }
        });

        OstoskoriRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskori")) {
                paivitaOstoskoriTallennaNappi();
            }
        });

        OstoskoriRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskori")) {
                paivitaOstoskorinMaaraJaHinta();
            }
        });

        /* Ostoskorin tuotteet ja haettu tuote eivät voi olla valittuna samaan
         * aikaan. Rivin valitseminen toisessa taulukossa poistaa valinnan
         * toisesta. Lisäksi täytyy käyttää kahden boolean-muuttujan
         * turvamekanismia, jotta toisen taulukon valinnan poistamisen
         * aiheuttamia muutoksia ei lasketa. Muutoin seuraisi tapahtumakierre,
         * jossa molemmat muutokset aiheuttaisivat toisensa. */
        ostoskoriTaulukkoSelectionModel.addListSelectionListener((ListSelectionEvent e) -> {
            if (!haetunTuotteenAiheuttamaMuutos) {
                ostoskorinAiheuttamaMuutos = true;
                haettuTuoteTaulukkoSelectionModel.getTogglingSelected().clear();
                ostoskorinAiheuttamaMuutos = false;

                paivitaHintatiedotTaulukko();
            }
        });

        haettuTuoteTaulukkoSelectionModel.addListSelectionListener((ListSelectionEvent e) -> {
            if (!ostoskorinAiheuttamaMuutos) {
                haetunTuotteenAiheuttamaMuutos = true;
                ostoskoriTaulukkoSelectionModel.getTogglingSelected().clear();
                haetunTuotteenAiheuttamaMuutos = false;

                paivitaHintatiedotTaulukko();
            }
        });

        OstoskoriRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("ostoskori") && !evt.isRootEvent()) {
                paivitaHintatiedotTaulukko();
            }
        });

        ostoskoriTaulukkoSelectionModel.addListSelectionListener((ListSelectionEvent e) -> {
            paivitaLisaaOstoskoriinNapit();
        });

        HaettuTuoteRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("tuote") && evt.isRootEvent()) {
                paivitaLisaaOstoskoriinNapit();
            }
        });

        AsetusRajapinta.addListener((ChangeEvent evt) -> {
            if (evt.getChangedProperty().equals("asetukset") && !evt.isRootEvent() && evt.getRootEvent().getChangedProperty().equals("suodatintiedosto")) {
                paivitaSuodattimetTiedostoKentta();
            }
        });
    }

    private void luoUrlKentanOikeaKlikkausValikko() {
        /* Luodaan url-kentälle normaali leikkaa, kopioi, liitä -tyyppinen
         * oikeaklikkausvalikko. */
        JPopupMenu urlKenttaOikeaKlikkausValikko = new JPopupMenu();

        JMenuItem leikkaaValinta = new JMenuItem("Leikkaa");
        leikkaaValinta.addActionListener((ActionEvent e) -> {
            urlKentta.cut();
        });
        urlKenttaOikeaKlikkausValikko.add(leikkaaValinta);

        JMenuItem kopioiValinta = new JMenuItem("Kopioi");
        kopioiValinta.addActionListener((ActionEvent e) -> {
            urlKentta.copy();
        });
        urlKenttaOikeaKlikkausValikko.add(kopioiValinta);

        JMenuItem liitaValinta = new JMenuItem("Liitä");
        liitaValinta.addActionListener((ActionEvent e) -> {
            urlKentta.paste();
        });
        urlKenttaOikeaKlikkausValikko.add(liitaValinta);

        urlKenttaOikeaKlikkausValikko.add(new JSeparator());

        JMenuItem valitseKaikkiValinta = new JMenuItem("Valitse kaikki");
        valitseKaikkiValinta.addActionListener((ActionEvent e) -> {
            urlKentta.selectAll();
        });
        urlKenttaOikeaKlikkausValikko.add(valitseKaikkiValinta);

        /* Hiirikäsittelijä oikeaklikkausvalikolle */
        urlKentta.addMouseListener(new MouseAdapter() {

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
                    urlKenttaOikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void luoHintatiedotTaulukonOikeaKlikkausValikot() {
        /* Luodaan kaksi oikeaklikkausvalikkoa hintatiedot-taulukolle. Toinen
         * hinnoille, toinen postikuluille. Valikoista lisätään uusia
         * korjauksia. */
        LocationPopupMenu hintaOikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem korjaaHintaValinta = new JMenuItem("Korjaa hinta");
        korjaaHintaValinta.addActionListener((ActionEvent e) -> {

            luoHintaKorjaus(hintatiedotTaulukko.hintatietoKohdassa(hintaOikeaKlikkausValikko.getTriggerLocation()));
        });
        hintaOikeaKlikkausValikko.add(korjaaHintaValinta);

        LocationPopupMenu postikulutOikeaKlikkausValikko = new LocationPopupMenu();

        JMenuItem korjaaPostikulutValinta = new JMenuItem("Korjaa postikulut");
        korjaaPostikulutValinta.addActionListener((ActionEvent e) -> {

            luoPostikuluKorjaus(hintatiedotTaulukko.hintatietoKohdassa(postikulutOikeaKlikkausValikko.getTriggerLocation()));
        });
        postikulutOikeaKlikkausValikko.add(korjaaPostikulutValinta);

        /* Hiirikäsittelijä oikeaklikkausvalikoille */
        hintatiedotTaulukko.addMouseListener(new MouseAdapter() {

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
                    if (hintatiedotTaulukko.columnAtPoint(e.getPoint()) == 1) { // Sarake 1 on "Hinta noudolla".
                        hintaOikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                    } else if (hintatiedotTaulukko.columnAtPoint(e.getPoint()) == 3) { // Sarake 3 on "Postikulut".
                        postikulutOikeaKlikkausValikko.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void luoHintaKorjaus(Hintatieto hintatieto) {
        Tuote tuote = hintatieto.getTuote();
        String kaupanNimi = hintatieto.getKaupanNimi();

        /* Jos korjausta ei vielä ole, luodaan sellainen käyttäen oletusarvona
         * nykyistä hintaa. */
        if (!KorjausRajapinta.onkoKorjausta(tuote, kaupanNimi, KorjauksenKohde.HINTA)) {
            Hinta oletusHinta = hintatieto.getHinta();
            KorjausRajapinta.lisaaKorjaus(new Korjaus(tuote.getUrl(), tuote.getNimi(), kaupanNimi, KorjauksenKohde.HINTA, oletusHinta));
        }

        Korjaus luotuKorjaus = KorjausRajapinta.haeKorjaus(tuote, kaupanNimi, KorjauksenKohde.HINTA);

        /* Vaihdetaan mukauttaminen-välilehteen ja korostetaan juuri lisätty
         * korjausrivi. */
        valilehtiPaneeli.setSelectedComponent(mukauttaminenValilehtiPaneeli);
        EventList<Korjaus> valitutKorjaukset = korjauksetTaulukkoSelectionModel.getTogglingSelected();
        valitutKorjaukset.clear();
        valitutKorjaukset.add(luotuKorjaus);
    }

    private void luoPostikuluKorjaus(Hintatieto hintatieto) {
        Tuote tuote = hintatieto.getTuote();
        String kaupanNimi = hintatieto.getKaupanNimi();

        /* Jos korjausta ei vielä ole, luodaan sellainen käyttäen oletusarvona
         * nykyisiä postikuluja. */
        if (!KorjausRajapinta.onkoKorjausta(tuote, kaupanNimi, KorjauksenKohde.POSTIKULUT)) {
            Hinta oletusPostikulut = hintatieto.getPostikulut().orElse(new Hinta(0));
            KorjausRajapinta.lisaaKorjaus(new Korjaus(tuote.getUrl(), tuote.getNimi(), kaupanNimi, KorjauksenKohde.POSTIKULUT, oletusPostikulut));
        }

        Korjaus luotuKorjaus = KorjausRajapinta.haeKorjaus(tuote, kaupanNimi, KorjauksenKohde.POSTIKULUT);

        /* Vaihdetaan mukauttaminen-välilehteen ja korostetaan juuri lisätty
         * korjausrivi. */
        valilehtiPaneeli.setSelectedComponent(mukauttaminenValilehtiPaneeli);
        EventList<Korjaus> valitutKorjaukset = korjauksetTaulukkoSelectionModel.getTogglingSelected();
        valitutKorjaukset.clear();
        valitutKorjaukset.add(luotuKorjaus);
    }

    private void paivitaOstoskoriTiedostoKentta() {
        if (kaynnissaOlevaOstoskorinAvaus != null) {
            ostoskoriTiedostoKentta.setText("Haetaan ostoskorin hintatietoja...");
        } else {
            Path tiedosto = OstoskoriRajapinta.getOstoskoritiedosto();
            ostoskoriTiedostoKentta.setText(tiedosto != null ? tiedosto.getFileName().toString() : "");
        }
    }

    private void paivitaOstoskoriTallennaNappi() {
        ostoskoriTallennaNappi.setEnabled(OstoskoriRajapinta.getOstoskoritiedosto() != null && !OstoskoriRajapinta.onkoTiedostoAjanTasalla());
    }

    private void paivitaOstoskorinMaaraJaHinta() {
        kokonaisMaaraKentta.setText(Integer.toString(OstoskoriRajapinta.getKokonaisMaara()));
        kokonaisHintaKentta.setText(OstoskoriRajapinta.getKokonaisHinta().toString());
    }

    private void paivitaHintatiedotTaulukko() {
        EventList<Tuote> valitutHaetutTuotteet = haettuTuoteTaulukkoSelectionModel.getSelected();
        if (!valitutHaetutTuotteet.isEmpty()) {
            HintatiedotRajapinta.naytaTuote(Optional.of(valitutHaetutTuotteet.get(0)));
        } else {
            EventList<OstoskoriRivi> valitutOstoskoriRivit = ostoskoriTaulukkoSelectionModel.getSelected();
            if (!valitutOstoskoriRivit.isEmpty()) {

                /* Jos ostoskorista on valittu monta riviä, ei välitetä kuin
                 * ensimmäisestä. */
                OstoskoriRivi ensimmainenValittuRivi = valitutOstoskoriRivit.get(0);
                Optional<Tuote> rivinEnsisijainenTuote = ensimmainenValittuRivi.getEnsisijainenTuote();
                Optional<Hintatieto> rivinTuotteenValittuHintatieto = ensimmainenValittuRivi.getOstos().getValittuHintatieto().filter((Hintatieto ht) -> {

                    /* Löytyykö rivin ostoksen valittu hintatieto rivin
                     * tuotteesta? */
                    return rivinEnsisijainenTuote.isPresent() && ht.getTuote().equals(rivinEnsisijainenTuote.get());
                });

                HintatiedotRajapinta.naytaTuote(rivinEnsisijainenTuote, rivinTuotteenValittuHintatieto);
            } else {
                HintatiedotRajapinta.naytaTuote(Optional.empty());
            }
        }
    }

    private void paivitaLisaaOstoskoriinNapit() {
        lisaaOstoskoriinNappi.setEnabled(HaettuTuoteRajapinta.onkoTuotetta());
        lisaaVaihtoehdoksiNappi.setEnabled(HaettuTuoteRajapinta.onkoTuotetta() && !ostoskoriTaulukkoSelectionModel.getSelected().isEmpty());
    }

    private void paivitaSuodattimetTiedostoKentta() {
        Path tiedosto = AsetusRajapinta.getSuodatintiedosto();
        suodattimetTiedostoKentta.setText(tiedosto.getFileName().toString());
    }

    private void paivitaHakuIlmoitusKentta() {
        if (kaynnissaOlevaTuotteenHaku != null) {
            hakuIlmoitusKentta.setForeground(null);
            hakuIlmoitusKentta.setText("Haetaan hintatietoja...");
        } else {
            hakuIlmoitusKentta.setForeground(Color.RED);
            hakuIlmoitusKentta.setText(virheHaussa);
        }
    }

    /** This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        valilehtiPaneeli = new javax.swing.JTabbedPane();
        hakuValilehtiPaneeli = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        ostoskoriTaulukko = new tuupertunut.hintahaku.gui.OstoskoriTable();
        jPanel22 = new javax.swing.JPanel();
        ostoskoriPoistaNappi = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        kaupoittainTaulukko = new tuupertunut.hintahaku.gui.KaupoittainTable();
        jPanel3 = new javax.swing.JPanel();
        kokonaisMaaraKentta = new javax.swing.JLabel();
        kokonaisHintaKentta = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel8 = new javax.swing.JPanel();
        ostoskoriTallennaNappi = new javax.swing.JButton();
        ostoskoriTallennaNimellaNappi = new javax.swing.JButton();
        ostoskoriAvaaNappi = new javax.swing.JButton();
        ostoskoriUusiNappi = new javax.swing.JButton();
        ostoskoriTiedostoKentta = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        haettuTuoteTaulukko = new tuupertunut.hintahaku.gui.HaettuTuoteTable();
        jPanel11 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        urlKentta = new javax.swing.JTextField();
        haeNappi = new javax.swing.JButton();
        hakuIlmoitusKentta = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        lisaaOstoskoriinNappi = new javax.swing.JButton();
        lisaaVaihtoehdoksiNappi = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        hintatiedotTaulukko = new tuupertunut.hintahaku.gui.HintatiedotTable();
        jSeparator2 = new javax.swing.JSeparator();
        mukauttaminenValilehtiPaneeli = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel17 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        suodattimetTaulukko = new tuupertunut.hintahaku.gui.SuodattimetTable();
        jPanel20 = new javax.swing.JPanel();
        suodattimetMonistaNappi = new javax.swing.JButton();
        suodattimetVaihdaNappi = new javax.swing.JButton();
        suodattimetUusiNappi = new javax.swing.JButton();
        suodattimetTiedostoKentta = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel18 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        korjauksetTaulukko = new tuupertunut.hintahaku.gui.KorjauksetTable();
        korjauksetPoistaNappi = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hintahaku");

        valilehtiPaneeli.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                valilehtiPaneeliStateChanged(evt);
            }
        });

        jSplitPane1.setDividerLocation(700);
        jSplitPane1.setResizeWeight(0.5);

        jSplitPane2.setDividerLocation(350);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel3.setText("Ostoskori");

        jScrollPane6.setViewportView(ostoskoriTaulukko);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(618, Short.MAX_VALUE))
            .addComponent(jScrollPane6)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE))
        );

        ostoskoriPoistaNappi.setText("Poista valitut");
        ostoskoriPoistaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ostoskoriPoistaNappiActionPerformed(evt);
            }
        });

        jLabel12.setText("Avaa selaimessa tuplaklikkaamalla tuotetta");

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addComponent(ostoskoriPoistaNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel12))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ostoskoriPoistaNappi)
                    .addComponent(jLabel12)))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane2.setTopComponent(jPanel4);

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel2.setText("Kaupoittain");

        jScrollPane1.setViewportView(kaupoittainTaulukko);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addContainerGap(607, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel5);

        kokonaisMaaraKentta.setText("kokonaisMaaraKentta");

        kokonaisHintaKentta.setFont(kokonaisHintaKentta.getFont().deriveFont(kokonaisHintaKentta.getFont().getSize()+4f));
        kokonaisHintaKentta.setText("kokonaisHintaKentta");

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel1.setText("Tuotteiden määrä:");

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD, jLabel4.getFont().getSize()+4));
        jLabel4.setText("Kokonaishinta:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(kokonaisHintaKentta))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(kokonaisMaaraKentta)))
                .addGap(0, 0, 0))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(kokonaisMaaraKentta)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(kokonaisHintaKentta)
                    .addComponent(jLabel4)))
        );

        ostoskoriTallennaNappi.setText("Tallenna");
        ostoskoriTallennaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ostoskoriTallennaNappiActionPerformed(evt);
            }
        });

        ostoskoriTallennaNimellaNappi.setText("Tallenna nimellä...");
        ostoskoriTallennaNimellaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ostoskoriTallennaNimellaNappiActionPerformed(evt);
            }
        });

        ostoskoriAvaaNappi.setText("Avaa...");
        ostoskoriAvaaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ostoskoriAvaaNappiActionPerformed(evt);
            }
        });

        ostoskoriUusiNappi.setText("Uusi");
        ostoskoriUusiNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ostoskoriUusiNappiActionPerformed(evt);
            }
        });

        ostoskoriTiedostoKentta.setText("ostoskoriTiedostoKentta");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ostoskoriTiedostoKentta)
                .addGap(18, 18, 18)
                .addComponent(ostoskoriUusiNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ostoskoriAvaaNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ostoskoriTallennaNimellaNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ostoskoriTallennaNappi)
                .addGap(5, 5, 5))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ostoskoriTallennaNappi)
                    .addComponent(ostoskoriTallennaNimellaNappi)
                    .addComponent(ostoskoriAvaaNappi)
                    .addComponent(ostoskoriUusiNappi)
                    .addComponent(ostoskoriTiedostoKentta))
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 704, Short.MAX_VALUE)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jSplitPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel6.setText("Haettu tuote");

        jScrollPane7.setViewportView(haettuTuoteTaulukko);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(539, Short.MAX_VALUE))
            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel5.setText("URL:");

        haeNappi.setText("Hae");
        haeNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                haeNappiActionPerformed(evt);
            }
        });

        hakuIlmoitusKentta.setText("hakuIlmoitusKentta");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(hakuIlmoitusKentta)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(urlKentta, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(haeNappi))))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlKentta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(haeNappi)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hakuIlmoitusKentta))
        );

        lisaaOstoskoriinNappi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/nuoli.png"))); // NOI18N
        lisaaOstoskoriinNappi.setToolTipText("Lisää ostoskoriin");
        lisaaOstoskoriinNappi.setPreferredSize(new java.awt.Dimension(32, 32));
        lisaaOstoskoriinNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lisaaOstoskoriinNappiActionPerformed(evt);
            }
        });

        lisaaVaihtoehdoksiNappi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/alinuoli.png"))); // NOI18N
        lisaaVaihtoehdoksiNappi.setToolTipText("Lisää vaihtoehdoksi valitulle tuotteelle");
        lisaaVaihtoehdoksiNappi.setPreferredSize(new java.awt.Dimension(32, 32));
        lisaaVaihtoehdoksiNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lisaaVaihtoehdoksiNappiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lisaaOstoskoriinNappi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lisaaVaihtoehdoksiNappi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(lisaaOstoskoriinNappi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lisaaVaihtoehdoksiNappi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel7.setText("halvin kauppa");

        jPanel15.setBackground(new java.awt.Color(141, 213, 144));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

        jPanel16.setBackground(new java.awt.Color(224, 154, 95));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 18, Short.MAX_VALUE)
        );

        jLabel9.setText("ostoskorin valittu kauppa");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jLabel7)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel9))
        );

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel8.setText("Kaupat");

        jScrollPane3.setViewportView(hintatiedotTaulukko);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 356, Short.MAX_VALUE)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane3)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane3))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        javax.swing.GroupLayout hakuValilehtiPaneeliLayout = new javax.swing.GroupLayout(hakuValilehtiPaneeli);
        hakuValilehtiPaneeli.setLayout(hakuValilehtiPaneeliLayout);
        hakuValilehtiPaneeliLayout.setHorizontalGroup(
            hakuValilehtiPaneeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        hakuValilehtiPaneeliLayout.setVerticalGroup(
            hakuValilehtiPaneeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        valilehtiPaneeli.addTab("Haku", hakuValilehtiPaneeli);

        jSplitPane3.setDividerLocation(500);
        jSplitPane3.setResizeWeight(0.5);

        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel10.setText("Suodattimet");

        jScrollPane4.setViewportView(suodattimetTaulukko);

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(398, Short.MAX_VALUE))
            .addComponent(jScrollPane4)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE))
        );

        suodattimetMonistaNappi.setText("Monista...");
        suodattimetMonistaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suodattimetMonistaNappiActionPerformed(evt);
            }
        });

        suodattimetVaihdaNappi.setText("Vaihda...");
        suodattimetVaihdaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suodattimetVaihdaNappiActionPerformed(evt);
            }
        });

        suodattimetUusiNappi.setText("Uusi...");
        suodattimetUusiNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suodattimetUusiNappiActionPerformed(evt);
            }
        });

        suodattimetTiedostoKentta.setText("suodattimetTiedostoKentta");

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(suodattimetTiedostoKentta)
                .addGap(18, 18, 18)
                .addComponent(suodattimetUusiNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(suodattimetVaihdaNappi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(suodattimetMonistaNappi)
                .addGap(5, 5, 5))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(suodattimetMonistaNappi)
                    .addComponent(suodattimetVaihdaNappi)
                    .addComponent(suodattimetUusiNappi)
                    .addComponent(suodattimetTiedostoKentta))
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator3)
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPane3.setLeftComponent(jPanel17);

        jLabel11.setFont(jLabel11.getFont().deriveFont(jLabel11.getFont().getStyle() | java.awt.Font.BOLD));
        jLabel11.setText("Korjaukset");

        jScrollPane5.setViewportView(korjauksetTaulukko);

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addContainerGap(797, Short.MAX_VALUE))
            .addComponent(jScrollPane5)
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel21Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE))
        );

        korjauksetPoistaNappi.setText("Poista valitut");
        korjauksetPoistaNappi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                korjauksetPoistaNappiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(korjauksetPoistaNappi)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(korjauksetPoistaNappi)
                .addContainerGap())
        );

        jSplitPane3.setRightComponent(jPanel18);

        javax.swing.GroupLayout mukauttaminenValilehtiPaneeliLayout = new javax.swing.GroupLayout(mukauttaminenValilehtiPaneeli);
        mukauttaminenValilehtiPaneeli.setLayout(mukauttaminenValilehtiPaneeliLayout);
        mukauttaminenValilehtiPaneeliLayout.setHorizontalGroup(
            mukauttaminenValilehtiPaneeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );
        mukauttaminenValilehtiPaneeliLayout.setVerticalGroup(
            mukauttaminenValilehtiPaneeliLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );

        valilehtiPaneeli.addTab("Mukauttaminen", mukauttaminenValilehtiPaneeli);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(valilehtiPaneeli)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(valilehtiPaneeli)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void haeNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_haeNappiActionPerformed
        HintaFiUrl url;
        try {
            url = HintaFiUrl.parse(urlKentta.getText());
        } catch (IllegalArgumentException ex) {
            virheHaussa = "URL ei ole hinta.fi:n tuote!";
            paivitaHakuIlmoitusKentta();
            return;
        }

        /* Jos edellinen haku on jo käynnissä, keskeytetään se. */
        if (kaynnissaOlevaTuotteenHaku != null) {
            kaynnissaOlevaTuotteenHaku.cancel(true);
        }

        kaynnissaOlevaTuotteenHaku = new SwingWorker<Tuote, Void>() {

            @Override
            protected Tuote doInBackground() throws Exception {
                return HintaFiHakija.haeTuote(url);
            }

            @Override
            protected void done() {
                try {
                    Tuote tuote = get();

                    /* Asetetaan tuote haetuksi tuotteeksi. */
                    HaettuTuoteRajapinta.setTuote(Optional.of(tuote));

                    /* Valitaan haettu tuote taulukossa. */
                    EventList<Tuote> valitutHaetutTuotteet = haettuTuoteTaulukkoSelectionModel.getTogglingSelected();
                    valitutHaetutTuotteet.clear();
                    valitutHaetutTuotteet.add(tuote);

                    virheHaussa = null;
                } catch (InterruptedException | CancellationException ex) {
                } catch (ExecutionException ex) {
                    try {
                        throw (Exception) ex.getCause();
                    } catch (HttpStatusException ex1) {
                        switch (ex1.getStatusCode()) {
                            case 404:
                                virheHaussa = "Haettua tuotetta ei ole olemassa!";
                                break;
                            case 503:
                                virheHaussa = "Tuotteen haku ei väliaikaisesti ole saatavilla!";
                                break;
                            default:
                                virheHaussa = "Ohjelma ei saa yhteyttä hinta.fi-palvelimeen!";
                        }
                    } catch (IOException ex1) {
                        virheHaussa = "Ohjelma ei saa yhteyttä hinta.fi-palvelimeen!";
                    } catch (Exception ex1) {
                        virheHaussa = "Tuntematon virhe!";
                    }
                }

                /* Jos parhaillaan käynnissä oleva haku on tämä haku, merkataan
                 * se loppuneeksi. */
                if (kaynnissaOlevaTuotteenHaku == this) {
                    kaynnissaOlevaTuotteenHaku = null;
                    paivitaHakuIlmoitusKentta();
                }
            }
        };
        paivitaHakuIlmoitusKentta();
        kaynnissaOlevaTuotteenHaku.execute();
    }//GEN-LAST:event_haeNappiActionPerformed

    private void valilehtiPaneeliStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_valilehtiPaneeliStateChanged
        if (valilehtiPaneeli.getSelectedComponent().equals(hakuValilehtiPaneeli)) {
            rootPane.setDefaultButton(haeNappi);
        } else {
            rootPane.setDefaultButton(null);
        }
    }//GEN-LAST:event_valilehtiPaneeliStateChanged

    private void ostoskoriAvaaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ostoskoriAvaaNappiActionPerformed
        Tiedostovalitsimet.avaaOstoskoriValitsimella(this, null, "Ostoskorit (xml)", "xml", (Path avattava) -> {

            RaakaOstoskori raakaOstoskori;
            try {
                raakaOstoskori = OstoskoriRajapinta.muodostaRaakaOstoskori(avattava);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Tiedoston avaaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (JDOMException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Avattava tiedosto ei ole ostoskori, tai se on virheellinen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                return;
            }

            /* Jos edellinen avaus on jo käynnissä, keskeytetään se. */
            if (kaynnissaOlevaOstoskorinAvaus != null) {
                kaynnissaOlevaOstoskorinAvaus.cancel(true);
            }

            kaynnissaOlevaOstoskorinAvaus = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    raakaOstoskori.haeTuotteet();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        OstoskoriRajapinta.avaa(raakaOstoskori, avattava);
                    } catch (InterruptedException | CancellationException ex) {
                    } catch (ExecutionException ex) {
                        try {
                            throw (Exception) ex.getCause();
                        } catch (HttpStatusException ex1) {
                            switch (ex1.getStatusCode()) {
                                case 404:
                                    JOptionPane.showMessageDialog(HintahakuFrame.this, "Yhtä tai useampaa ostoskorin tuotetta ei ole olemassa!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                                    break;
                                case 503:
                                    JOptionPane.showMessageDialog(HintahakuFrame.this, "Tuotteiden haku ei väliaikaisesti ole saatavilla!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                                    break;
                                default:
                                    JOptionPane.showMessageDialog(HintahakuFrame.this, "Ohjelma ei saa yhteyttä hinta.fi-palvelimeen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (IOException ex1) {
                            JOptionPane.showMessageDialog(HintahakuFrame.this, "Ohjelma ei saa yhteyttä hinta.fi-palvelimeen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception ex1) {
                            JOptionPane.showMessageDialog(HintahakuFrame.this, "Tuntematon virhe!", "Virhe!", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    /* Jos parhaillaan käynnissä oleva avaus on tämä avaus,
                     * merkataan se loppuneeksi. */
                    if (kaynnissaOlevaOstoskorinAvaus == this) {
                        kaynnissaOlevaOstoskorinAvaus = null;
                        paivitaOstoskoriTiedostoKentta();
                    }
                }
            };
            paivitaOstoskoriTiedostoKentta();
            kaynnissaOlevaOstoskorinAvaus.execute();
        });
    }//GEN-LAST:event_ostoskoriAvaaNappiActionPerformed

    private void ostoskoriUusiNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ostoskoriUusiNappiActionPerformed
        Object[] napit = {"Kyllä", "Peruuta"};
        int valinta = JOptionPane.showOptionDialog(this, "Haluatko varmasti sulkea nykyisen ostoskorin ja aloittaa uuden?", "Varmistus", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, napit, napit[1]);
        if (valinta != JOptionPane.OK_OPTION) {
            return;
        }

        OstoskoriRajapinta.uusi();
    }//GEN-LAST:event_ostoskoriUusiNappiActionPerformed

    private void ostoskoriTallennaNimellaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ostoskoriTallennaNimellaNappiActionPerformed
        Tiedostovalitsimet.tallennaOstoskoriValitsimella(this, null, "Ostoskorit (xml)", "xml", (Path tallennettava) -> {

            try {
                OstoskoriRajapinta.tallennaNimella(tallennettava);

                paivitaOstoskoriTallennaNappi();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Tiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_ostoskoriTallennaNimellaNappiActionPerformed

    private void ostoskoriTallennaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ostoskoriTallennaNappiActionPerformed
        try {
            OstoskoriRajapinta.tallenna();

            paivitaOstoskoriTallennaNappi();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Tiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_ostoskoriTallennaNappiActionPerformed

    private void korjauksetPoistaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_korjauksetPoistaNappiActionPerformed
        for (Korjaus korjaus : new ArrayList<>(korjauksetTaulukkoSelectionModel.getSelected())) {
            KorjausRajapinta.poistaKorjaus(korjaus);
        }
    }//GEN-LAST:event_korjauksetPoistaNappiActionPerformed

    private void ostoskoriPoistaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ostoskoriPoistaNappiActionPerformed
        for (OstoskoriRivi rivi : new ArrayList<>(ostoskoriTaulukkoSelectionModel.getSelected())) {
            OstoskoriRajapinta.poistaRivi(rivi);
        }
    }//GEN-LAST:event_ostoskoriPoistaNappiActionPerformed

    private void lisaaOstoskoriinNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lisaaOstoskoriinNappiActionPerformed
        OstoskoriRajapinta.lisaaOstos(HaettuTuoteRajapinta.getTuote().get());
    }//GEN-LAST:event_lisaaOstoskoriinNappiActionPerformed

    private void lisaaVaihtoehdoksiNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lisaaVaihtoehdoksiNappiActionPerformed

        /* Jos ostoskorista on valittu monta riviä, ei välitetä kuin
         * ensimmäisestä. */
        OstoskoriRajapinta.lisaaVaihtoehto(ostoskoriTaulukkoSelectionModel.getSelected().get(0).getOstos(), HaettuTuoteRajapinta.getTuote().get());
    }//GEN-LAST:event_lisaaVaihtoehdoksiNappiActionPerformed

    private void suodattimetUusiNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suodattimetUusiNappiActionPerformed
        Tiedostovalitsimet.tallennaSuodattimetValitsimella(this, null, "Suodattimet (xml)", "xml", (Path tallennettava) -> {

            try {
                SuodatinRajapinta.uusi(tallennettava);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Uuden tiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_suodattimetUusiNappiActionPerformed

    private void suodattimetVaihdaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suodattimetVaihdaNappiActionPerformed
        Tiedostovalitsimet.avaaSuodattimetValitsimella(this, null, "Suodattimet (xml)", "xml", (Path avattava) -> {

            try {
                SuodatinRajapinta.vaihda(avattava);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Tiedoston avaaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            } catch (JDOMException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Avattava tiedosto ei ole suodatintiedosto, tai se on virheellinen!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_suodattimetVaihdaNappiActionPerformed

    private void suodattimetMonistaNappiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suodattimetMonistaNappiActionPerformed
        Tiedostovalitsimet.tallennaSuodattimetValitsimella(this, null, "Suodattimet (xml)", "xml", (Path tallennettava) -> {

            try {
                SuodatinRajapinta.monista(tallennettava);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Uuden tiedoston tallentaminen epäonnistui!", "Virhe!", JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_suodattimetMonistaNappiActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new HintahakuFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton haeNappi;
    private tuupertunut.hintahaku.gui.HaettuTuoteTable haettuTuoteTaulukko;
    private javax.swing.JLabel hakuIlmoitusKentta;
    private javax.swing.JPanel hakuValilehtiPaneeli;
    private tuupertunut.hintahaku.gui.HintatiedotTable hintatiedotTaulukko;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private tuupertunut.hintahaku.gui.KaupoittainTable kaupoittainTaulukko;
    private javax.swing.JLabel kokonaisHintaKentta;
    private javax.swing.JLabel kokonaisMaaraKentta;
    private javax.swing.JButton korjauksetPoistaNappi;
    private tuupertunut.hintahaku.gui.KorjauksetTable korjauksetTaulukko;
    private javax.swing.JButton lisaaOstoskoriinNappi;
    private javax.swing.JButton lisaaVaihtoehdoksiNappi;
    private javax.swing.JPanel mukauttaminenValilehtiPaneeli;
    private javax.swing.JButton ostoskoriAvaaNappi;
    private javax.swing.JButton ostoskoriPoistaNappi;
    private javax.swing.JButton ostoskoriTallennaNappi;
    private javax.swing.JButton ostoskoriTallennaNimellaNappi;
    private tuupertunut.hintahaku.gui.OstoskoriTable ostoskoriTaulukko;
    private javax.swing.JLabel ostoskoriTiedostoKentta;
    private javax.swing.JButton ostoskoriUusiNappi;
    private javax.swing.JButton suodattimetMonistaNappi;
    private tuupertunut.hintahaku.gui.SuodattimetTable suodattimetTaulukko;
    private javax.swing.JLabel suodattimetTiedostoKentta;
    private javax.swing.JButton suodattimetUusiNappi;
    private javax.swing.JButton suodattimetVaihdaNappi;
    private javax.swing.JTextField urlKentta;
    private javax.swing.JTabbedPane valilehtiPaneeli;
    // End of variables declaration//GEN-END:variables
}
