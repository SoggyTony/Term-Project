/*
 * Author: Peter Stelzer
 */

import java.io.IOException;
import java.util.LinkedList;

public class Guesser {
   final String word;

   private Guesser(final String word) {
      this.word = word;
   }

   public static void main(final String[] args) throws IOException {
      final long startTime = System.nanoTime();
      PartitionByLength.run("words.txt");
      System.out.println(run(args[0].toLowerCase()));
      System.out.println(System.nanoTime() - startTime);
   }

   /*
    * 0) Get set with words of right length
    * 1) Analyze set for letter that has not been guessed yet and is in the most
    * words
    * 2) Guess letter -> get word with guessed letter in positions
    * 3) Shrink set exclude words which do not have the guessed letter in positions
    * 4) Repeat from 1
    */
   public static boolean run(final String word) throws IOException {
      final StringBuilder sequence = new StringBuilder(word.length());
      sequence.append(word.length());
      int strikes = 0;

      final Guesser guesser = new Guesser(word);
      HomogenousWordSet possibleWords = MatchWordstoPattern.run(Integer.toString(word.length()));

      while (true) {
         // System.out.println (possibleWords);
         final char guess = CharacterDistributionAnalyzer.getNext(possibleWords, sequence.toString());

         // System.out.println (guess);

         if (possibleWords.occupancy == 0) {
            return false;
         }

         if (guess < 'a') {
            System.out.println(sequence.toString());
            System.out.println(possibleWords);
            return true;
         }

         sequence.append(guess);

         final var positions = guesser.checkGuess(guess);

         if (positions.size() == 0) {
            strikes++;

            if (strikes >= 6) {
               return false;
            }
         }

         sequence.append(positions.size());

         possibleWords = MatchWordstoPattern.runPositionsForLetterOnSet(positions, guess,
               possibleWords);
      }
   }

   public LinkedList<Integer> checkGuess(final char guess) {
      final LinkedList<Integer> presentIndices = new LinkedList<>();
      for (int i = 0; i < word.length(); i++) {
         if (word.charAt(i) == guess) {
            presentIndices.add(i);
         }
      }
      return presentIndices;
   }
}
