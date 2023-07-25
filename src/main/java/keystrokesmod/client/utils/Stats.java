package keystrokesmod.client.utils;

public class Stats {
    boolean nicked;
    int level;
    int uhcKills;
    double uhcKD;

    public Stats(boolean nicked){
        this.nicked = nicked;
    }
    public Stats(boolean nicked, int level, int uhcKills, double uhcKD){
        this.nicked = nicked;
        this.level = level;
        this.uhcKills = uhcKills;
        this.uhcKD = uhcKD;

    }

    public boolean isNicked() {
        return nicked;
    }

    public int getLevel() {
        return level;
    }

    public int getUhcKills() {
        return uhcKills;
    }

    public double getUhcKD() {
        return uhcKD;
    }
}
