/*

  Authors (group members):
  Email addresses of group members:
  Group name:

  Course:
  Section:

  Description of the overall algorithm:


*/

import java.io.IOException;
import java.util.LinkedList;

public class HangmanPlayer {
  StringBuilder sequence;
  HomogenousWordSet possibleWords;
  char guess;

  // initialize HangmanPlayer with a file of English words
  public HangmanPlayer(String wordFile) {
    try {
      PartitionByLength.run(wordFile);
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
      sequence = new StringBuilder(currentWord.length());
      sequence.append(currentWord.length());
      try {
        possibleWords = MatchWordstoPattern.run(Integer.toString(currentWord.length()));
      } catch (IOException e) {
        System.out.println("FUCK");
        System.exit(-1);
      }
    }

    guess = CharacterDistributionAnalyzer.getNext(possibleWords, sequence.toString());

    sequence.append(guess);
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
    possibleWords = MatchWordstoPattern.runPositionsForLetterOnSet(presentIndices, guess, possibleWords);
  }
}