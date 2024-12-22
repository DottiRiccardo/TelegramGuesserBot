import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Scraper scraper = new Scraper();
        DB db = new DB("tguesserbot", "root", "");

        List<String> champions = scraper.findAllChampions();
        System.out.println("Lista dei campioni trovati: " + champions);

        for (String champ : champions) {
            LOLChamp a = scraper.getChamp(champ);
            db.insertChamp(a);
        }

        /*
        String botToken = "7420686675:AAHhkvjQl8B3lZ-xYOLynY9AgPggFPcTQj4";
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new Bot(botToken));
            System.out.println("MyAmazingBot successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
    }
}