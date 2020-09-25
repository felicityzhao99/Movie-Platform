import javax.print.DocFlavor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class log_processing {
    public void main(String[] args) {
        try {
            String filepath = "search_log1.txt";

            File myObj = new File(filepath);
            Scanner myReader = new Scanner(myObj);

            int count = 0;
            long TS_total = 0;
            long TJ_total = 0;

            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String res[] = data.split(",");
                TS_total += Long.parseLong(res[0]);
                TJ_total += Long.parseLong(res[1]);
                count ++;
            }
            long TS = TS_total/count;
            long TJ = TJ_total/count;

            String writepath = "average.txt";

            try {
                FileWriter myWriter = new FileWriter(writepath,true);
                myWriter.write(TS + "," + TJ + "\n");
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
