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
//import sun.jvm.hotspot.types.basic.BasicTypeDataBase;

import static java.sql.Types.NULL;

public class SAXActorsParser extends DefaultHandler{
    List<Actor> myActors;
    private String tempVal;
    private Actor tempActor;
    private int tempId = 1;

    public SAXActorsParser() {myActors = new ArrayList<Actor>();}

    public void runParser() throws IllegalAccessException, ClassNotFoundException, InstantiationException, FileNotFoundException {
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
            sp.parse("src/XMLParser/stanford-movies/actors63.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private void printData() {

        System.out.println("Number of Actors '" + myActors.size() + "'.");

        Iterator<Actor> it = myActors.iterator();
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
        Actor temp = null;
        int temp_id = 0;
        PrintWriter out = new PrintWriter("actor_inconsistency.txt");
        System.out.println("Begin getting the max id..");
        sqlInsertRecord="insert into stars (id, name, birthYear) " +
                "select * from (select ?, ?, ?) as tmp" +
                " where not exists (select name, birthYear from stars where name = ? and birthYear = ?) Limit 1;";
        try {
            String query = "select max(id) from stars;";
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs1 = statement.executeQuery();
            if (rs1.next()) {
                String id = rs1.getString(1);
                id_num = Integer.valueOf(id.substring(2));

            }

            rs1.close();
            statement.close();
            //conn.close();
            conn.setAutoCommit(false);

            psInsertRecord=conn.prepareStatement(sqlInsertRecord);

            System.out.println("Begin adding to db...");
            Iterator<Actor> it = myActors.iterator();
            while (it.hasNext())
            {
                Actor cur = it.next();
                temp = cur;
                int index = Integer.parseInt(cur.getId());
                String final_id = "nm" + (id_num + index);
                String final_name = cur.getName();
                int final_birthYear = cur.getBirth();
                if (temp_id != index) {
                    psInsertRecord.setString(1, final_id);
                    temp_id = index;
                }
                else {
                    out.println(temp);
                    continue;
                }
                psInsertRecord.setString(2, final_name);
                if (final_birthYear == 0)
                    psInsertRecord.setNull(3, NULL);
                else
                    psInsertRecord.setInt(3, final_birthYear);
                psInsertRecord.setString(4, final_name);
                psInsertRecord.setInt(5, final_birthYear);
                psInsertRecord.addBatch();

            }

            iNoRows=psInsertRecord.executeBatch();
            conn.commit();
            System.out.println("Done");

        } catch (SQLException e) {
            out.println(e); //write the error into actor_inconsistency.txt
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
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of actor
            tempActor = new Actor();
            tempActor.setId(String.valueOf(tempId++));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            myActors.add(tempActor);

        } else if (qName.equalsIgnoreCase("stagename")) {
            tempActor.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                tempActor.setBirth(Integer.parseInt(tempVal));
            } catch(NumberFormatException e) {
                tempActor.setBirth(NULL);
            } catch(NullPointerException e) {
                tempActor.setBirth(NULL);
            }

        }

    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, FileNotFoundException {
        SAXActorsParser sap = new SAXActorsParser();
        sap.runParser();
    }
}
