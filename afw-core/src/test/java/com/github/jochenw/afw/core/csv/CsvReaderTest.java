package com.github.jochenw.afw.core.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.csv.CsvReader.CsvRow;
import com.github.jochenw.afw.core.util.Consumer;
import com.github.jochenw.afw.core.util.FinalizableConsumer;
import com.github.jochenw.afw.core.util.Tests;

import org.junit.Assert;


public class CsvReaderTest {
	private static final String LINE1 = "\"Stammdaten\";\"288362\";\"42/17\";\"IE\";\"verwendbar\";" +
								 "\"Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC\";" +
								 "\"Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC 4 fach sort.\";" +
								 "\"MW\";\"NonFood\";\"Projektartikel_NF\";\"115.108 Küchenhelfer\";" +
								 "\"45.2 X-MAS Küche (Food Expo Deluxe 1)\";;;;\"1\";\"1\";\"Stk.\";\"PK\";;;;\"279620\";" +
								 "\"DE: , FR: , IT: , ES: , GB: , BE: 42/17, PT: , NL: , AT: , GR: , IE: 42/17, NI: 42/17," +
								 " PL: 42/17, FI: 42/17, CZ: 42/17, SE: , SK: 42/17, HU: 42/17, DK: , HR: , SI: , CH: , CY: ," +
								 " BG: 42/17, RO: 42/17, LT: 42/17, US: , OSDE: , OSBE: , OSNL: , OSCZ: , OSES: , OSGB: , OSFR:" +
								 " , RS: \";\"DE: , , FR: , , IT: , , ES: , , GB: , , BE: , , PT: , , NL: , , AT: , , GR: , ," +
								 " IE: , , NI: , , PL: , , FI: , , CZ: , , SE: , , SK: , , HU: , , DK: , , HR: , , SI: , , CH:" +
								 " , , CY: , , BG: , , RO: , , LT: , , US: , , OSDE: , , OSBE: , , OSNL: , , OSCZ: , , OSES: ," +
								 " , OSGB: , , OSFR: , , RS: , \";\"AM\";\"45.2 - X-MAS Küche (Food Expo Deluxe 1)\";" +
								 ";;\" \";\"ohne Style Ilex, Referenz US 291387\";;;\"LGA\";\"TASKOM\";;;" +
								 "\"42/17\";\"45/17\";;;\"FSC-Mix\";;\" \";;\"32\";;;" +
								 "\"BE, IE, NI, PL, FI, CZ, SK, HU, BG, RO, LT\";" +
								 "\"Chantal(Sortierung),Sammy(Sortierung),neu(Sortierung),Goldstern(Sortierung)\";" +
								 "\"Chantal,Sammy,neu,Goldstern\";\"288362_A,288362_B,288362_C,288362_D\";" +
								 "\"CB1,CB2,CB5,CB6,CB7,CB3,CB4,CB9,CB8\";\"45/17\";"; 

	@Test
	public void testHeader() throws Exception {
		final FinalizableConsumer<CsvRow> consumer = new FinalizableConsumer<CsvRow>() {
			String[] headers;

			@Override
			public void accept(CsvRow pRow) {
				if (headers == null) {
					headers = pRow.getHeaderArray();
				}
				Tests.assertArrayEquals(headers, "Stammdaten", "Int.-Artikelnummer", "LT plan", "Land", "Artikelstatus",
						"Artikelbezeichnung", "Kontraktbezeichnung", "Einkäufer", "Warenbereich", "Artikeltyp", "Warengruppe",
						"ThemaNr + Themenbezeichnung", "Markenart", "Marke", "EAN", "Gebindeanzahl", "Gebindeinhalt",
						"Gebindeeinheit", "Verpackung", "Kolliinhalt", "Bestandteile", "BA/SK/PA-Zugehörigkeit", "Vorgänger",
						"LT Land", "Länderbemerkung", "Artikelart", "Thema AM", "Referenzvorgänger",
						"Displaytyp", "Wettbewerber 1", "EK Bemerkung", "Topseller",
						"Thema INT", "Prüfinstitut", "Verpackungsagentur", "Sicherheitsdatenblatt", "Anlage Batterie",
						"Liefertermin Thema", "Werbetermin Thema", "Positionierung", "Andere Test Kriterien",
						"Zertifzierungen/Eigenschaften", "Logos auf Verpackung", "Wettbewerber 2", "Vorgänger 2",
						"Kollinhalt (KI)", "WT Land real", "Frühester WT Land real", "Bestellländer", "Größen", "Farben", "Bildinfos",
						"Sortierungscluster", "WT AM Land", "Abteilung");
			}

			@Override
			public void finished() {
				Assert.assertNotNull(headers);
			}
		};
		run(consumer);
	}

	@Test
	public void testParser() throws Exception {
		final CsvParser parser = new CsvParser("\r\n", "\"", ";");
		final String[] array = parser.asArray(LINE1, 0);
		Assert.assertEquals(55, array.length);
		Assert.assertEquals("Stammdaten", array[0]);
		Assert.assertEquals("", array[54]);
		final FinalizableConsumer<CsvRow> consumer = new FinalizableConsumer<CsvRow>() {
			String[] row;

			@Override
			public void accept(CsvRow pValue) {
				if (row == null) {
					row = pValue.getRowAsArray();
				}
			}

			@Override
			public void finished() {
				Assert.assertNotNull(row);
				Tests.assertArrayEquals(row,
                        "Stammdaten", "288362", "42/17", "IE", "verwendbar",
                        "Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC",
                        "Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC 4 fach sort.",
                        "MW", "NonFood", "Projektartikel_NF", "115.108 Küchenhelfer",
                        "45.2 X-MAS Küche (Food Expo Deluxe 1)",
                        "",
                        "",
                        "",
                        "1",
                        "1",
                        "Stk.",
                        "PK",
                        "",
                        "",
                        "",
                        "279620",
                        "DE: , FR: , IT: , ES: , GB: , BE: 42/17, PT: , NL: , AT: , GR: , IE: 42/17, NI: 42/17, PL: 42/17, FI: 42/17, CZ: 42/17, SE: , SK: 42/17, HU: 42/17, DK: , HR: , SI: , CH: , CY: , BG: 42/17, RO: 42/17, LT: 42/17, US: , OSDE: , OSBE: , OSNL: , OSCZ: , OSES: , OSGB: , OSFR: , RS: ",
                        "DE: , , FR: , , IT: , , ES: , , GB: , , BE: , , PT: , , NL: , , AT: , , GR: , , IE: , , NI: , , PL: , , FI: , , CZ: , , SE: , , SK: , , HU: , , DK: , , HR: , , SI: , , CH: , , CY: , , BG: , , RO: , , LT: , , US: , , OSDE: , , OSBE: , , OSNL: , , OSCZ: , , OSES: , , OSGB: , , OSFR: , , RS: , ",
                        "AM",
                        "45.2 - X-MAS Küche (Food Expo Deluxe 1)",
                        "",
                        "",
                        " ",
                        "ohne Style Ilex, Referenz US 291387",
                        "",
                        "",
                        "LGA",
                        "TASKOM",
                        "",
                        "",
                        "42/17",
                        "45/17",
                        "",
                        "",
                        "FSC-Mix",
                        "",
                        " ",
                        "",
                        "32",
                        "",
                        "",
                        "BE, IE, NI, PL, FI, CZ, SK, HU, BG, RO, LT",
                        "Chantal(Sortierung),Sammy(Sortierung),neu(Sortierung),Goldstern(Sortierung)",
                        "Chantal,Sammy,neu,Goldstern",
                        "288362_A,288362_B,288362_C,288362_D",
                        "CB1,CB2,CB5,CB6,CB7,CB3,CB4,CB9,CB8",
                        "45/17",
                        "");

			}
		};
		run(consumer);
	}

	@Test
	public void testCountLines() throws Exception {
		final FinalizableConsumer<CsvRow> consumer = new FinalizableConsumer<CsvRow>() {
			int numLines = 0;
			public void accept(CsvRow pRow) {
				++numLines;
			}
			@Override
			public void finished() {
				Assert.assertEquals(39, numLines);
			}
		};
		run(consumer);
	}

	@Test
	public void testFirstRow() throws Exception {
		final FinalizableConsumer<CsvRow> consumer = new FinalizableConsumer<CsvRow>() {
			private boolean done;

			@Override
			public void accept(CsvRow pRow) {
				if (!done) { 
					Tests.assertMapEquals(pRow.getRowAsMap(),
				              "Stammdaten", "Stammdaten",
				              "Int.-Artikelnummer", "288362",
				              "LT plan", "42/17",
				              "Land", "IE",
				              "Artikelstatus", "verwendbar",
				              "Artikelbezeichnung", "Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC",
				              "Kontraktbezeichnung", "Weihnachtstischsets Papier in Stoffoptik 12er 30 x 40 cm FSC 4 fach sort.",
				              "Einkäufer", "MW",
				              "Warenbereich", "NonFood",
				              "Artikeltyp", "Projektartikel_NF",
				              "Warengruppe", "115.108 Küchenhelfer",
				              "ThemaNr + Themenbezeichnung", "45.2 X-MAS Küche (Food Expo Deluxe 1)",
				              "Markenart", "",
				              "Marke", "",
				              "EAN", "",
				              "Gebindeanzahl", "1",
				              "Gebindeinhalt", "1",
				              "Gebindeeinheit", "Stk.",
				              "Verpackung", "PK",
				              "Kolliinhalt", "",
				              "Bestandteile", "",
				              "BA/SK/PA-Zugehörigkeit", "",
				              "Vorgänger", "279620",
				              "LT Land", "DE: , FR: , IT: , ES: , GB: , BE: 42/17, PT: , NL: , AT: , GR: , IE: 42/17, NI: 42/17, PL: 42/17, FI: 42/17, CZ: 42/17, SE: , SK: 42/17, HU: 42/17, DK: , HR: , SI: , CH: , CY: , BG: 42/17, RO: 42/17, LT: 42/17, US: , OSDE: , OSBE: , OSNL: , OSCZ: , OSES: , OSGB: , OSFR: , RS: ",
				              "Länderbemerkung", "DE: , , FR: , , IT: , , ES: , , GB: , , BE: , , PT: , , NL: , , AT: , , GR: , , IE: , , NI: , , PL: , , FI: , , CZ: , , SE: , , SK: , , HU: , , DK: , , HR: , , SI: , , CH: , , CY: , , BG: , , RO: , , LT: , , US: , , OSDE: , , OSBE: , , OSNL: , , OSCZ: , , OSES: , , OSGB: , , OSFR: , , RS: , ",
				              "Artikelart", "AM",
				              "Thema AM", "45.2 - X-MAS Küche (Food Expo Deluxe 1)",
				              "Referenzvorgänger", "",
				              "Displaytyp", "",
				              "Wettbewerber 1", " ",
				              "EK Bemerkung", "ohne Style Ilex, Referenz US 291387",
				              "Topseller", "",
				              "Thema INT", "",
				              "Prüfinstitut", "LGA",
				              "Verpackungsagentur", "TASKOM",
				              "Sicherheitsdatenblatt", "",
				              "Anlage Batterie", "",
				              "Liefertermin Thema", "42/17",
				              "Werbetermin Thema", "45/17",
				              "Positionierung", "",
				              "Andere Test Kriterien", "",
				              "Zertifzierungen/Eigenschaften", "FSC-Mix",
				              "Logos auf Verpackung", "",
				              "Wettbewerber 2", " ",
				              "Vorgänger 2", "",
				              "Kollinhalt (KI)", "32",
				              "WT Land real", "",
				              "Frühester WT Land real", "",
				              "Bestellländer", "BE, IE, NI, PL, FI, CZ, SK, HU, BG, RO, LT",
				              "Größen", "Chantal(Sortierung),Sammy(Sortierung),neu(Sortierung),Goldstern(Sortierung)",
				              "Farben", "Chantal,Sammy,neu,Goldstern",
				              "Bildinfos", "288362_A,288362_B,288362_C,288362_D",
				              "Sortierungscluster", "CB1,CB2,CB5,CB6,CB7,CB3,CB4,CB9,CB8",
				              "WT AM Land", "45/17",
				              "Abteilung", ""
				              );
					done = true;
				}
			}

			@Override
			public void finished() {
				Assert.assertTrue(done);
			}
			
		};
		run(consumer);
	}
	
	private void run(Consumer<CsvRow> pConsumer) throws IOException {
		final File inputFile = requireTestFile();
		try (InputStream in = new FileInputStream(inputFile)) {
			new CsvReader().parse(in, pConsumer);
		}
		if (pConsumer instanceof FinalizableConsumer) {
			final FinalizableConsumer<CsvRow> consumer = (FinalizableConsumer<CsvRow>) pConsumer;
			consumer.finished();
		}
	}

	private File requireTestFile() {
		final File inputFile = Tests.requireFileResource("com/github/jochenw/afw/core/csv/Lago-Input.csv");
		return inputFile;
	}
}
