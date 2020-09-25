package XMLParser;

public class Movie {
    private String id;
    private String title;
    private int releaseYear;
    private String genre;
    private String directorName;

    public Movie(){

    }

    public Movie(String id, String title, int releaseYear, String genre, String directorName) {
        this.id  = id;
        this.title = title;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.directorName = directorName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getReleaseyear() {
        return releaseYear;
    }

    public void setReleaseyear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirectorname() {
        return directorName;
    }

    public void setDirectorname(String directorName) {
        this.directorName = directorName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        /*sb.append("Movie Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("ReleaseYear:" + getReleaseyear());
        sb.append(", ");
        sb.append("Genre: " + getGenre());
        sb.append(", ");
        sb.append("Director:" + getDirectorname());
        sb.append(".");*/
        sb.append(getId());
        sb.append(",");
        sb.append(getGenre());
        sb.append(",");
        sb.append(getDirectorname());
        sb.append(",");
        sb.append(getReleaseyear());
        sb.append(",");
        sb.append(getTitle());
        sb.append("\n");

        return sb.toString();
    }
}
