/*
 * Author: Peter Stelzer
 */

import java.io.IOException;

public class AssortByCharacterPresence {
   public static void main (final String[] args) throws IOException {

   }

   public static HomogenousWordSet[] run (final HomogenousWordSet set, final char character) {
      final StringBuilder present = new StringBuilder ();
      final StringBuilder absent = new StringBuilder ();

      int wordsProcessed = 0;
      long markTime = System.nanoTime ();
      Processor: for (final String word : set) {
         if (wordsProcessed > Integer.MAX_VALUE) {
            break;
         }
         wordsProcessed++;

         if (System.nanoTime () - markTime > 500000000L) {
            System.out.printf ("Assorted %d%n", wordsProcessed);
         }

         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);
            if (c == character) {
               present.append(word);
               continue Processor;
            }
         }

         absent.append (word);
      }

      final HomogenousWordSet[] subSets = new HomogenousWordSet[2];
      subSets[0] = new HomogenousWordSet(absent, set.wordLength);
      subSets[1] = new HomogenousWordSet(present, set.wordLength);

      return subSets;
   }
}
