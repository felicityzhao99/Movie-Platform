package XMLParser;

public class ActorInMovie {
    private String filmId;
    private String starName;

    public ActorInMovie() {

    }

    public ActorInMovie(String filmId, String starName) {
        this.filmId = filmId;
        this.starName = starName;
    }

    public String getfilmId() {return filmId;}
    public void setfilmId(String id) {this.filmId = id;}

    public String getStarName() {return starName;}
    public void setStarName(String name) {this.starName = name;}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Stars in Movies Details - ");
        sb.append("StarName:" + getStarName());
        sb.append(", ");
        sb.append("MovieId:" + getfilmId());
        sb.append(".");

        return sb.toString();
    }
}
