package XMLParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import static java.sql.Types.NULL;

public class SAXMovieParser extends DefaultHandler{
    List<Movie> myMovies;
    private String tempVal;
    private Movie tempMovie;
    boolean checkYear = false;
    boolean checkDir = false;
    boolean checkTitle = false;
    boolean checkId = false;
    boolean checkGenre = false;
    PrintWriter out = new PrintWriter("movie_inconsistency.txt");

    public SAXMovieParser() throws FileNotFoundException {myMovies = new ArrayList<Movie>();}

    public void runParser() throws FileNotFoundException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        parseDocument();
        printData();
        long startTime = System.currentTimeMillis();
        batchInsert();
        long endTime = System.currentTimeMillis();
        System.out.println("That tooks " + (endTime - startTime) + " milliseconds.");
        out.close();
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("src/XMLParser/stanford-movies/mains243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() throws FileNotFoundException {

        System.out.println("Number of Movies '" + myMovies.size() + "'.");
        PrintWriter out2 = new PrintWriter("movies.txt");
        Iterator<Movie> it = myMovies.iterator();
        while (it.hasNext()) {
            out2.println(it.next());
            System.out.println(it.next().toString());
        }
        out2.close();
    }

    private void batchInsert() throws InstantiationException, IllegalAccessException, ClassNotFoundException{
        Connection conn = null;

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser", "mypassword");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement psInsertRecord=null;
        PreparedStatement psInsertRecord2=null;
        PreparedStatement psInsertRecord3=null;
        String sqlInsertRecord=null;
        String sqlInsertRecord2=null;
        String sqlInsertRecord3=null;

        int[] iNoRows=null;
        int[] iNoRows2=null;
        int[] iNoRows3=null;
        int id_num = 0;
        Movie temp = null;
        String temp_id = "";
        String temp_title = "";
        boolean alarm = false;
        boolean alarm2 = false;

        System.out.println("Begin getting the max id..");
        sqlInsertRecord="insert into genres (name) " +
                "select * from (select ?) as tmp" +
                " where not exists (select name from genres where name = ?) Limit 1;";
        sqlInsertRecord2="insert into movies(id, title, year, director)" +
                "select * from (select ?, ?, ?, ?) as tmp" +
                " where not exists (select id, title, year, director from movies where" +
                " id = ? and title = ? and year = ? and director = ?) Limit 1;";
        sqlInsertRecord3="insert into genres_in_movies(genreId, movieId) " +
                "select * from (select ?, ?) as tmp" +
                " where not exists(select genreId, movieId from genres_in_movies where " +
                " genreId = ? and movieId = ?) Limit 1;";
        try {
            conn.setAutoCommit(false);

            psInsertRecord=conn.prepareStatement(sqlInsertRecord);
            psInsertRecord2=conn.prepareStatement(sqlInsertRecord2);
            psInsertRecord3=conn.prepareStatement(sqlInsertRecord3);

            System.out.println("Begin adding to genres and movies db...");
            Iterator<Movie> it = myMovies.iterator();
            while (it.hasNext())
            {
                Movie cur = it.next();
                temp = cur;

                String final_genreId = "";
                String query = "select id from genres where name = ?;";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, cur.getGenre());
                ResultSet rs1 = statement.executeQuery();
                if (rs1.next()) {
                    final_genreId = rs1.getString(1);

                }
                rs1.close();
                statement.close();

                String final_genre = cur.getGenre();
                psInsertRecord.setString(1, final_genre);
                psInsertRecord.setString(2, final_genre);

                String result_id = "tt" + cur.getId();

                if (!temp_id.equals(result_id) && !alarm) {

                    psInsertRecord2.setString(1, result_id);
                    temp_id = result_id;
                    if (result_id.equals("ttMiM10"))
                        alarm = true;
                }
                else {
                    out.println(temp);
                    continue;
                }

                String result_title = cur.getTitle();
                if (!temp_title.equals(result_title) && !alarm2) {
                    psInsertRecord2.setString(2, result_title);
                    temp_title = result_title;
                    if (result_title.equals("1984"))
                        alarm2 = true;
                }
                else {
                    out.println(temp);
                    continue;
                }
                psInsertRecord2.setInt(3, cur.getReleaseyear());
                psInsertRecord2.setString(4, cur.getDirectorname());
                psInsertRecord2.setString(5, result_id);
                psInsertRecord2.setString(6, cur.getTitle());
                psInsertRecord2.setInt(7, cur.getReleaseyear());
                psInsertRecord2.setString(8, cur.getDirectorname());

                psInsertRecord3.setString(1, final_genreId);
                psInsertRecord3.setString(2, result_id);
                psInsertRecord3.setString(3, final_genreId);
                psInsertRecord3.setString(4, result_id);

                psInsertRecord.addBatch();
                psInsertRecord2.addBatch();
                psInsertRecord3.addBatch();

            }

            iNoRows=psInsertRecord.executeBatch();
            iNoRows2=psInsertRecord2.executeBatch();
            iNoRows3=psInsertRecord3.executeBatch();
            conn.commit();
            System.out.println("Done");

        } catch (SQLException e) {
            out.println(e); //write the error into movie_inconsistency.txt
            e.printStackTrace();

        }
        out.close();
        try {
            if(psInsertRecord!=null) psInsertRecord.close();
            if(conn!=null) conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";

        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirn")) {
            if (!tempVal.equalsIgnoreCase("")) {
                checkDir = true;
                tempMovie.setDirectorname(tempVal);
            }
        } else if (qName.equalsIgnoreCase("fid")) {
            if (!tempVal.equals("")) {
                checkId = true;
                tempMovie.setId(tempVal);
            }
        } else if (qName.equalsIgnoreCase("t")) {
            if (!tempVal.equals("NKT")) {
                checkTitle = true;
                tempMovie.setTitle(tempVal);
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            if (!tempVal.equals("")){
                tempVal = tempVal.toLowerCase();
                tempVal = tempVal.replaceAll("\\s+", "");

                checkGenre = true;
                if (tempVal.equals("susp"))
                    tempVal = "Thriller";
                if (tempVal.equals("cnr"))
                    tempVal = "cops and robbers";
                if (tempVal.equals("dram") || tempVal.equals("draam"))
                    tempVal = "Drama";
                if (tempVal.equals("west") || tempVal.equals("west1"))
                    tempVal = "Western";
                if (tempVal.equals("myst"))
                    tempVal = "Mystery";
                if (tempVal.equals("s.f."))
                    tempVal = "Sci-Fi";
                if (tempVal.equals("advt"))
                    tempVal = "Adventure";
                if (tempVal.equals("horr") || tempVal.equals("hor"))
                    tempVal = "Horror";
                if (tempVal.equals("romt"))
                    tempVal = "Romance";
                if (tempVal.equals("comd"))
                    tempVal = "Comedy";
                if (tempVal.equals("musc"))
                    tempVal = "Musical";
                if (tempVal.equals("docu"))
                    tempVal = "Documentary";
                if (tempVal.equals("biop"))
                    tempVal = "Biography";
                if (tempVal.equals("tv"))
                    tempVal = "TV show";
                if (tempVal.equals("tvs"))
                    tempVal = "TV series";
                if (tempVal.equals("tvm"))
                    tempVal = "TV miniseries";
                if (tempVal.equals("porn"))
                    tempVal = "Pornography";
                if (tempVal.equals("noir"))
                    tempVal = "Black";
                if (tempVal.equals("crim"))
                    tempVal = "Crime";
                if (tempVal.equals("sports"))
                    tempVal = "Sport";
                tempMovie.setGenre(tempVal);
            }
            else {
                out.println("Genre empty error");
            }
        } else if (qName.equalsIgnoreCase("year")) {
            tempVal = tempVal.replaceAll("\\D+", "");
            if (!tempVal.equalsIgnoreCase("")) {
                checkYear = true;
                tempMovie.setReleaseyear(Integer.parseInt(tempVal));
            }
        } else if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            if (checkId && checkYear && checkDir && checkTitle && checkGenre)
                myMovies.add(tempMovie);
            checkId = false;
            checkYear = false;
            checkDir = false;
            checkTitle = false;
            checkGenre = false;
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        SAXMovieParser smp = new SAXMovieParser();
        smp.runParser();
    }
}
