import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public LOLChamp getLOLChampByName(String name) {
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




    // 1. Inserisci un campione completo
    public void insertChampion(LOLChamp champ) {

        try {
            String insertChampQuery = "INSERT INTO LOLChamp (Name, Gender, Resource, RangeType, ReleaseYear) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement champStmt = conn.prepareStatement(insertChampQuery)) {
                champStmt.setString(1, champ.name);
                champStmt.setString(2, champ.gender);
                champStmt.setString(3, champ.resource);
                champStmt.setString(4, champ.rangeType);
                champStmt.setInt(5, champ.releaseYear);
                champStmt.executeUpdate();
            }

            // AssSpecies
            String insertSpeciesQuery = "INSERT INTO AssSpecies (ChampName, SpeciesName) VALUES (?, ?)";
            try (PreparedStatement speciesStmt = conn.prepareStatement(insertSpeciesQuery)) {
                for (String species : champ.species) {
                    speciesStmt.setString(1, champ.name);
                    speciesStmt.setString(2, species);
                    speciesStmt.addBatch();
                }
                speciesStmt.executeBatch();
            }

            // AssRegion
            String insertRegionsQuery = "INSERT INTO AssRegion (ChampName, RegionName) VALUES (?, ?)";
            try (PreparedStatement regionStmt = conn.prepareStatement(insertRegionsQuery)) {
                for (String region : champ.regions) {
                    regionStmt.setString(1, champ.name);
                    regionStmt.setString(2, region);
                    regionStmt.addBatch();
                }
                regionStmt.executeBatch();
            }

            // AssPosition
            String insertPositionsQuery = "INSERT INTO AssPosition (ChampName, PositionName) VALUES (?, ?)";
            try (PreparedStatement positionStmt = conn.prepareStatement(insertPositionsQuery)) {
                for (String position : champ.positions) {
                    positionStmt.setString(1, champ.name);
                    positionStmt.setString(2, position);
                    positionStmt.addBatch();
                }
                positionStmt.executeBatch();
            }

            // AssClass
            String insertClassesQuery = "INSERT INTO AssClass (ChampName, ClassName) VALUES (?, ?)";
            try (PreparedStatement classStmt = conn.prepareStatement(insertClassesQuery)) {
                for (String clazz : champ.classes) {
                    classStmt.setString(1, champ.name);
                    classStmt.setString(2, clazz);
                    classStmt.addBatch();
                }
                classStmt.executeBatch();
            }

        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback in case of error
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }
    }

    private void insertIfNotExists(String tableName, String value) throws SQLException {
        String query = "INSERT IGNORE INTO " + tableName + " (Name) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, value);
        stmt.executeUpdate();
    }

    private void insertAssociation(String tableName, String champName, String associatedName) throws SQLException {
        String query = "INSERT IGNORE INTO " + tableName + " (ChampName, " + tableName.replace("Ass", "") + "Name) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, champName);
        stmt.setString(2, associatedName);
        stmt.executeUpdate();
    }

    // 2. Seleziona un campione con tutte le informazioni (incluse associazioni)
    public void selectChampionByName(String champName) {
        String query = """
        SELECT 
            c.Name, 
            c.Gender, 
            c.Resource, 
            c.RangeType, 
            c.ReleaseYear,
            GROUP_CONCAT(DISTINCT p.Name) AS Positions,
            GROUP_CONCAT(DISTINCT cl.Name) AS Classes,
            GROUP_CONCAT(DISTINCT s.Name) AS Species,
            GROUP_CONCAT(DISTINCT r.Name) AS Regions
        FROM 
            LOLChamp c
        LEFT JOIN AssPosition ap ON c.Name = ap.ChampName
        LEFT JOIN Position p ON ap.PositionName = p.Name
        LEFT JOIN AssClass ac ON c.Name = ac.ChampName
        LEFT JOIN Class cl ON ac.ClassName = cl.Name
        LEFT JOIN AssSpecies aspec ON c.Name = aspec.ChampName
        LEFT JOIN Species s ON aspec.SpeciesName = s.Name
        LEFT JOIN AssRegion ar ON c.Name = ar.ChampName
        LEFT JOIN Region r ON ar.RegionName = r.Name
        WHERE 
            c.Name = ?
        GROUP BY 
            c.Name, c.Gender, c.Resource, c.RangeType, c.ReleaseYear
    """;

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, champName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Name: " + rs.getString("Name"));
                System.out.println("Gender: " + rs.getString("Gender"));
                System.out.println("Resource: " + rs.getString("Resource"));
                System.out.println("RangeType: " + rs.getString("RangeType"));
                System.out.println("ReleaseYear: " + rs.getInt("ReleaseYear"));
                System.out.println("Positions: " + rs.getString("Positions"));
                System.out.println("Classes: " + rs.getString("Classes"));
                System.out.println("Species: " + rs.getString("Species"));
                System.out.println("Regions: " + rs.getString("Regions"));
            } else {
                System.out.println("Champion not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Inserisci un player con nome e password
    public void insertPlayer(String username, String password) {
        String query = "INSERT INTO Player (Username, Password) VALUES (?, ?)";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. Aggiorna ChampFound e AverageTry di un player
    public void updatePlayerStats(String username, int champFound, float averageTry) {
        String query = "UPDATE Player SET ChampFound = ?, AverageTry = ? WHERE Username = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, champFound);
            stmt.setFloat(2, averageTry);
            stmt.setString(3, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5. Seleziona solo la password di un player
    public String getPlayerPassword(String username) {
        String query = "SELECT Password FROM Player WHERE Username = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 6. Seleziona tutti i dettagli di un player
    public void selectPlayer(String username) {
        String query = "SELECT * FROM Player WHERE Username = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                System.out.println("Username: " + rs.getString("Username"));
                System.out.println("Password: " + rs.getString("Password"));
                System.out.println("ChampFound: " + rs.getInt("ChampFound"));
                System.out.println("AverageTry: " + rs.getFloat("AverageTry"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
