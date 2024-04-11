/*
 * 1) Get word length
 * 2) Get letter which appears at least once in the most words of that length (precomputed)
 * 3) Guess letter, get number of that letter in the word
 * 4) Get letter which appears at least once in the most words of that length with that many of
 * the previous letter (precomputed)
 */


/*
 * represent with (response, guess) tuple nodes where response is the length of the word if its
 * the root and the number of letters uncovered otherwise and the guess is the response to the
 * new information.
 * Each node will have children as the possible responses
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class App {
   final static Scanner stdIn = new Scanner (System.in);
   static FileWriter successes;
   static FileWriter failedWords;
   static int numberGuessedWords = 0;
   static int numberFailedWords = 0;
   final static Pattern pattern = Pattern.compile ("[a-z]\\d+$");

   public static void main (String[] args) throws Exception {
      successes = new FileWriter ("sequences.txt");
      failedWords = new FileWriter ("failedWords.txt");
      final int maxLength = PartitionByLength.run (args[0]);

      for (int i = 2; i <= maxLength; i++) {
         // int i = 9;
         System.out.println (i);
         final BufferedReader partitionIn =
               new BufferedReader (new FileReader (String.format ("sets/%d.txt", i)));
         final StringBuilder sb = new StringBuilder ();

         String word;
         while ((word = partitionIn.readLine ()) != null) {
            sb.append (word);
         }
         partitionIn.close ();
         successes.write (String.format ("%n%d", i));
         run2 (new HomogenousWordSet (sb, i), Integer.toString (i), 0);
      }
      successes.close ();
      System.out.println (numberGuessedWords);
      System.out.println (numberFailedWords);
   }

   public static void run2 (final HomogenousWordSet set, final String sequence,
         final int fails) throws IOException {

      if (set == null || set.occupancy == 0) {
         return;
      }

      numberGuessedWords += MatchWordstoPattern.runParticular (sequence).occupancy;

      if (sequence.length () > 2) {
         successes.write (String.format ("%n%s", sequence.split ("(?=[a-z]\\d+$)")[1]));
      }

      //DOES SET CONTAIN GUESSED WORDS
      if (fails >= 6) {
         // failedWords.write (String.format ("%s%n", sequence));
         failedWords.write (set.toString ());
         numberFailedWords += set.occupancy;
         successes.write ("**\\");
         return;
      }

      final char nextGuess = CharacterDistributionAnalyzer.getNext (set, sequence);

      if (nextGuess < 'a') {
         // final Matcher matcher = pattern.matcher(sequence);
         // System.out.println(sequence);
         // System.out.println(matcher.group());
         successes.write ("\\");
         /// successes.write (String.format ("%s%n", sequence));
         return;
      }

      final var subSets = AssortByCharacterPresence.run (set, nextGuess);
      for (int i = 0; i < subSets.length; i++) {
         final var subSet = subSets[i];
         if (subSet == null) {
            continue;
         }

         if (i == 0) {
            run2 (subSet, String.format ("%s%s%s", sequence, nextGuess, '0'), fails + 1);
         }
         else {
            run2 (subSets[1], String.format ("%s%s%d", sequence, nextGuess, i), fails);
         }
      }
   }

   public static void run (final String set, final int fails) throws IOException {
      if (fails >= 6) {
         return;
      }
      // System.out.println (set);
      CharacterDistributionAnalyzer.run (set);

      final Scanner reportIn =
            new Scanner (Paths.get (String.format ("counts/%s.txt", set)), "US-ASCII");

      int mostCommon = -1;
      int frequency = 0;
      String validCounts = null;
      NextCharSelector: while (reportIn.hasNextLine ()) {
         final String[] tokens = reportIn.nextLine ().split ("\\s+");
         final char character = tokens[0].charAt (0);
         final int wordCount = Integer.parseInt (tokens[1]);
         if (wordCount > frequency) {
            for (int i = 0; i < set.length (); i++) {
               if (set.charAt (i) == character) {
                  continue NextCharSelector;
               }
            }
            frequency = wordCount;
            mostCommon = character;
            validCounts = tokens[2];
         }
      }

      // System.out.println(validCounts);
      if (frequency > 0) {
         AssortByCharacterOccurance.run (set, (char) mostCommon);
         for (int i = 0; i < validCounts.length (); i++) {
            // System.out.printf("About to assort %s for counts of %s, continue?", set,
            // (char)mostCommon);
            // stdIn.hasNextLine();
            if (validCounts.charAt (validCounts.length () - i - 1) == '1') {
               run (String.format ("%s%s%d", set, (char) mostCommon, i),
                     fails + ((i == 0) ? 1 : 0));
            }
         }
      }
   }
}
