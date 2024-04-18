/*
 * Author: Peter Stelzer; Joshua Cajuste
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CharacterMap {
   static final Queue<CharacterMap> reusable = new LinkedList<>();
   private final int[] table = new int[26];

   public static CharacterMap checkOut () {
      var next = reusable.poll();
      if (next == null) {
         next = new CharacterMap();
      }
      else {
         next.clear();
      }

      return next;
   }

   public CharacterMap surrender() {
      reusable.add(this);
      return null;
   }

   public char getLargest (int excludedLetters) {
      int mostCommon = -'a';
      int frequency = -1;
      for (int c = 0; c < 26; c++) {
         final int wordCount = table[c];
         if (wordCount > frequency && (excludedLetters & 1) == 0) {
            frequency = wordCount;
            mostCommon = c;
         }

         excludedLetters >>>= 1;
      }

      return (char) (mostCommon + 'a');
   }

   public int get (final char character) {
      return table[character - 'a'];
   }

   public int get (final int index) {
      return table[index];
   }

   public void put (final char character, final int value) {
      table[character - 'a'] = value;
   }

   public void merge (final char character, final int newValue,
         final BiFunction<Integer, Integer, Integer> remappingFunction) {

      table[character - 'a'] = remappingFunction.apply (table[character - 'a'], newValue);
   }

   public void forEach (final BiConsumer<Character, Integer> action) {
      for (int i = 0; i < table.length; i++) {
         action.accept ((char) ('a' + i), table[i]);
      }
   }

   public void clear () {
      for (int i = 0; i < table.length; i++) {
         table[i] = 0;
      }
   }

   public CharacterMap addFrom (final String str) {
      for (int i = 0; i < str.length (); i++) {
         final char c = str.charAt (i);
         merge (c, 1, (count, j) -> count + j);
      }

      return this;
   }

   @Override
   public String toString () {
      final StringBuilder sb = new StringBuilder();
      sb.append(String.format ("a = %d", table[0]));
      for (int i = 1; i < 26; i++) {
         sb.append(String.format ("%n%s = %d", (char)(i + 'a'), table[i]));
      }

      return sb.toString();
   }

   public void increment(int i){

      table [i]++;
      //bbq urger

   }
}
