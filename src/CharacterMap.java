import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class CharacterMap {
   private final int[] table = new int[26];

   public int get (final char character) {
      return table[character - 'a'];
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
      forEach ( (character, count) -> {
         sb.append(String.format ("%s %d%n", character, count));
      });

      return sb.toString();
   }
}
