import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scraper {
    private final String mainWebUrl = "https://wiki.leagueoflegends.com/en-us/List_of_champions";
    private final String statsWebUrl = "https://wiki.leagueoflegends.com/en-us/";
    private final String loreWebUrl = "https://wiki.leagueoflegends.com/en-us/Universe:";

    public List<String> findAllChampions() {
        List<String> champions = new ArrayList<>();
        try {
            System.out.println("Connettendo alla pagina principale...");
            Document main = Jsoup.connect(mainWebUrl).get();
            System.out.println("Pagina caricata con successo.");

            // Selezionare tutti gli elementi con la classe specificata
            Elements champElements = main.select("td[style=text-align:left;] > span.inline-image > span[style=white-space:normal;] a");
            System.out.println("Numero di elementi trovati: " + champElements.size());

            for (Element champElement : champElements) {
                // Estrarre il testo del tag <a>
                String champName = champElement.textNodes().get(0).text();
                System.out.println("Nome campione trovato: " + champName);
                champions.add(champName);
            }
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della pagina: " + e.getMessage());
        }
        return champions;
    }

    public void test(String champName) {
        try {
            LOLChamp Champ = new LOLChamp(champName);

            Document stats = Jsoup.connect(statsWebUrl + champName).get();

            Elements rowsStats = stats.select("div.infobox-data-row");

            for (Element row : rowsStats) {
                Element label = row.selectFirst("div.infobox-data-label");
                Element value = row.selectFirst("div.infobox-data-value");

                if (label != null && value != null) {
                    switch (label.text()) {
                        case "Position(s)":
                            System.out.println("Position: " + value.text());
                            break;
                        case "Class(es)":
                            System.out.println("Class: " + value.text());
                            break;
                        case "Range type":
                            Champ.rangeType = value.text();
                            System.out.println("Range type: " + value.text());
                            break;
                        case "Resource":
                            System.out.println("Resource: " + value.text());
                            break;
                        case "Release date":
                            String releaseDate = value.text();
                            if (releaseDate.length() >= 4) {
                                int year = Integer.parseInt(releaseDate.substring(0, 4));
                                System.out.println("Release year: " + year);
                            } else {
                                System.out.println("Release year: Data not valid");
                            }
                            break;
                    }
                }
            }

            Document lore = Jsoup.connect(loreWebUrl + champName).get();

            Elements rowsLore = lore.select("div.infobox-data-row");

            for (Element row : rowsLore) {
                Element label = row.selectFirst("div.infobox-data-label");
                Element value = row.selectFirst("div.infobox-data-value");

                if (label != null && value != null) {
                    switch (label.text()) {
                        case "Pronoun(s)":
                            String pronouns = value.text();
                            if (pronouns.contains("He/Him") && pronouns.contains("She/Her")) {
                                System.out.println("Gender: Other");
                            } else if (pronouns.contains("He/Him")) {
                                System.out.println("Gender: Male");
                            } else if (pronouns.contains("She/Her")) {
                                System.out.println("Gender: Female");
                            } else {
                                System.out.println("Gender: Unknown");
                            }
                            break;
                        case "Species":
                            Elements species = value.select("li");
                            if (species.size() > 0) {
                                for (Element specie : species) {
                                    if (!specie.select("s").isEmpty()) {
                                        continue;
                                    } else {
                                        System.out.println("Species: " + specie.text());
                                    }
                                }
                            } else {
                                System.out.println("Species: " + value.text());
                            }
                            break;
                        case "Region(s)":
                            System.out.println("Region: " + value.text());
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della pagina: " + e.getMessage());
        }
    }
}
