/*
 * Author: Peter Stelzer
 */

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class AssortByCharacterOccurance {
   public static void main (final String[] args) throws IOException {
      CharacterDistributionAnalyzer.run (args[0]);
      // runAll(args[0]);
      run (args[0], args[1].charAt (0));
   }

   public static HomogenousWordSet[] run (final HomogenousWordSet set, final char character) {
      final ArrayList<StringBuilder> setBuilders = new ArrayList<> ();

      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      for (final String word : set) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }
         wordsProcessed++;

         if (System.nanoTime () - markTime > 500000000L) {
            System.out.printf ("Assorted %d%n", wordsProcessed);
         }

         int occurances = 0;

         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);
            if (c == character) {
               occurances++;
            }
         }

         while (occurances >= setBuilders.size()) {
            setBuilders.add(null);
         }

         if (setBuilders.get(occurances) == null) {
            setBuilders.set(occurances, new StringBuilder());
         }

         setBuilders.get(occurances).append(word);
      }

      final HomogenousWordSet[] subSets = new HomogenousWordSet[setBuilders.size ()];
      for (int i = 0; i < setBuilders.size (); i++) {
         final StringBuilder setBuilder = setBuilders.get (i);
         if (setBuilder == null) {
            continue;
         }

         subSets[i] = new HomogenousWordSet (setBuilder, set.wordLength);
      }

      return subSets;
   }

   public static void run (final String set, final char character) throws IOException {
      final Scanner setIn =
            new Scanner (Paths.get (String.format ("sets/%s.txt", set)), "US-ASCII");
      final ArrayList<FileWriter> fws = new ArrayList<> ();

      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      while (setIn.hasNextLine ()) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }

         if (System.nanoTime () - markTime > 500000000L) {
            System.out.printf ("Assorted %d%n", wordsProcessed);
         }

         final String word = setIn.nextLine ().toLowerCase ();
         int occurances = 0;

         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);
            if (c == character) {
               occurances++;
            }
         }

         while (fws.size () <= occurances) {
            fws.add (null);
         }
         if (fws.get (occurances) == null) {
            fws.set (occurances, new FileWriter (
                  String.format ("sets/%s%s%d.txt", set, character, occurances)));
         }
         fws.get (occurances).write (String.format ("%s%n", word));

         wordsProcessed++;

      }

      for (final FileWriter fileWriter : fws) {
         if (fileWriter != null) {
            fileWriter.close ();
         }
      }
   }

   public static void runAll (final String set) throws IOException {
      final FileWriter[][] subSets = new FileWriter[26][];
      final Scanner file =
            new Scanner (Paths.get (String.format ("counts/%s.txt", set)), "US-ASCII");
      for (int i = 0; file.hasNextLine (); i++) {
         final String[] tokens = file.nextLine ().split ("\\s+");
         final int maxOccurances = Integer.parseInt (tokens[2]);
         subSets[i] = new FileWriter[maxOccurances + 1];
      }

      final Scanner dict =
            new Scanner (Paths.get (String.format ("sets/%s.txt", set)), "US-ASCII");
      final CharacterMap characterCounts = new CharacterMap ();
      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      while (dict.hasNextLine ()) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }

         if (System.nanoTime () - markTime > 500000000L) {
            System.out.println (wordsProcessed);
         }

         final String word = dict.nextLine ().toLowerCase ();
         characterCounts.clear ();

         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         characterCounts.forEach ( (character, occurances) -> {
            try {
               if (subSets[character - 'a'][occurances] == null) {
                  subSets[character - 'a'][occurances] = new FileWriter (
                        String.format ("sets/%s%s%d.txt", set, character, occurances));
               }
               subSets[character - 'a'][occurances].write (String.format ("%s%n", word));
            } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace ();
            } catch (ArrayIndexOutOfBoundsException e) {
               System.out.printf ("%s%d %s%n", character, occurances, word);
               System.exit (-1);
            }
         });

         wordsProcessed++;
      }


      for (final FileWriter[] arr : subSets) {
         for (final FileWriter fw : arr) {
            fw.close ();
         }
      }
   }
}
