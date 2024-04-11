import java.util.Iterator;

public class HomogenousWordSet implements Iterable<String> {
   final String set;
   final int wordLength;
   final int occupancy;

   public HomogenousWordSet (final StringBuilder sb, final int wordLength) {
      this.set = sb.toString ();
      this.wordLength = wordLength;
      this.occupancy = set.length() / wordLength;
   }

   public String getAt (final int i) {
      return set.substring (i * wordLength, (i + 1) * wordLength);
   }

   @Override
   public Iterator<String> iterator () {
      return new Iterator<String>() {
         int i = 0;

         @Override
         public boolean hasNext () {
            return i < occupancy;
         }

         @Override
         public String next () {
            return getAt(i++);
         }
         
      };
   }

   @Override
   public String toString () {
      final StringBuilder sb = new StringBuilder();
      for (final String word : this) {
         sb.append(String.format("%s%n",word));
      }
      return sb.toString();
   }
}
