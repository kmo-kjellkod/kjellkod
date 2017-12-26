package doff.file;

public class LeakageMeasurementData {

    public String uuid = "";
    public String date = "datum";
    public String author = "förnamn efternamn";

    public String toString() {
        String s = "";
        s += date + "\n";
        s += author + "\n";
        return s;
    }

}
