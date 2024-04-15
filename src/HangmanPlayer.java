/*
 * Authors : Peter Stelzer; Anthony Menendez Mendez; Gianni Bubb; Joshua Cajuste
 * Email addresses of group members: pstelzer2023@my.fit.edu; amenendezmen2022@my.fit.edu;
 * gbubb2022@my.fit.edu; jcajuste2022@my.fit.edu
 * Group name: The Stardust Crusaders
 * Course: CSE 2010
 * Section: S23
 * Description of the overall algorithm:
 * Init: Create files for each word length, which contain all the words of that length.
 * 
 * guess(): If hiddenWord is a newWord, load all words of hiddenWord length into the possible
 * word set. Next, find and guess the letter that appears in the most words in the
 * possibleWords set that has not been guessed before. Mark that the letter was guessed.
 * 
 * feedback(): Find where the guessed letter appears in the hiddenWord. Update the
 * possibleWords set to only include words that match the pattern of the hiddenWord.
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class HangmanPlayer {
   final ArrayList<PartitionedWordSet> partitions;
   int guessedLetters;
   PartitionedWordSet possibleWords;
   char guess;
   final CharacterMap wordsWithCharacter;
   boolean skip;

   // initialize HangmanPlayer with a file of English words
   public HangmanPlayer (String wordFile) {
      partitions = loadWordFileIntoPartitions (wordFile);
      wordsWithCharacter = new CharacterMap ();
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

         guessedLetters = 0;
         possibleWords = partitions.get (currentWord.length () - 1);
         possibleWords.restore ();

         guess = possibleWords.bestFirstGuess;
      }

      guessedLetters |= 1 << (guess - 'a');

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

      // Determine where the guessed letter appears in the hidden word
      int positions = 0;
      int letterVal = 1; // the binary number with 1 in the spot corresponding to the current
                         // letter
      for (int i = 0; i < currentWord.length (); i++) {
         if (currentWord.charAt (i) == guess) {
            positions |= letterVal;
         }
         letterVal <<= 1;
      }

      // Update the possibleWords set to only include words that match the pattern of
      // the hidden word
      discardThenGuess (positions);
   }

   // seperates the words by length into seperate files
   public ArrayList<PartitionedWordSet> loadWordFileIntoPartitions (final String wordFile) {
      final ArrayList<PartitionedWordSet> partitions = new ArrayList<> (24);
      final ArrayList<CharacterMap> wordMapList = new ArrayList<> (24);

      try (BufferedReader setIn = new BufferedReader (new FileReader (wordFile))) {       

         String word;
         while ((word = setIn.readLine ()) != null) {

            word = word.toLowerCase ();
            // if a partition doesn't exist create a new one
            while (partitions.size () < word.length ()) {
               partitions.add (new PartitionedWordSet ());
               wordMapList.add (new CharacterMap ());
            }

            // add word to the partition containing the words of the same length
            partitions.get (word.length () - 1).append (word);

            // write the bit storing the characters that have been seen in the word
            int encounteredCharacters = 0;
            for (int i = 0; i < word.length (); i++) {
               final char currentLetter = word.charAt (i);
               final int val = 1 << (currentLetter - 'a');

               if ((encounteredCharacters & val) == 0) {
                  encounteredCharacters |= val;
                  wordMapList.get (word.length () - 1).increment (currentLetter - 'a');
               }
            }
         }
      } catch (IOException e) {
         throw new RuntimeException ("Failed to open wordFile");
      }

      // Get the letter that appears at least once in the most words for each partition
      for (int i = 0; i < partitions.size (); i++) {
         int mostCommon = -'a'; // we want null character if no letter can be picked
         int frequency = -1;
         for (int currentLetter = 0; currentLetter < 26; currentLetter++) {
            final int wordCount = wordMapList.get (i).get (currentLetter);
            if (wordCount > frequency) {
               frequency = wordCount;
               mostCommon = currentLetter;
            }
         }
         partitions.get (i).bestFirstGuess = (char) (mostCommon + 'a');
      }

      return partitions;
   }

   public void discardThenGuess (final int positionsWithGuess) {

      wordsWithCharacter.clear ();

      Processor: for (final var node : possibleWords) {
         final String word = node.word;

         int encounteredLetters = 0;   // keeps track of what characters appear in this word
         int positions = positionsWithGuess; // copy positions with guessed letter to mutate
         for (int i = 0; i < word.length (); i++) {
            final char currentLetter = word.charAt (i);

            // If one invalid character -> discard
            if ((currentLetter == guess && (positions & 1) == 0)
                  || (currentLetter != guess && (positions & 1) == 1)) {
               // System.out.printf("%s does not match because it has a %s at %d%n", node.word,
               // node.word.charAt(i), i);
               possibleWords.discard (node);
               continue Processor;
            }

            encounteredLetters |= 1 << (currentLetter - 'a');  // mark letter
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
      int mostCommon = -'a'; // we want null character if no letter can be picked
      int frequency = -1;
      for (int currentLetter = 0; currentLetter < 26; currentLetter++) {
         final int wordCount = wordsWithCharacter.get (currentLetter);
         if (wordCount > frequency && ((guessedLetters >>> currentLetter) & 1) == 0) {
            frequency = wordCount;
            mostCommon = currentLetter;
         }
      }
      guess = (char) (mostCommon + 'a');
   }
}
