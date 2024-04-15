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
   final ArrayList<WordSetPartitions> partitions;
   int guessedLetters;
   WordSetPartitions.NondestructiveWordSet possibleWords;
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
         possibleWords = null;
         guess = partitions.get (currentWord.length () - 1).bestFirstGuess;
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

      final int wordLength = currentWord.length ();

      // Determine where the guessed letter appears in the hidden word
      int positions = 0;
      int occurances = 0;
      int letterVal = 1; // the binary number with 1 in the spot corresponding to the current
                         // letter
      for (int i = 0; i < wordLength; i++) {
         if (currentWord.charAt (i) == guess) {
            positions |= letterVal;
            occurances++;
         }
         letterVal <<= 1;
      }

      if (possibleWords == null) {
         if (partitions.get (wordLength - 1).partitioned) {
            possibleWords = partitions.get (wordLength - 1).sets.get (occurances);
            possibleWords.restore ();

         }
         else {
            possibleWords = partitions.get (wordLength - 1).sets.get (0);
            partitionDiscardAndGetNextGuess (positions, occurances);
            return;
         }
      }

      // Update the possibleWords set to only include words that match the pattern of
      // the hidden word
      discardThenGuess (positions);
   }

   // seperates the words by length into seperate files
   public ArrayList<WordSetPartitions> loadWordFileIntoPartitions (final String wordFile) {
      final ArrayList<WordSetPartitions> partitions = new ArrayList<> (24);
      final ArrayList<CharacterMap> wordMapList = new ArrayList<> (24);

      try (BufferedReader setIn = new BufferedReader (new FileReader (wordFile))) {       

         String word;
         while ((word = setIn.readLine ()) != null) {

            word = word.toLowerCase ();
            // if a partition doesn't exist create a new one
            while (partitions.size () < word.length ()) {
               partitions.add (new WordSetPartitions ());
               wordMapList.add (new CharacterMap ());
            }

            // add word to the partition containing the words of the same length
            partitions.get (word.length () - 1).sets.get (0).append (word);

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


   private void partitionDiscardAndGetNextGuess (final int positions, final int occurancesInHiddenWord) {
      wordsWithCharacter.clear ();
      final var partition = possibleWords.superSet ();

      for (final var node : possibleWords) {
         final String word = node.word;

         int encounteredCharacters = 0;   // keeps track of what characters appear in this word
         int mutPositions = positions; // copy I can mutate for this word
         int guessOccurances = 0;
         boolean discard = false;
         for (int i = 0; i < word.length (); i++) {
            final char c = word.charAt (i);

            if (c == guess) {
               guessOccurances++;
            }

            if (discard) {
               continue;
            }

            // If one invalid character -> discard
            if ((c == guess && (mutPositions & 1) == 0)
                  || (c != guess && (mutPositions & 1) == 1)) {
               // System.out.printf("%s does not match because it has a %s at %d%n", word,
               // word.charAt(i), i);
               discard = true;

            }

            encounteredCharacters |= 1 << (c - 'a');

            mutPositions >>= 1;  // the requirement for the next position
         }

         if (guessOccurances > 0) {
            while (partition.sets.size () <= guessOccurances) {
               partition.sets.add (partition.new NondestructiveWordSet ());
            }

            // System.out.printf("relocating %s to %d%n", node.word, guessOccurances);
            possibleWords.relocate (node, partition.sets.get (guessOccurances));
         }

         if (guessOccurances == occurancesInHiddenWord && discard) {
            partition.sets.get (guessOccurances).discard (node);
         }

         if (!discard) {
            // add characters that appear to the tally after sure we won't discard
            // the 25th bit of encounteredChars is the most significant bit that will ever be
            // set
            // so i will never > 25
            // System.out.println(Integer.toBinaryString(encounteredCharacters));
            for (int i = 0; encounteredCharacters > 0; i++) {
               if ((encounteredCharacters & 1) == 1) {
                  wordsWithCharacter.increment (i);
               }
               encounteredCharacters >>>= 1; // get if next character appeared
            }
         }
      }

      // Pick next guess
      int mostCommon = 0;
      int frequency = 0;
      for (int c = 0; c < 26; c++) {
         final int wordCount = wordsWithCharacter.get (c);
         if (wordCount > frequency && ((guessedLetters >> c) & 1) == 0) {
            frequency = wordCount;
            mostCommon = c;
         }
      }

      guess = (char) (mostCommon + 'a');

      possibleWords = partition.sets.get (occurancesInHiddenWord);
      partition.partitioned = true;
   }
}
