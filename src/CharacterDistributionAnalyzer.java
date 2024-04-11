import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class CharacterDistributionAnalyzer {
   public static void main (final String[] args) throws IOException {
      run (args[0]);
   }

   public static char getNext (final HomogenousWordSet set, final String sequence) {
      final CharacterMap wordsWithCharacter = new CharacterMap ();

      // Per word
      final CharacterMap characterCounts = new CharacterMap ();

      /* final int samples = Math.min(5000, set.occupancy);
      for (int i = 0; i < samples; i++) {
         final String word = set.getAt (i * (set.occupancy / samples));
         characterCounts.clear ();

         for (int k = 0; k < set.wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         characterCounts.forEach ( (character, occurances) -> {
            wordsWithCharacter.merge (character, Math.min (Math.max (occurances, 0), 1),
                  (count, j) -> count + j);
         });
      } */

      for (final String word : set) {
         characterCounts.clear ();

         for (int k = 0; k < set.wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         characterCounts.forEach ( (character, occurances) -> {
            wordsWithCharacter.merge (character, Math.min (Math.max (occurances, 0), 1),
                  (count, j) -> count + j);
         });
      }

      int mostCommon = 0;
      int frequency = 0;
      NextCharSelector: for (int c = 'a'; c < 'a' + 26; c++) {
         final int wordCount = wordsWithCharacter.get((char)c);
         if (wordCount > frequency) {
            for (int i = 0; i < sequence.length (); i++) {
               if (sequence.charAt (i) == c) {
                  continue NextCharSelector;
               }
            }
            frequency = wordCount;
            mostCommon = c;
         }
      }
  
      return (char)mostCommon;
   }

   public static int run (final String set) throws IOException {
      final Scanner setIn =
            new Scanner (Paths.get (String.format ("sets/%s.txt", set)), "US-ASCII");
      final FileWriter fw = new FileWriter (String.format ("counts/%s.txt", set));
      final StringBuilder sb = new StringBuilder ();

      final CharacterMap wordsWithCharacter = new CharacterMap ();
      final CharacterMap maxCharacterInAWord = new CharacterMap ();

      // Per word
      final CharacterMap characterCounts = new CharacterMap ();

      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      while (setIn.hasNextLine ()) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }

         if (System.nanoTime () - markTime > 500000000L) {
            System.out.printf ("Analyzed %d%n", wordsProcessed);
         }

         final String word = setIn.nextLine ().toLowerCase ();
         sb.append (word);
         characterCounts.clear ();

         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         characterCounts.forEach ( (character, occurances) -> {
            wordsWithCharacter.merge (character, Math.min (Math.max (occurances, 0), 1),
                  (count, j) -> count + j);
            maxCharacterInAWord.merge (character, occurances, (oldMax, currentMax) -> {
               return oldMax | (1 << currentMax);
               // if (currentMax > oldMax) {
               // return currentMax;
               // }
               // else {
               // return oldMax;
               // }
            });
         });

         wordsProcessed++;
      }

      final ReportEntry[] exhaustive = new ReportEntry[26];
      wordsWithCharacter.forEach ( (character, count) -> {
         exhaustive[character - 'a'] = new ReportEntry (character, count);
         final String str = String.format ("%s %d %s%n", character, count,
               Integer.toBinaryString (maxCharacterInAWord.get (character)));
         // System.out.print (str);
         try {
            fw.write (str);
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
         }
      });

      fw.close ();

      final HomogenousWordSet words = new HomogenousWordSet (sb, Integer.parseInt (set));
      wordsWithCharacter.clear ();

      final int samples = 1000;
      for (int i = 0; i < samples; i++) {
         final String word = words.getAt (i * (wordsProcessed / samples));

         characterCounts.clear ();

         for (int k = 0; k < words.wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         characterCounts.forEach ( (character, occurances) -> {
            wordsWithCharacter.merge (character, Math.min (Math.max (occurances, 0), 1),
                  (count, j) -> count + j);
         });
      }

      final ReportEntry[] lean = new ReportEntry[26];
      wordsWithCharacter.forEach ( (character, count) -> {
         lean[character - 'a'] = new ReportEntry (character, count);
         System.out.print (String.format ("%s %d%n", character, count));
      });

      Arrays.sort (exhaustive,
            Comparator.comparing (ReportEntry::hasCountsOf, Comparator.reverseOrder ()));
      Arrays.sort (lean,
            Comparator.comparing (ReportEntry::hasCountsOf, Comparator.reverseOrder ()));

      System.out.printf ("Exha: %s%n", Arrays.toString (exhaustive));
      System.out.printf ("Lean: %s%n", Arrays.toString (lean));

      return wordsProcessed;
   }

   record ReportEntry (char character, int hasCountsOf) {
      @Override
      public final String toString () {
         return Character.toString (character);
      }
   }

}
