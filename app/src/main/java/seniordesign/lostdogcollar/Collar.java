package seniordesign.lostdogcollar;

/**
 * Created by britne on 3/13/16.
 */
public class Collar {

    private int id;
    private String name;

    public Collar(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Collar)) {
            return false;
        }
        Collar other = (Collar) o;
        return name.equals(other.name) && id == other.id;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
