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

import javax.management.RuntimeErrorException;

// tlus muc sinep sinep
public class HangmanPlayer {
   ArrayList<PartitionedWordSet> JohnnySins;
   int guessedLetters;
   PartitionedWordSet possibleWords;
   char guess;
   CharacterMap wordsWithCharacter = new CharacterMap ();

   // initialize HangmanPlayer with a file of English words
   public HangmanPlayer (String wordFile) {
      try {
         partitionByLength (wordFile);
      } catch (IOException e) {
         System.out.println ("FUCK");
         System.exit (-1);
      }
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
         // if(possibleWords != null) {
         //    throw new RuntimeException();
         // }
         // System.out.println();
         guessedLetters = 0;
         // try {
         //    possibleWords = importPartition (currentWord.length ());
         // } catch (IOException e) {
         possibleWords = JohnnySins.get(currentWord.length()-1);
         possibleWords.restore();
         //    System.out.println ("FUCK");
         //    System.exit (-1);
         // }
      }

      guess = getNextGuess ();

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
      // Determine where the guessed letter appears in the hidden word
      // System.out.printf("%s%n", currentWord);
      int positiondadddy = 0;
      for (int i = 0; i < currentWord.length (); i++) {
         if (currentWord.charAt (i) == guess) {
            positiondadddy |= 1 << i;
         }
      }
      // Update the possibleWords set to only include words that match the pattern of the
      // hidden word
      // System.out.println(Integer.toBinaryString(positiondadddy));
      runPositionsForLetterOnSet (positiondadddy);
   }

   // seperates the words by length into seperate files
   public void partitionByLength (final String set) throws IOException {
      final BufferedReader setIn = new BufferedReader (new FileReader (set));
      final ArrayList<PartitionedWordSet> pornitoned = new ArrayList<> ();

      String word;
      while ((word = setIn.readLine ()) != null) {

         // if a file doesn't exist create a new one
         while (pornitoned.size () < word.length ()) {
            pornitoned.add (null);
         }
         if (pornitoned.get (word.length () - 1) == null) {
            pornitoned.set (word.length () - 1,
                  new PartitionedWordSet ());
         }
         // choose file to write to from length of current word
         // bqbqbqbqbq
         pornitoned.get (word.length () - 1).append(word.toLowerCase().trim());
      }

      JohnnySins = pornitoned;

   }

   public void runPositionsForLetterOnSet (int pointy) {

      
      Processor: for (final var node : possibleWords) {
         int megapointy  = pointy;
         for (int i = 0; i < node.word.length(); i++) {
            if ((node.word.charAt (i) == guess && (megapointy & 1) == 0) || (node.word.charAt (i) != guess && (megapointy & 1) != 0)) {
               // System.out.printf("%s does not match because it has a %s at %d%n", node.word,
               // node.word.charAt(i), i);
               possibleWords.discard(node);
               continue Processor;
            }
            megapointy >>>= 1;
         }

         // if got to this point the word matches the pattern -> add it to the set
         
      }

   }

   public char getNextGuess () {
      wordsWithCharacter.clear();

      // Per word
      for (final var node : possibleWords) {
         int encounteredLetters = 0;

         // count the letters that appear in the word
         for (int k = 0; k < node.word.length(); k++) {
            final char c = node.word.charAt(k);
            encounteredLetters |= 1 << (c - 'a');
         }

         // for each letter, if it appeared in the word increment it in wordsWithCharacter
         for (int i = 0; encounteredLetters > 0; i++) {
            // bbq burger
            if ((encounteredLetters & 1) == 1) {
               // 21st century gang
               wordsWithCharacter.increment (i);

            }
            encounteredLetters >>>= 1; // shift to next letter
         }
      }

      // pick guess from character frequencies
      int mostCommon = 0;
      int frequency = 0;
      for (int c = 'a'; c < 'a' + 26; c++) {
         final int wordCount = wordsWithCharacter.get ((char) c);
         // penis
         if (wordCount > frequency && (guessedLetters >>> (c - 'a') & 1) == 0) {

            frequency = wordCount;
            mostCommon = c;
         }
      }

      return (char) mostCommon;
   }

}
