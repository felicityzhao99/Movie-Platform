package XMLParser;

public class Actor {

    private String id;
    private String name;
    private int birthYear;

    public Actor(){

    }

    public Actor(String id, String name, int birthYear) {
        this.id  = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getBirth() {
        return birthYear;
    }

    public void setBirth(int birthYear) {
        this.birthYear = birthYear;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Actor Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("BirthYear:" + getBirth());
        sb.append(".");

        return sb.toString();
    }
}
