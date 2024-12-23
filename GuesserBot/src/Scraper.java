import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scraper {
    private final String mainWebUrl = "https://wiki.leagueoflegends.com/en-us/List_of_champions";
    private final String statsWebUrl = "https://wiki.leagueoflegends.com/en-us/";
    private final String loreWebUrl = "https://wiki.leagueoflegends.com/en-us/Universe:";

    public List<String> findAllChampions() {
        List<String> champions = new ArrayList<>();
        try {
            Document main = Jsoup.connect(mainWebUrl).get();

            Elements champElements = main.select("td[style=text-align:left;] > span.inline-image > span[style=white-space:normal;] a");
            System.out.println("Numero di elementi trovati: " + champElements.size());

            for (Element champElement : champElements) {
                String champName = champElement.textNodes().get(0).text();
                champions.add(champName);
            }
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della pagina: " + e.getMessage());
        }
        return champions;
    }

    public LOLChamp getChamp(String champName) {
        try {
            LOLChamp Champ = new LOLChamp(champName);

            String urlName = champName.contains("&") ? champName.split("&")[0].trim() : champName;
            Document stats = Jsoup.connect(statsWebUrl + urlName).get();

            Elements rowsStats = stats.select("div.infobox-data-row");

            for (Element row : rowsStats) {
                Element label = row.selectFirst("div.infobox-data-label");
                Element value = row.selectFirst("div.infobox-data-value");

                if (label != null && value != null) {
                    switch (label.text()) {
                        case "Position(s)":
                            Champ.positions = Arrays.asList(value.text().split("\\s+")); // toglie le parentesi e il loro contenuto
                            break;
                        case "Legacy":
                            Champ.classes = Arrays.asList(value.text().split("\\s+"));
                            break;
                        case "Range type":
                            Champ.rangeType = value.text();
                            break;
                        case "Resource":
                            Champ.resource = value.text().replaceAll("\\(.*?\\)", "").trim();
                            break;
                        case "Release date":
                            String releaseDate = value.text();
                            if (releaseDate.length() >= 4) {
                                int year = Integer.parseInt(releaseDate.substring(0, 4));
                                Champ.releaseYear = year;
                            }
                            break;
                    }
                }
            }

            Document lore = Jsoup.connect(loreWebUrl + urlName).get();

            Elements rowsLore = lore.select("div.infobox-data-row");

            for (Element row : rowsLore) {
                Element label = row.selectFirst("div.infobox-data-label");
                Element value = row.selectFirst("div.infobox-data-value");

                if (label != null && value != null) {
                    switch (label.text()) {
                        case "Pronoun(s)":
                            String pronouns = value.text();
                            if (pronouns.contains("He/Him") && pronouns.contains("She/Her")) {
                                Champ.gender = "Other";
                            } else if (pronouns.contains("He/Him")) {
                                Champ.gender = "Male";
                            } else if (pronouns.contains("She/Her")) {
                                Champ.gender = "Female";
                            } else {
                                Champ.gender = "Other";
                            }
                            break;
                        case "Species":
                            Elements species = value.select("li");
                            if (species.size() > 0) {
                                for (Element specie : species) {
                                    if (!specie.select("s").isEmpty()) {
                                        continue;
                                    } else {
                                        Champ.species.add(specie.text().replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").trim());
                                    }
                                }
                            } else {
                                Champ.species.add(value.text().replaceAll("\\(.*?\\)", "").replaceAll("\\[.*?\\]", "").trim());
                            }
                            break;
                        case "Region(s)":
                            Champ.regions = Arrays.asList(value.text().split("\\s+"));
                            break;
                    }
                }
            }
            return Champ;
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della pagina: " + e.getMessage());
        }
        return new LOLChamp("");
    }
}
