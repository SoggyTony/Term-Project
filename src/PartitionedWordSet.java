import java.util.Iterator;

public class PartitionedWordSet
      implements Iterable<PartitionedWordSet.Node>, Iterator<PartitionedWordSet.Node> {

   private final Node header;
   private final Node trailer;
   // seperates valid and discarded nodes
   private final Node boundary;
   public char bestFirstGuess;
   // number of discarded and valid nodes
   int size;
   // number of nodes in the list that has not been discarded
   int occupancy;

   private Node iteratorCurrent;

   PartitionedWordSet() {
      header = new Node(null, null, null);
      boundary = new Node(header, null, null);
      trailer = new Node(boundary, null, null);

      header.next = boundary;
      boundary.next = trailer;

      size = 0;
      occupancy = 0;
   }

   public class Node {
      private Node prev;
      private Node next;
      public final String word;

      private Node(Node prev, Node next, String word) {
         this.prev = prev;
         this.next = next;
         this.word = word;
      }
   }

   // node is placed just before boundary
   public void append(String word) {
      Node newNode = new Node(boundary.prev, boundary, word);
      size++;
      occupancy++;

      boundary.prev.next = newNode;
      boundary.prev = newNode;
   }

   // node is placed directly after boundary
   public void discard(Node discardNode) {
      discardNode.prev.next = discardNode.next;
      discardNode.next.prev = discardNode.prev;

      discardNode.prev = boundary;
      discardNode.next = boundary.next;

      boundary.next.prev = discardNode;
      boundary.next = discardNode;

      occupancy--;
   }

   // move the boundary after the second to last node and before trailer
   // which sets all discarded nodes as valid nodes
   public void restore() {
      if (boundary.next != trailer) {
         boundary.prev.next = boundary.next;
         boundary.next.prev = boundary.prev;

         boundary.prev = trailer.prev;
         boundary.next = trailer;

         trailer.prev.next = boundary;
         trailer.prev = boundary;

         // occupancy is reset
         occupancy = size;
      }
   }

   public static void main(String[] args) {
      PartitionedWordSet test = new PartitionedWordSet();

      for (int i = 0; i < 35; i++) {
         test.append(Integer.toString(i));
      }

      System.out.println(test.toStringTesting());

      System.out.println(test.size);
      System.out.println(test.occupancy);

      var current = test.header.next;
      while (current != test.boundary) {
         current = current.next;
         if (Math.random() < 0.5) {
            test.discard(current.prev);
         }
      }

      System.out.println(test.toStringTesting());

      System.out.println(test.size);
      System.out.println(test.occupancy);

      test.restore();

      System.out.println(test.toStringTesting());

      System.out.println(test.size);
      System.out.println(test.occupancy);
   }

   // for checking linked list's creation appending, discarding, and restore
   private String toStringTesting() {
      StringBuilder bobList = new StringBuilder();
      Node current = header.next;

      bobList.append("h");
      while (current != null) {
         bobList.append("-");

         if (current == boundary) {
            bobList.append("b");
         } else if (current == trailer) {
            bobList.append("t");
         } else {
            bobList.append(current.word);
         }
         current = current.next;
      }

      return bobList.toString();
   }

   public String toString() {
      StringBuilder bobList = new StringBuilder();
      if (occupancy > 0) {
         // set current as the header's next node's next
         Node current = header.next.next;

         bobList.append(header.next);
         while (current != boundary) {

            bobList.append(String.format("%n%s", current.word));

            current = current.next;
         }
      }

      return bobList.toString();
   }

   @Override
   public boolean hasNext() {
      return iteratorCurrent != boundary;
   }

   @Override
   public PartitionedWordSet.Node next() {
      iteratorCurrent = iteratorCurrent.next;
      return iteratorCurrent.prev;
   }

   // Pray that iterator is called only once at a time
   @Override
   public Iterator<PartitionedWordSet.Node> iterator() {
      iteratorCurrent = header.next;
      return this;
   }
}
