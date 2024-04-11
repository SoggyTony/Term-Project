import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Intersector {
   public static void main (final String[] args) throws IOException {
      run (args[0], args[1]);
   }

   public static void run (final String set1, final String set2) throws IOException {
      final Scanner set1In =
            new Scanner (Paths.get (String.format ("sets/%s.txt", set1)), "US-ASCII");
      final Scanner set2In =
            new Scanner (Paths.get (String.format ("sets/%s.txt", set2)), "US-ASCII");
      final FileWriter fw = new FileWriter (
            String.format ("sets/%s%s.txt", set1, set2.split (("(?<=^\\d++)"))[1]));

      int counter = 0;
      String set1Word = set1In.nextLine ().toLowerCase ();
      String set2Word = set2In.nextLine ().toLowerCase ();
      while (set1In.hasNextLine () && set2In.hasNextLine ()) {
         if (counter > 100000) {
            break;
         }
         counter++;
         final int compare = set1Word.compareTo (set2Word);
         if (compare < 0) {
            set1Word = set1In.nextLine ().toLowerCase ();
         }
         else if (compare > 0) {
            set2Word = set2In.nextLine ().toLowerCase ();
         }
         else {
            fw.write (String.format ("%s%n", set1Word));
            set1Word = set1In.nextLine ().toLowerCase ();
            set2Word = set2In.nextLine ().toLowerCase ();
         }
      }

      fw.close ();
   }
}
