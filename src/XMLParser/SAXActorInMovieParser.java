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

public class SAXActorInMovieParser extends DefaultHandler{
    List<ActorInMovie> myList;
    private String tempVal;
    private ActorInMovie tempAIM;

    public SAXActorInMovieParser() {myList = new ArrayList<ActorInMovie>();}

    public void runParser() throws ClassNotFoundException, FileNotFoundException, InstantiationException, IllegalAccessException {
        parseDocument();
        printData();
        long startTime = System.currentTimeMillis();
        batchInsert();
        long endTime = System.currentTimeMillis();
        System.out.println("That tooks " + (endTime - startTime) + " milliseconds.");
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("src/XMLParser/stanford-movies/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() {

        System.out.println("Number of Stars in Movies combination '" + myList.size() + "'.");

        Iterator<ActorInMovie> it = myList.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    private void batchInsert() throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException {
        Connection conn = null;

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser", "mypassword");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement psInsertRecord=null;
        String sqlInsertRecord=null;

        int[] iNoRows=null;
        int id_num = 0;
        ActorInMovie temp = null;
        int temp_id = 0;
        PrintWriter out = new PrintWriter("AIM_inconsistency.txt");
        System.out.println("Begin getting the max id..");
        sqlInsertRecord="insert into stars_in_movies (starId, movieId) " +
                "select * from (select ?, ?) as tmp" +
                " where not exists (select starId, movieId from stars_in_movies where starId = ? and movieId = ?) Limit 1;";
        try {

            conn.setAutoCommit(false);

            psInsertRecord=conn.prepareStatement(sqlInsertRecord);

            System.out.println("Begin adding to stars_in_movies db...");
            Iterator<ActorInMovie> it = myList.iterator();
            while (it.hasNext())
            {
                ActorInMovie cur = it.next();
                temp = cur;
                String final_starid = "";
                String query = "select id from stars where name = ?;";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, cur.getStarName());
                ResultSet rs1 = statement.executeQuery();
                if (rs1.next()) {
                    final_starid = rs1.getString(1);

                }
                rs1.close();
                statement.close();

                String final_movieid = cur.getfilmId();
                psInsertRecord.setString(1, final_starid);
                psInsertRecord.setString(2, final_movieid);
                psInsertRecord.setString(3, final_starid);
                psInsertRecord.setString(4, final_movieid);

                psInsertRecord.addBatch();

            }

            iNoRows=psInsertRecord.executeBatch();
            conn.commit();
            System.out.println("Done");

        } catch (SQLException e) {
            out.println(e); //write the error into AIM_inconsistency.txt
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
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of actor
            tempAIM = new ActorInMovie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("f")) {
            tempAIM.setfilmId(tempVal);
        } else if (qName.equalsIgnoreCase("a")) {
            tempAIM.setStarName(tempVal);
            myList.add(tempAIM);
        }

    }

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, InstantiationException, IllegalAccessException {
        SAXActorInMovieParser saim = new SAXActorInMovieParser();
        saim.runParser();
    }
}
