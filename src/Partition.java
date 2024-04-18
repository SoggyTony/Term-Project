/*
 * Author: Peter Stelzer
 */

import java.util.concurrent.ConcurrentSkipListMap;

public class Partition {
   ConcurrentSkipListMap<Integer, Partition> partitions;
   WordSet unprocessed;
   char bestGuess;
   int guessedLetters;
   CharacterMap wordTally;

   public Partition () {
      this.unprocessed = WordSet.checkOut ();
      wordTally = CharacterMap.checkOut ();
   }

   /**
    * Retrieves the subpartition containing words that have the partition's letter in the given
    * positions, partitioning itself if it has not already
    * 
    * @param positions
    * @return
    */
   public Partition get (final int positions) {
      Partition ws = null;
      if (bestGuess < 'a') {
         return null;
      }

      if (partitions == null) {  // partition into positions of bestGuess
         
         partitions = new ConcurrentSkipListMap<> ();
         for (final var node : unprocessed) {
            final String word = node.word;

            int encounteredCharacters = 0; // keeps track of what characters appear in this
                                           // word
            int guessPositions = 0;
            int currentPosition = 1;   // initialize to first position: 000...00000000000000001
            for (int i = 0; i < word.length (); i++) {
               final char c = word.charAt (i);

               if (c == bestGuess) {
                  guessPositions |= currentPosition;
               }

               encounteredCharacters |= 1 << (c - 'a');

               currentPosition <<= 1; // the next position
            }

            // Get or make subpartition for the current word's guess positions
            var subPartition = partitions.get(guessPositions);
            if (subPartition == null) {
               subPartition = new Partition();
               partitions.put(guessPositions, subPartition);

               if (guessPositions == positions) {
                  ws = subPartition;
               }
            }

            // Move from unpartitioned to correct partition
            unprocessed.relocate (node, subPartition.unprocessed);

            // Increment letter presences for that subpartition
            for (int i = 0; encounteredCharacters > 0; i++) {
               if ((encounteredCharacters & 1) == 1) {
                  subPartition.wordTally.increment (i);
               }
               encounteredCharacters >>>= 1; // get if next character appeared
            }
         }

         // Finalize each subpartition
         partitions.forEach ( (i, v) -> {
            v.bestGuess = v.wordTally.getLargest (guessedLetters);
            v.wordTally = v.wordTally.surrender ();
            v.guessedLetters = guessedLetters | (1 << (v.bestGuess - 'a'));
         });

         if (unprocessed.size != 0) {
            throw new RuntimeException ("there's still something here");
         }

         unprocessed = unprocessed.surrender();
         
      }

      // Get corresponding partition
      else {
         ws = partitions.get (positions);
         if (ws.unprocessed != null) {
            ws.unprocessed.restore ();
         }
      }

      return ws;
   }
}
