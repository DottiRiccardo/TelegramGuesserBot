import java.sql.*;
import java.util.*;

public class DB {
    private Connection conn;

    public DB(String db, String user, String pass) {
        String address = "jdbc:mysql://localhost:3306/" + db;
        try {
            conn = DriverManager.getConnection(address, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // CHAMP


    public void insertChamp(LOLChamp champ) {
        if (isChampExists(champ.name)) {
            System.out.println("Champ " + champ.name + " già inserito");
            return;
        }

        String insertChampQuery = "INSERT INTO LOLChamp (Name, Gender, Resource, RangeType, ReleaseYear) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertChampQuery)) {
            stmt.setString(1, champ.name);
            stmt.setString(2, champ.gender);
            stmt.setString(3, champ.resource);
            stmt.setString(4, champ.rangeType);
            stmt.setInt(5, champ.releaseYear);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        insertAssociations(champ);
    }

    private boolean isChampExists(String name) {
        String checkChampQuery = "SELECT 1 FROM LOLChamp WHERE Name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkChampQuery)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Restituisce true se esiste almeno un record
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insertAssociations(LOLChamp champ) {
        insertPosition(champ.name, champ.positions);
        insertClass(champ.name, champ.classes);
        insertSpecies(champ.name, champ.species);
        insertRegion(champ.name, champ.regions);
    }

    private void insertPosition(String champName, List<String> positions) {
        String insertPositionQuery = "INSERT IGNORE INTO Assoc (ChampName, Type, Name) VALUES (?, 'Position', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertPositionQuery)) {
            for (String position : positions) {
                stmt.setString(1, champName);
                stmt.setString(2, position);
                stmt.addBatch();
            }
            stmt.executeBatch(); // Esegui tutto insieme
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertClass(String champName, List<String> classes) {
        String insertClassQuery = "INSERT IGNORE INTO Assoc (ChampName, Type, Name) VALUES (?, 'Class', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertClassQuery)) {
            for (String className : classes) {
                stmt.setString(1, champName);
                stmt.setString(2, className);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertSpecies(String champName, List<String> species) {
        String insertSpeciesQuery = "INSERT IGNORE INTO Assoc (ChampName, Type, Name) VALUES (?, 'Species', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSpeciesQuery)) {
            for (String specie : species) {
                stmt.setString(1, champName);
                stmt.setString(2, specie);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertRegion(String champName, List<String> regions) {
        String insertRegionQuery = "INSERT IGNORE INTO Assoc (ChampName, Type, Name) VALUES (?, 'Region', ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertRegionQuery)) {
            for (String region : regions) {
                stmt.setString(1, champName);
                stmt.setString(2, region);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LOLChamp getChampByName(String name) {
        LOLChamp champ = null;
        name = name.contains("&") ? name.split("&")[0].trim() : name;

        String selectChampQuery = "SELECT * FROM LOLChamp WHERE Name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectChampQuery)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    champ = new LOLChamp(name);
                    champ.gender = rs.getString("Gender");
                    champ.resource = rs.getString("Resource");
                    champ.rangeType = rs.getString("RangeType");
                    champ.releaseYear = rs.getInt("ReleaseYear");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (champ != null) {
            champ.positions = getAssociations(champ.name, "Position");
            champ.classes = getAssociations(champ.name, "Class");
            champ.species = getAssociations(champ.name, "Species");
            champ.regions = getAssociations(champ.name, "Region");
        }

        return champ;
    }

    private List<String> getAssociations(String champName, String type) {
        List<String> associations = new ArrayList<>();
        String selectAssocQuery = "SELECT Name FROM Assoc WHERE ChampName = ? AND Type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(selectAssocQuery)) {
            stmt.setString(1, champName);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    associations.add(rs.getString("Name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return associations;
    }


    // LIMITAZIONI


    public List<String> getChampNamesByCases(List<String> cases) {
        List<String> champNames = new ArrayList<>();

        StringBuilder query = new StringBuilder("SELECT Name FROM LOLChamp");
        if (!cases.isEmpty()) {
            query.append(" WHERE ");
            query.append(String.join(" AND ", cases));
        }

        try (PreparedStatement stmt = conn.prepareStatement(query.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                champNames.add(rs.getString("Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return champNames;
    }

    public Map<String, List<String>> getPossibleValues() {
        Map<String, List<String>> possibleValues = new HashMap<>();

        // Query per LOLChamp
        String lolChampQuery = "SELECT DISTINCT Gender, Resource, RangeType, ReleaseYear FROM LOLChamp";
        try (PreparedStatement stmt = conn.prepareStatement(lolChampQuery);
             ResultSet rs = stmt.executeQuery()) {

            // Inizializza le liste
            List<String> genders = new ArrayList<>();
            List<String> resources = new ArrayList<>();
            List<String> rangeTypes = new ArrayList<>();
            List<String> releaseYears = new ArrayList<>();

            // Popola le liste
            while (rs.next()) {
                genders.add(rs.getString("Gender"));
                resources.add(rs.getString("Resource"));
                rangeTypes.add(rs.getString("RangeType"));
                releaseYears.add(String.valueOf(rs.getInt("ReleaseYear")));
            }

            // Aggiunge i valori alla mappa
            possibleValues.put("Gender", genders);
            possibleValues.put("Resource", resources);
            possibleValues.put("RangeType", rangeTypes);
            possibleValues.put("ReleaseYear", releaseYears);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Query per Assoc
        String assocQuery = "SELECT DISTINCT Type, Name FROM Assoc";
        try (PreparedStatement stmt = conn.prepareStatement(assocQuery);
             ResultSet rs = stmt.executeQuery()) {

            Map<String, List<String>> assocValues = new HashMap<>();

            while (rs.next()) {
                String type = rs.getString("Type");
                String name = rs.getString("Name");
                assocValues.putIfAbsent(type, new ArrayList<>());
                assocValues.get(type).add(name);
            }

            // Aggiunge i valori alla mappa
            for (Map.Entry<String, List<String>> entry : assocValues.entrySet()) {
                possibleValues.put(entry.getKey(), entry.getValue());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return possibleValues;
    }


    // PLAYER


    public void insertPlayer(long chat_id, String username) {
        String query = "INSERT INTO Player (Chat_id, Username) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, chat_id); // Imposta il chat_id
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per controllare se un chat_id esiste nel database
    public boolean checkChatId(long chat_id) {
        String query = "SELECT 1 FROM Player WHERE Chat_id = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, chat_id);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Se il risultato è presente, il chat_id esiste
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM players WHERE username = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // Funzione per ottenere il username dato un chat_id
    public String getUsername(long chat_id) {
        String query = "SELECT Username FROM Player WHERE Chat_id = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setLong(1, chat_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Funzione per aggiornare le statistiche del giocatore
    public void updateStats(long chat_id, int champFound, float averageTry) {
        String query = "UPDATE Player SET ChampFound = ?, AverageTry = ? WHERE Chat_id = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, champFound);
            stmt.setFloat(2, averageTry);
            stmt.setLong(3, chat_id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funzione per selezionare un giocatore
    public PlayerStats getStats(long chat_id) {
        String query = "SELECT Username, ChampFound, AverageTry FROM Player WHERE Chat_id = ?";
        PlayerStats stats = null;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, chat_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Crea un oggetto PlayerStats con le informazioni recuperate
                    stats = new PlayerStats();
                    stats.ChampFound = rs.getInt("ChampFound");
                    stats.AverageTry = rs.getFloat("AverageTry");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    public List<String> getTopPlayers(int n) {
        String query = "SELECT Username " +
                "FROM Player " +
                "WHERE ChampFound >= 10 " +
                "ORDER BY AverageTry ASC " +
                "LIMIT ?";
        List<String> topPlayers = new ArrayList<>();

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, n);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topPlayers.add(rs.getString("Username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topPlayers;
    }
}
