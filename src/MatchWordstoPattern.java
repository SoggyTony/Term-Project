import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class MatchWordstoPattern {
   public static void main (final String[] args) throws IOException {
      System.out.println (run ("11e2r1t0n0i0l2s0o2g1b0f1x1y1"));
   }

   public static HomogenousWordSet runParticular (final String sequence) throws IOException {
      final String[] tokens = sequence.split ("(?<=[^a-z]+[a-z])|(?=[a-z][^a-z])");
      final int wordLength = Integer.parseInt (tokens[0]);
      final CharacterMap map = new CharacterMap ();
      for (int i = 1; i < tokens.length; i += 2) {
         final int count =
               (tokens[i + 1].equals ("*")) ? -1 : Integer.parseInt (tokens[i + 1]);
         map.put (tokens[i].charAt (0), count);
      }

      final CharacterMap characterCounts = new CharacterMap ();
      final StringBuilder setBuilder = new StringBuilder ();

      int wordsProcessed = 0;
      int wordsFailed = 0;
      String word;
      final BufferedReader partitionIn =
            new BufferedReader (new FileReader (String.format ("sets/%d.txt", wordLength)));
      Processor: while ((word = partitionIn.readLine ()) != null) {
         word = word.toLowerCase ().trim ();

         wordsProcessed++;

         characterCounts.clear ();

         for (int k = 0; k < wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         for (int k = 'a'; k <= 'z'; k++) {
            if (map.get ((char) k) != -1
                  && characterCounts.get ((char) k) != map.get ((char) k)) {
               wordsFailed++;
               continue Processor;
            }
         }

         setBuilder.append (word);
      }

      // System.out.printf ("%d processed, %d failed%n", wordsProcessed, wordsFailed);

      return new HomogenousWordSet (setBuilder, wordLength);
   }

   public static HomogenousWordSet run (final String sequence) throws IOException {
      final String[] tokens = sequence.split ("(?<=[^a-z]+[a-z])|(?=[a-z][^a-z])");
      final int wordLength = Integer.parseInt (tokens[0]);
      final CharacterMap map = new CharacterMap ();

      for (int k = 'a'; k <= 'z'; k++) {
         map.put ((char) k, -1);
      }

      for (int i = 1; i < tokens.length; i += 2) {
         final int count =
               (tokens[i + 1].equals ("*")) ? -1 : Integer.parseInt (tokens[i + 1]);
         map.put (tokens[i].charAt (0), count);
      }

      final CharacterMap characterCounts = new CharacterMap ();
      final StringBuilder setBuilder = new StringBuilder ();

      int wordsProcessed = 0;
      int wordsFailed = 0;
      String word;
      final BufferedReader partitionIn =
            new BufferedReader (new FileReader (String.format ("sets/%d.txt", wordLength)));
      Processor: while ((word = partitionIn.readLine ()) != null) {
         word = word.toLowerCase ().trim ();

         wordsProcessed++;

         characterCounts.clear ();

         for (int k = 0; k < wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         for (int k = 'a'; k <= 'z'; k++) {
            if (map.get ((char) k) != -1
                  && characterCounts.get ((char) k) != map.get ((char) k)) {
               wordsFailed++;
               continue Processor;
            }
         }

         setBuilder.append (word);
      }

      // System.out.printf ("%d processed, %d failed%n", wordsProcessed, wordsFailed);

      return new HomogenousWordSet (setBuilder, wordLength);
   }

   public static HomogenousWordSet runParitalWord (final String partialWord)
         throws IOException {
      final StringBuilder setBuilder = new StringBuilder ();
      final LinkedList<Integer> presentIndices = new LinkedList<> ();

      for (int i = 0; i < partialWord.length (); i++) {
         if (partialWord.charAt (i) != ' ') {
            presentIndices.add (i);
         }
      }

      String word;
      final BufferedReader partitionIn = new BufferedReader (
            new FileReader (String.format ("sets/%d.txt", partialWord.length ())));
      Processor: while ((word = partitionIn.readLine ()) != null) {
         word = word.toLowerCase ().trim ();

         for (final int i : presentIndices) {
            if (word.charAt (i) != partialWord.charAt (i)) {
               continue Processor;
            }
         }

         setBuilder.append (word);
      }

      return new HomogenousWordSet (setBuilder, partialWord.length ());
   }

   public static HomogenousWordSet runPositionsForLetterOnSet (
         final LinkedList<Integer> indices, final char c, final HomogenousWordSet set) {
      final StringBuilder setBuilder = new StringBuilder ();

      Processor: for (final String word : set) {
         for (final int i : indices) {
            if (word.charAt (i) != c) {
               continue Processor;
            }
         }

         setBuilder.append (word);
      }

      return new HomogenousWordSet (setBuilder, set.wordLength);
   }
}
