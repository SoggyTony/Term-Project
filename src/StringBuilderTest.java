/*
 * Author: Peter Stelzer
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class StringBuilderTest {
   public static void main(final String[] args) throws IOException {
      final BufferedReader setIn = new BufferedReader (new FileReader ("hiddenWords1.txt"));
      final StringBuilder sbSet = new StringBuilder();

      String word;
      while ((word = setIn.readLine ()) != null) {
         sbSet.append(word);
      }

      long markTime;
      final Random rng = new Random();
      markTime = System.nanoTime();
      for (int i = 0; i < 1000; i++) {
         sbSet.charAt(rng.nextInt(sbSet.length()));
      }
      System.out.println((System.nanoTime() - markTime) / 1000d);

      markTime = System.nanoTime();
      final HomogenousWordSet strSet = new HomogenousWordSet(sbSet, 1);
      System.out.println(System.nanoTime() - markTime);

      markTime = System.nanoTime();
      for (int i = 0; i < 1000; i++) {
         strSet.set.charAt(rng.nextInt(strSet.set.length()));
      }
      System.out.println((System.nanoTime() - markTime) / 1000d);
   }
}