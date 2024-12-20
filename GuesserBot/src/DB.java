import java.sql.*;

public class DB {
    private Connection conn;

    // Costruttore
    public DB(String db, String user, String pass) {
        String address = "jdbc:mysql://localhost:3306/" + db;
        try {
            conn = DriverManager.getConnection(address, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 1. Inserisci un campione completo
    public void insertChampion(String name, String gender, String resource, String rangeType, int releaseYear,
                               String position, String champClass, String species, String region) {
        try {
            // Inserimento campione nella tabella LOLChamp
            String champQuery = "INSERT IGNORE INTO LOLChamp (Name, Gender, Resource, RangeType, ReleaseYear) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement champStmt = conn.prepareStatement(champQuery);
            champStmt.setString(1, name);
            champStmt.setString(2, gender);
            champStmt.setString(3, resource);
            champStmt.setString(4, rangeType);
            champStmt.setInt(5, releaseYear);
            champStmt.executeUpdate();

            // Assicurarsi che le associazioni esistano
            insertIfNotExists("Position", position);
            insertIfNotExists("Class", champClass);
            insertIfNotExists("Species", species);
            insertIfNotExists("Region", region);

            // Inserire nelle tabelle di associazione
            insertAssociation("AssPosition", name, position);
            insertAssociation("AssClass", name, champClass);
            insertAssociation("AssSpecies", name, species);
            insertAssociation("AssRegion", name, region);
        } catch (SQLException e) {
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
