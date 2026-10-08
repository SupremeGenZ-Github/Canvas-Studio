package studio.canvas;

import java.util.UUID;

/** Server-owned capability, bound to the exact held stack and hand. Thread confined. */
public final class ExchangeSession<T> {
 private final T stack;
 private final boolean offHand;
 private final CanvasTier tier;
 private final long expires;
 private UUID token=UUID.randomUUID();
 public ExchangeSession(T stack,boolean offHand,CanvasTier tier,long now){
  this.stack=stack;this.offHand=offHand;this.tier=tier;this.expires=now+6000;
 }
 public UUID token(){return token;}
 public boolean matches(T held,boolean off,CanvasTier actual,long now){
  return held==stack&&off==offHand&&actual==tier&&now<=expires;
 }
 /** Call only after drawing, registry ID and stack count have passed validation. */
 public boolean claim(T held,boolean off,CanvasTier actual,UUID submitted,long now){
  if(!matches(held,off,actual,now)||!tier.canExchange()||token==null||!token.equals(submitted))return false;
  // Rotate before reward mutation: a duplicate packet can never reuse this capability.
  token=tier==CanvasTier.INFINITY?UUID.randomUUID():null;
  return true;
 }
}
