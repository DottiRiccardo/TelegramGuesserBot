public class Game {
    String randomName;
    private int nTry;

    public Game () {
        randomName = null;
        nTry = 0;
    }

    public Game (String randomName) {
        this.randomName = randomName;
        nTry = 0;
    }

    public void incriseNTry(){
        nTry++;
    }

    public int getNTry() {
        return nTry;
    }
}
