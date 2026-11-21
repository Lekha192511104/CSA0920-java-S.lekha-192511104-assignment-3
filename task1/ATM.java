import java.util.*;
import java.io.*;

public class ATM {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double balance = 5000;
        try {
            System.out.print("Enter account number: ");
            String account = sc.nextLine();
            System.out.print("Enter withdrawal amount: ");
            int amount = sc.nextInt();
            if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
            if (amount > balance) throw new IllegalStateException("Insufficient balance");
            // Simulate a card reader file check
            FileReader reader = new FileReader("cardreader.txt");
            reader.close();
            balance -= amount;
            System.out.println("Withdrawal successful. Balance: " + balance);
        } catch (InputMismatchException | IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("Transaction rejected: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Card reader error: " + e.getMessage());
        } finally {
            System.out.println("ATM session ended (finally executed).");
            sc.close();
        }
    }
}
