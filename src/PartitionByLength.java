/*
 * Author: Peter Stelzer
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class PartitionByLength {
   public static void main (final String[] args) throws IOException {
      run (args[0]);
   }

   public static int run (final String set) throws IOException {
      final BufferedReader setIn = new BufferedReader (new FileReader (set));
      final ArrayList<FileWriter> fws = new ArrayList<> ();

      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      String word;
      while ((word = setIn.readLine ()) != null) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }

         if (System.nanoTime () - markTime > 5000000000L) {
            System.out.printf ("Paritioned %d%n", wordsProcessed);
         }

         while (fws.size () < word.length ()) {
            fws.add (null);
         }
         if (fws.get (word.length () - 1) == null) {
            fws.set (word.length () - 1,
                  new FileWriter (String.format ("sets/%d.txt", word.length ())));
         }
         fws.get (word.length () - 1).write (String.format ("%s%n", word.toLowerCase ()));
         wordsProcessed++;
      }

      final int maxLength = fws.size ();

      for (final FileWriter fileWriter : fws) {
         if (fileWriter != null) {
            fileWriter.close ();
         }
      }

      return maxLength;
   }
}
