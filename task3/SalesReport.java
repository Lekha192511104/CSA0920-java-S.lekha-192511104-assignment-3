import java.io.*;
import java.util.*;

public class SalesReport {
    static int readTotal(String[] files) throws IOException {
        int total = 0;
        for (String name : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(name))) {
                String line;
                while ((line = br.readLine()) != null) {
                    total += Integer.parseInt(line.trim());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid sales value in " + name + ": " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File failure in " + name + ": " + e.getMessage());
                // Continue processing other files
            }
        }
        return total;
    }
    public static void main(String[] args) throws IOException {
        String[] files = {"store1.txt", "store2.txt", "store3.txt"};
        int total = readTotal(files);
        try (PrintWriter out = new PrintWriter(new FileWriter("summary.txt"))) {
            out.println("Total sales: " + total);
        }
        System.out.println("Total sales: " + total);
    }
}

