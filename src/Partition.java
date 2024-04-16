/*
 * Author: Peter Stelzer
 */

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.LinkedList;
import java.util.Queue;

public class Partition {
   static final Queue<ConcurrentSkipListMap<Integer, Partition>> reusable =
         new LinkedList<> ();

   ConcurrentSkipListMap<Integer, Partition> partitions;
   WordSet unprocessed;
   char bestGuess;
   int guessedLetters;
   CharacterMap wordTally;
   Partition next = null;

   public Partition () {
      this.unprocessed = WordSet.get ();
      wordTally = CharacterMap.getTally ();
   }

   public Partition (final WordSet baseSet, final char bestGuess,
         final int guessedLetters) {
      this.unprocessed = baseSet;
      this.bestGuess = bestGuess;
      this.guessedLetters = guessedLetters | (1 << (bestGuess - 'a'));
   }

   // Reuse empty skip lists
   private ConcurrentSkipListMap<Integer, Partition> getNewPartition () {
      var newPartition = reusable.poll ();
      if (newPartition == null) {
         newPartition = new ConcurrentSkipListMap<> ();
      }
      else {
         newPartition.clear ();
      }

      return newPartition;
   }

   /**
    * Retrieves the partition containing words that have the partition's letter in the given
    * positions
    * 
    * @param positions
    * @return
    */
   public Partition get (final int positions) {
      Partition ws = null;
      if (bestGuess < 'a') {
         return null;
      }

      // Only one partition was made
      if (next != null) {
         return next;
      }

      if (partitions == null) {  // partition into positions of bestGuess
         partitions = getNewPartition ();
         for (final var node : unprocessed) {
            final String word = node.word;

            int encounteredCharacters = 0; // keeps track of what characters appear in this
                                           // word
            int guessPositions = 0;
            int currentPosition = 1;
            for (int i = 0; i < word.length (); i++) {
               final char c = word.charAt (i);

               if (c == bestGuess) {
                  guessPositions |= currentPosition;
               }

               encounteredCharacters |= 1 << (c - 'a');

               currentPosition <<= 1; // the requirement for the next position
            }

            var correctPartition = partitions.get (guessPositions);
            if (correctPartition == null) {
               correctPartition = new Partition ();
               partitions.put (guessPositions, correctPartition);

               if (guessPositions == positions) {
                  ws = correctPartition;
               }
            }

            unprocessed.relocate (node, correctPartition.unprocessed);

            for (int i = 0; encounteredCharacters > 0; i++) {
               if ((encounteredCharacters & 1) == 1) {
                  correctPartition.wordTally.increment (i);
               }
               encounteredCharacters >>>= 1; // get if next character appeared
            }
         }

         // Finalize
         partitions.forEach ( (i, v) -> {
            v.bestGuess = v.wordTally.getLargest (guessedLetters);
            v.wordTally = v.wordTally.surrender ();
            v.guessedLetters = guessedLetters | (1 << (v.bestGuess - 'a'));
         });

         if (unprocessed.size != 0) {
            throw new RuntimeException ("there's still something here");
         }

         unprocessed = null; // unprocessed.surrender();

         // If only one partition was made, don't waste memory on a skiplist
         if (partitions.size () == 1) {
            next = partitions.higherEntry (-1).getValue ();
            reusable.add (partitions);
         }

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
