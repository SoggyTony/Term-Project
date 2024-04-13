/*
  Authors : Peter Stelzer; Anthony Menendez Mendez; Gianni Bubb; Joshua Cajuste
  Email addresses of group members: pstelzer2023@my.fit.edu ; amenendezmen2022@my.fit.edu ; gbubb2022@my.fit.edu; jcajuste2022@my.fit.edu
  Group name: The Stardust Crusaders
  Course: CSE 2010
  Section: S23
  Description of the overall algorithm: 
*/

import java.io.IOException;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class HangmanPlayer {
  StringBuilder guessedLetters;
  HomogenousWordSet possibleWords;
  char guess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) {
    try {
      partitionByLength(wordFile);
    } catch (IOException e) {
      System.out.println("FUCK");
      System.exit(-1);
    }
  }

  // based on the current (partial or intitially blank) word
  // guess a letter
  // currentWord: current word, currenWord.length has the length of the hidden
  // word
  // isNewWord: indicates a new hidden word
  // returns the guessed letter
  // assume all letters are in lower case
  public char guess(String currentWord, boolean isNewWord) {

    if (isNewWord) {
      // System.out.println();
      guessedLetters = new StringBuilder(currentWord.length());
      guessedLetters.append(currentWord.length());
      try {
        possibleWords = importPartition(currentWord.length());
      } catch (IOException e) {
        System.out.println("FUCK");
        System.exit(-1);
      }
    }

    guess = getNextGuess(possibleWords, guessedLetters.toString());

    guessedLetters.append(guess);
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
  public void feedback(boolean isCorrectGuess, String currentWord) {
    // System.out.println("Current Word: " + currentWord);
    final LinkedList<Integer> presentIndices = new LinkedList<>();
    for (int i = 0; i < currentWord.length(); i++) {
      if (currentWord.charAt(i) == guess) {
        presentIndices.add(i);
      }
    }
    possibleWords = runPositionsForLetterOnSet(presentIndices, guess, possibleWords);
  }
  
  // seperates the words by length into seperate files
   public static void partitionByLength (final String set) throws IOException {
     final BufferedReader setIn = new BufferedReader (new FileReader (set));
     final ArrayList<FileWriter> fws = new ArrayList<> ();

      String word;
      while ((word = setIn.readLine ()) != null) {
         
         // if a file doesn't exist create a new one
         while (fws.size () < word.length ()) {
            fws.add (null);
         }
         if (fws.get (word.length () - 1) == null) {
            fws.set (word.length () - 1,
                  new FileWriter (String.format ("sets/%d.txt", word.length ())));
         }
         // choose file to write to from length of current word
         fws.get (word.length () - 1).write (String.format ("%s%n", word.toLowerCase ()));
      }

      for (final FileWriter fileWriter : fws) {
         if (fileWriter != null) {
            fileWriter.close ();
         }
      }
   }
   
   public static HomogenousWordSet runPositionsForLetterOnSet (
         final LinkedList<Integer> indices, final char c, final HomogenousWordSet set) {
      final StringBuilder setBuilder = new StringBuilder ();

      Processor: for (final String word : set) {
         final var posIterator = indices.iterator ();
         int nextPos = Integer.MAX_VALUE;
         if (posIterator.hasNext ()) {
            nextPos = posIterator.next ();
         }

         for (int i = 0; i < word.length (); i++) {
            // if the guessed character is in the correct position then get the next correct position
            if (word.charAt (i) == c && nextPos == i) {
               nextPos = posIterator.hasNext() ? posIterator.next() : Integer.MAX_VALUE;
            }
            // if the guessed character is not at a correct position then discard the current word
            else if (word.charAt (i) == c || nextPos <= i) {
               //System.out.printf("%s does not match because it has a %s at %d%n", word, word.charAt(i), i);
               continue Processor;
            }
         }

         // if got to this point the word matches the pattern -> add it to the set
         setBuilder.append (word);
      }

      return new HomogenousWordSet (setBuilder, set.wordLength);
   }
   
   // import parition and puts the file into word set
   public static HomogenousWordSet importPartition (final int wordLength) throws IOException {
      
      final CharacterMap characterCounts = new CharacterMap ();
      final StringBuilder setBuilder = new StringBuilder ();
      
      String word;
      final BufferedReader partitionIn =
            new BufferedReader (new FileReader (String.format ("sets/%d.txt", wordLength)));
      Processor: while ((word = partitionIn.readLine ()) != null) {
         word = word.toLowerCase ().trim ();
         
         characterCounts.clear ();
         
         for (int k = 0; k < wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }
         
         
         setBuilder.append (word);
      }
      
      return new HomogenousWordSet (setBuilder, wordLength);
   }
   
   public static char getNextGuess (final HomogenousWordSet set, final String sequence) {
      final CharacterMap wordsWithCharacter = new CharacterMap ();

      // Per word
      final CharacterMap characterCounts = new CharacterMap ();

      for (final String word : set) {
         characterCounts.clear ();

         // count the letters that appear in the word
         for (int k = 0; k < set.wordLength; k++) {
            final char c = word.charAt (k);
            characterCounts.merge (c, 1, (count, j) -> count + j);
         }

         // for each letter, if it appeared in the word increment it in wordsWithCharacter
         characterCounts.forEach ( (character, occurances) -> {
            wordsWithCharacter.merge (character, Math.min (Math.max (occurances, 0), 1),
                  (count, j) -> count + j);
         });
      }

      // pick guess from character frequencies
      int mostCommon = 0;
      int frequency = 0;
      NextCharSelector: for (int c = 'a'; c < 'a' + 26; c++) {
         final int wordCount = wordsWithCharacter.get((char)c);
         if (wordCount > frequency) {
            for (int i = 0; i < sequence.length (); i++) {
               if (sequence.charAt (i) == c) {
                  continue NextCharSelector;
               }
            }
            frequency = wordCount;
            mostCommon = c;
         }
      }
  
      return (char)mostCommon;
   }
   
}
