/*
 * Authors : Peter Stelzer; Anthony Menendez Mendez; Gianni Bubb; Joshua Cajuste
 * Email addresses of group members: pstelzer2023@my.fit.edu; amenendezmen2022@my.fit.edu;
 * gbubb2022@my.fit.edu; jcajuste2022@my.fit.edu
 * Group name: The Stardust Crusaders
 * Course: CSE 2010
 * Section: S23
 * Description of the overall algorithm:
 * Init: Create partitioned sets for each word length, which contain all the words of that
 * length and the best guess for those words.
 * 
 * guess(): If hiddenWord is a newWord, get the set of words of hiddenWord length into the
 * possible word set. Get the guess associated with the set. Mark that the letter was guessed.
 * 
 * feedback(): Find where the guessed letter appears in the hiddenWord. Update the
 * possibleWords set to only include words that match the pattern of the hiddenWord. If the set
 * has not been partitioned yet, partition it into sets with all of the possible positions of
 * the guess unless the set is small. If the set is small, shrink the set to only include words
 * that match the pattern and determine the next best guess.
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class HangmanPlayer {
   final ArrayList<Partition> partitions;
   Partition possibleWords;
   boolean skip;
   char guess;
   int guesssedLetters;

   private static final int BRANCH_MIN = 50;

   // initialize HangmanPlayer with a file of English words
   public HangmanPlayer (String wordFile) {
      partitions = loadWordFileIntoPartitions (wordFile);
   }

   // based on the current (partial or intitially blank) word
   // guess a letter
   // currentWord: current word, currenWord.length has the length of the hidden
   // word
   // isNewWord: indicates a new hidden word
   // returns the guessed letter
   // assume all letters are in lower case
   public char guess (String currentWord, boolean isNewWord) {

      if (isNewWord) {
         // We can only handle <=32 length words because of the way we encode guessed
         // letter positions
         skip = currentWord.length () > 32;

         possibleWords = partitions.get (currentWord.length () - 1);

         // If a length parition is smaller than the min, it will not be partitioned and the
         // NdWordSet must be restored from the previous use
         if (possibleWords.unprocessed != null
               && possibleWords.unprocessed.size < BRANCH_MIN) {
            possibleWords.unprocessed.restore ();
         }

         guesssedLetters = 0;
         guess = possibleWords.bestGuess;
      }

      guesssedLetters |= 1 << (guess - 'a');

      return guess;
   }

   // feedback on the guessed letter
   // isCorrectGuess: true if the guessed letter is one of the letters in the
   // hidden word
   // currentWord: partially filled or blank word
   //
   // Case isCorrectGuess currentWord
   // a. true partial word with the guessed letter
   // or the whole word if the guessed letter was the
   // last letter needed
   // b. false partial word without the guessed letter
   public void feedback (boolean isCorrectGuess, String currentWord) {
      if (skip) {
         return;
      }

      final int wordLength = currentWord.length ();

      // Determine where the guessed letter appears in the hidden word
      int positions = 0;
      int letterVal = 1; // the binary number with 1 in the spot corresponding to the current
                         // letter
      boolean complete = true;
      for (int i = 0; i < wordLength; i++) {
         if (currentWord.charAt (i) == guess) {
            positions |= letterVal;
         }
         letterVal <<= 1;

         if (currentWord.charAt (i) == ' ') {
            complete = false;
         }
      }

      if (!complete) {
         if (possibleWords.unprocessed != null
               && possibleWords.unprocessed.size < BRANCH_MIN) {

            guess = discardThenGuess (positions);
         }
         else {
            possibleWords = possibleWords.get (positions);
            guess = possibleWords.bestGuess;
         }
      }


   }

   // seperates the words by length into seperate files
   public ArrayList<Partition> loadWordFileIntoPartitions (final String wordFile) {
      final ArrayList<Partition> partitions = new ArrayList<> (24);

      try (BufferedReader setIn = new BufferedReader (new FileReader (wordFile))) {

         String word;
         while ((word = setIn.readLine ()) != null) {

            word = word.toLowerCase ();
            // if a partition doesn't exist create a new one
            while (partitions.size () < word.length ()) {
               partitions.add (new Partition ());
            }

            // add word to the partition containing the words of the same length
            partitions.get (word.length () - 1).unprocessed.append (word);

            // write the bit storing the characters that have been seen in the word
            int encounteredCharacters = 0;
            for (int i = 0; i < word.length (); i++) {
               final char currentLetter = word.charAt (i);
               final int val = 1 << (currentLetter - 'a');

               if ((encounteredCharacters & val) == 0) {
                  encounteredCharacters |= val;
                  partitions.get (word.length () - 1).wordTally
                        .increment (currentLetter - 'a');
               }
            }
         }
      } catch (IOException e) {
         throw new RuntimeException ("Failed to open wordFile");
      }

      // Get the letter that appears at least once in the most words for each partition
      for (int i = 0; i < partitions.size (); i++) {
         final var partition = partitions.get (i);
         partition.bestGuess = partition.wordTally.getLargest (0);
         partition.wordTally = partition.wordTally.surrender ();
         partition.guessedLetters |= 1 << (partition.bestGuess - 'a');
      }

      return partitions;
   }

   public char discardThenGuess (final int positionsWithGuess) {

      final CharacterMap wordsWithCharacter = CharacterMap.checkOut ();

      Processor: for (final var node : possibleWords.unprocessed) {
         final String word = node.word;

         int encounteredLetters = 0; // keeps track of what characters appear in this word
         int positions = positionsWithGuess; // copy positions with guessed letter to mutate
         for (int i = 0; i < word.length (); i++) {
            final char currentLetter = word.charAt (i);

            // If one invalid character -> discard
            if ((currentLetter == guess && (positions & 1) == 0)
                  || (currentLetter != guess && (positions & 1) == 1)) {
               // System.out.printf("%s does not match because it has a %s at %d%n", node.word,
               // node.word.charAt(i), i);
               possibleWords.unprocessed.discard (node);
               continue Processor;
            }

            encounteredLetters |= 1 << (currentLetter - 'a'); // mark letter
            positions >>>= 1;
         }

         // add characters that appear to the tally after sure we won't discard
         for (int j = 0; encounteredLetters > 0; j++) {
            if ((encounteredLetters & 1) == 1) {
               wordsWithCharacter.increment (j);
            }
            encounteredLetters >>>= 1; // shift to next letter
         }
      }


      // Pick next guess
      final char nextGuess = wordsWithCharacter.getLargest (guesssedLetters);
      wordsWithCharacter.surrender ();
      return nextGuess;
   }
}
