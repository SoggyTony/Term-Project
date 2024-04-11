import java.io.IOException;
import java.util.LinkedList;

public class Guesser {
   final String word;
   private final CharacterMap chars;

   private Guesser (final String word) {
      this.word = word;
      chars = new CharacterMap ().addFrom (word);
   }

   public static void main (final String[] args) throws IOException {
      final long startTime = System.nanoTime ();
      System.out.println (run (args[0].toLowerCase ()));
      System.out.println (System.nanoTime () - startTime);
   }

   public static boolean run (final String word) throws IOException {
      final StringBuilder sequence = new StringBuilder (word.length ());
      sequence.append (word.length ());
      int strikes = 0;

      final Guesser guesser = new Guesser (word);
      HomogenousWordSet possibleWords =
            MatchWordstoPattern.run (Integer.toString (word.length ()));

      while (true) {
         final char guess =
               CharacterDistributionAnalyzer.getNext (possibleWords, sequence.toString ());

         if (guess < 'a') {
            System.out.println (possibleWords);
            return true;
         }

         sequence.append (guess);

         final var positions = guesser.checkGuess (guess);

         if (positions.size () == 0) {
            strikes++;

            if (strikes >= 6) {
               return false;
            }
         }

         sequence.append (positions.size ());

         possibleWords = MatchWordstoPattern.runPositionsForLetterOnSet (positions, guess,
               possibleWords);
      }
   }

   public char guess (final String sequence) throws IOException {
      System.out.println (sequence);
      final var possibleSet = MatchWordstoPattern.run (sequence);
      return CharacterDistributionAnalyzer.getNext (possibleSet, sequence);
   }

   public LinkedList<Integer> checkGuess (final char guess) {
      final LinkedList<Integer> presentIndices = new LinkedList<> ();
      for (int i = 0; i < word.length (); i++) {
         if (word.charAt (i) == guess) {
            presentIndices.add (i);
         }
      }
      return presentIndices;
   }
}
