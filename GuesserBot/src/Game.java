public class Game {
    String randomName;
    private int nTry = 0;

    public Game (String randomName) {
        this.randomName = randomName;
    }

    public void incriseNTry(){
        nTry++;
    }

    public int getNTry() {
        return nTry;
    }
}
