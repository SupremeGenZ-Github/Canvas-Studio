package studio.canvas;

public enum CanvasTier {
 BLANK("Blank Canvas", "Get Item locked: requires Molten or Infinity Canvas"),
 MOLTEN("Molten Canvas", "Get Item: one successful use"),
 INFINITY("Infinity Canvas", "Get Item: unlimited successful uses");
 public final String label, availability;
 CanvasTier(String label,String availability){this.label=label;this.availability=availability;}
 public boolean canExchange(){return this!=BLANK;}
}
