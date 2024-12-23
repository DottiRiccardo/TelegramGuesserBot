import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        DB db = new DB("tguesserbot", "root", "");
        Map<String, List<String>> possibleValues = db.getPossibleValues();

        printMap(possibleValues);
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
    public static void printMap(Map<String, List<String>> map) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            System.out.println(key + ":");
            for (String value : values) {
                System.out.println("  - " + value);
            }
        }
    }

}