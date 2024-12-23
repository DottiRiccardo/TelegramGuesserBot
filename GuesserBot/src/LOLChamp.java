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

    @Override
    public String toString() {
        return "LOLChamp{" +
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
