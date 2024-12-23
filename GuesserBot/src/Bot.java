import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private static DB db;
    private Map<Long, List<String>> limitations;
    private Map<Long, Game> onGames;
    private static final long ADMIN_CHAT_ID = 1030344509;

    public Bot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        db = new DB("tguesserbot", "root", "");
        limitations = new HashMap<>();
        onGames = new HashMap<>();
    }

    @Override
    public void consume(Update update) {
        if (update.getMessage() == null) {
            return;
        }
        long chat_id = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (!db.checkChatId(chat_id) && !messageText.startsWith("/start") && !messageText.startsWith("/username")) {
                sendMessage(chat_id, "You must set a username first using /username [name].");
                return;
            }

            if (onGames.containsKey(chat_id) && !(messageText.startsWith("/try") || messageText.startsWith("/list"))) {
                sendMessage(chat_id, "You can only use /try or /list during an active game.");
                return;
            }

            switch (messageText.split(" ")[0]) {
                case "/start":
                    start(chat_id);
                    break;
                case "/help":
                    help(chat_id);
                    break;
                case "/username":
                    username(chat_id, messageText);
                    break;
                case "/list":
                    list(chat_id);
                    break;
                case "/try":
                    tryChamp(chat_id, messageText);
                    break;
                case "/stats":
                    playerStats(chat_id);
                    break;
                case "/addlimitation":
                    addLimitation(chat_id, messageText);
                    break;
                case "/guidelimitation":
                    guideLimitation(chat_id, messageText);
                    break;
                case "/clearlimitations":
                    clearLimitations(chat_id);
                    break;
                case "/play":
                    play(chat_id);
                    break;
                case "/topplayer":
                    topPlayer(chat_id);
                    break;
                case "/adminUpdateChamp":
                    if (chat_id == ADMIN_CHAT_ID) {
                        adminUpdateChamp(chat_id);
                    } else {
                        sendMessage(chat_id, "This command is restricted to admins.");
                    }
                    break;
                default:
                    sendMessage(chat_id, "Unknown command. Type /help for a list of commands.");
                    break;
            }
        }
    }

    private void start(long chat_id) {
        if (db.checkChatId(chat_id)) {
            String storedUsername = db.getUsername(chat_id);
            sendMessage(chat_id, "Welcome back, " + storedUsername + "! Ready to play?");
        } else {
            sendMessage(chat_id, "Welcome! Please set your username using /username [name].");
        }
    }

    private void help(long chat_id) {
        String helpText = "Here are the available commands:\n" +
                "/start - Start the game\n" +
                "/help - Show this help message\n" +
                "/username [name] - Set your username\n" +
                "/list - Show a list of available champions\n" +
                "/try [champion name] - Try to guess a champion\n" +
                "/stats - Show your game statistics\n" +
                "/addlimitation [condition] - Add a limitation for champion listing\n" +
                "/clearlimitations - Clear all limitations for champion listing\n" +
                "/play - Start a new game\n" +
                "/topplayer - Show the top players\n" +
                "/admin [action] - Admin-only command.";
        sendMessage(chat_id, helpText);
    }

    private void username(long chat_id, String messageText) {
        String username = messageText.substring(10).trim();
        if (username.isEmpty()) {
            sendMessage(chat_id, "Please provide a valid username.");
            return;
        }

        if (db.usernameExists(username)) {
            sendMessage(chat_id, "The username \"" + username + "\" is already taken. Please choose another one.");
            return;
        }

        if (db.checkChatId(chat_id)) {
            String storedUsername = db.getUsername(chat_id);
            sendMessage(chat_id, "You are already logged in as: " + storedUsername);
        } else {
            db.insertPlayer(chat_id, username);
            sendMessage(chat_id, "Username accepted! Welcome, " + username + "!");
        }
    }

    private void list(long chat_id) {
        List<String> champions = limitations.get(chat_id).isEmpty() ? db.getChampNamesByCases(new ArrayList<>()) : db.getChampNamesByCases(limitations.get(chat_id));
        if (champions.isEmpty()) {
            sendMessage(chat_id, "No champions found with the current limitations.");
        } else {
            StringBuilder response = new StringBuilder("List of available champions:\n");
            for (String champion : champions) {
                response.append("- ").append(champion).append("\n");
            }
            response.append("\nTotal: ").append(champions.size()).append(" champions.");
            sendMessage(chat_id, response.toString());
        }
    }

    private void tryChamp(long chat_id, String messageText) {
        if (messageText.length() <= 5) {
            sendMessage(chat_id, "Please provide a valid champion name after the /try command.");
            return;
        }

        String guessedChampionName = messageText.substring(5).trim();

        if (!onGames.containsKey(chat_id)) {
            sendMessage(chat_id, "No game is active. Use /play to start a new game.");
            return;
        }

        String targetChampionName = onGames.get(chat_id).randomName;
        LOLChamp guessedChampion = db.getChampByName(guessedChampionName);
        LOLChamp targetChampion = db.getChampByName(targetChampionName);

        if (guessedChampion == null) {
            sendMessage(chat_id, "Champion \"" + guessedChampionName + "\" not found in the database.");
            return;
        }

        StringBuilder response = new StringBuilder("Comparison results:\n");
        response.append(compareField("Gender", guessedChampion.gender, targetChampion.gender));
        response.append(compareList("Species", guessedChampion.species, targetChampion.species));
        response.append(compareList("Regions", guessedChampion.regions, targetChampion.regions));
        response.append(compareList("Positions", guessedChampion.positions, targetChampion.positions));
        response.append(compareList("Classes", guessedChampion.classes, targetChampion.classes));
        response.append(compareField("Range Type", guessedChampion.rangeType, targetChampion.rangeType));
        response.append(compareField("Resource", guessedChampion.resource, targetChampion.resource));
        response.append(compareYear("Release Year", guessedChampion.releaseYear, targetChampion.releaseYear));

        onGames.get(chat_id).incriseNTry();

        if ((guessedChampion.toString()).equals(targetChampion.toString())) {
            response.append("\n\uD83C\uDF89 Congratulations! You guessed the champion correctly in your " + onGames.get(chat_id).getNTry() + " try!");

            System.out.println(limitations.get(chat_id).isEmpty());
            System.out.println(limitations.get(chat_id).toString());
            if (limitations.get(chat_id).isEmpty()) {
                PlayerStats ps = db.getStats(chat_id);
                float avTry = ((ps.AverageTry * ps.ChampFound) + onGames.get(chat_id).getNTry()) / (ps.ChampFound + 1);
                int nChamp = ps.ChampFound + 1;
                db.updateStats(chat_id, nChamp, avTry);
            } else {
                response.append("\nBecause of limitations the statistics were not updated");
            }

            // end game
            onGames.remove(chat_id);
            limitations.remove(chat_id);
        }

        sendMessage(chat_id, response.toString());
    }

    private String compareField(String fieldName, Object guessedValue, Object targetValue) {
        String emoji = guessedValue.equals(targetValue) ? "\uD83D\uDFE9" : "\uD83D\uDFE5";
        return emoji + " " + fieldName + ": " + guessedValue + "\n";
    }

    private String compareList(String fieldName, List<String> guessedList, List<String> targetList) {
        boolean isEqual = guessedList.containsAll(targetList) && targetList.containsAll(guessedList);
        boolean isPartial = !isEqual && guessedList.stream().anyMatch(targetList::contains);
        String emoji = isEqual ? "\uD83D\uDFE9" : isPartial ? "\uD83D\uDFE8" : "\uD83D\uDFE5";
        return emoji + " " + fieldName + ": " + guessedList + "\n";
    }

    private String compareYear(String fieldName, int guessedYear, int targetYear) {
        String emoji;
        if (guessedYear == targetYear) {
            emoji = "\uD83D\uDFE9";
        } else if (guessedYear < targetYear) {
            emoji = "\u2B06";
        } else {
            emoji = "\u2B07";
        }
        return emoji + " " + fieldName + ": " + guessedYear + "\n";
    }

    private void playerStats(long chat_id) {
        PlayerStats ps = db.getStats(chat_id);
        String Username = db.getUsername(chat_id);
        String stats = "Your stats:\n" +
                "Username: " + Username + "\n" +
                "Champions found: " + ps.ChampFound + "\n" +
                "Average tries per champion: " + ps.AverageTry;
        sendMessage(chat_id, stats);
    }

    private void addLimitation(long chat_id, String messageText) {
        String condition = messageText.substring(14).trim();
        if (condition.isEmpty()) {
            sendMessage(chat_id, "Please provide a valid condition.");
            return;
        }

        limitations.putIfAbsent(chat_id, new ArrayList<>());

        limitations.get(chat_id).add(condition);

        List<String> champions = db.getChampNamesByCases(limitations.get(chat_id));
        if (champions.size() > 1) {
            sendMessage(chat_id, "Limitation added: " + condition + "\nNote: Scores won't be counted if limitations are active.");
        } else {
            limitations.remove(condition);
            sendMessage(chat_id, "Limitation not added. It would leave fewer than 2 champions.");
        }
    }

    private void guideLimitation(long chat_id, String messageText) {
        String info = "Se vuoi impostare delle limitazioni segui questa guida:" +
                "impostare su quale argomento creare una limitazione:" +
                "   1) Gender" +
                "   2) Species" +
                "   3) Region" +
                "   4) Position" +
                "   5) Class" +
                "   6) Range Type" +
                "   7) Resource" +
                "   8) release Year";
        sendMessage(chat_id, info);
    }

    private void clearLimitations(long chat_id) {
        if (limitations.containsKey(chat_id)) {
            limitations.get(chat_id).clear();
            sendMessage(chat_id, "All limitations have been cleared.");
        } else {
            sendMessage(chat_id, "You have no active limitations to clear.");
        }
    }

    private void play(long chat_id) {
        onGames.put(chat_id, new Game(randomChamp(chat_id)));
        limitations.putIfAbsent(chat_id, new ArrayList<>());
        System.out.println(onGames.get(chat_id).randomName);
        sendMessage(chat_id, "Random Champion has been extracted. Use the command /try to try the game");
    }

    private String randomChamp(long chat_id) {
        List<String> champions;
        if (limitations.get(chat_id) == null || limitations.get(chat_id).isEmpty()) {
            champions = db.getChampNamesByCases(new ArrayList<>());
        } else {
            champions = db.getChampNamesByCases(limitations.get(chat_id));
        }
        Random random = new Random();
        return champions.get(random.nextInt(champions.size()));
    }

    private void topPlayer(long chat_id) {
        int n = 5;
        List<String> topPlayers = db.getTopPlayers(n);

        if (topPlayers.isEmpty()) {
            sendMessage(chat_id, "No top players found. Start playing to climb the leaderboard!");
        } else {
            StringBuilder response = new StringBuilder("Top players:\n");
            for (int i = 0; i < topPlayers.size(); i++) {
                response.append(i + 1).append(". ").append(topPlayers.get(i)).append("\n");
            }
            sendMessage(chat_id, response.toString());
        }
    }

    private void adminUpdateChamp(long chat_id) {
        Scraper scraper = new Scraper();

        List<String> champions = scraper.findAllChampions();
        System.out.println("Lista dei campioni trovati: " + champions);

        for (String champ : champions) {
            LOLChamp a = scraper.getChamp(champ);
            db.insertChamp(a);
        }

        sendMessage(chat_id, "Lista dei campioni aggiornata");
    }

    private void sendMessage(long chat_id, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chat_id)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
