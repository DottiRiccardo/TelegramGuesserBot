import java.util.ArrayList;
import java.util.List;

public class LOLChamp {

    public String name;

    public String gender;
    public List<String> species;
    public List<String> regions;
    public List<String> positions;
    public List<String> classes;
    public String rangeType;
    public String resource;
    public int releaseYear;

    public LOLChamp(String name) {
        this.name = name;
        this.species = new ArrayList<>();
        this.regions = new ArrayList<>();
        this.positions = new ArrayList<>();
        this.classes = new ArrayList<>();
    }

    public LOLChamp(String name, String gender, String resource, String rangeType, int releaseYear,
                    List<String> positions, List<String> classes, List<String> species, List<String> regions) {
        this.name = name;
        this.gender = gender;
        this.resource = resource;
        this.rangeType = rangeType;
        this.releaseYear = releaseYear;
        this.positions = positions;
        this.classes = classes;
        this.species = species;
        this.regions = regions;
    }

    // Override toString for better readability
    @Override
    public String toString() {
        return "LOLChamp{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", resource='" + resource + '\'' +
                ", rangeType='" + rangeType + '\'' +
                ", releaseYear=" + releaseYear +
                ", positions=" + positions +
                ", classes=" + classes +
                ", species=" + species +
                ", regions=" + regions +
                '}';
    }
}
