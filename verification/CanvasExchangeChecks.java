import studio.canvas.*;
import java.util.*;

public class CanvasExchangeChecks {
 static void check(boolean value,String why){if(!value)throw new AssertionError(why);}
 static byte[] drawing(){byte[] p=new byte[SavePainting.PIXELS];Arrays.fill(p,(byte)34);Arrays.fill(p,0,8,(byte)172);return p;}
 static boolean request(ExchangeSession<Object> s,Object held,boolean off,CanvasTier tier,int count,UUID token,byte[] pixels,boolean valid,long now){
  return CanvasExchange.authorize(s,held,off,tier,count,token,pixels,valid,now);
 }
 public static void main(String[] args){
  for(boolean off:new boolean[]{false,true}){
   Object stack=new Object();byte[] p=drawing();
   var blank=new ExchangeSession<>(stack,off,CanvasTier.BLANK,0);
   check(!request(blank,stack,off,CanvasTier.BLANK,1,blank.token(),p,true,1),"Blank locked");
   var molder=new ExchangeSession<>(stack,off,CanvasTier.MOLTEN,0);UUID token=molder.token();
   check(!request(molder,stack,off,CanvasTier.MOLTEN,1,token,p,false,1),"Invalid item rejected");
   check(!request(molder,stack,off,CanvasTier.MOLTEN,1,token,new byte[1],true,1),"Invalid drawing rejected");
   check(!request(molder,new Object(),off,CanvasTier.MOLTEN,1,token,p,true,1),"Held stack switch rejected");
   check(!request(molder,stack,!off,CanvasTier.MOLTEN,1,token,p,true,1),"Wrong hand rejected");
   check(!request(molder,stack,off,CanvasTier.INFINITY,1,token,p,true,1),"Tier spoof rejected");
   check(!request(molder,stack,off,CanvasTier.MOLTEN,2,token,p,true,1),"Stack count rejected");
   check(!request(molder,stack,off,CanvasTier.MOLTEN,1,UUID.randomUUID(),p,true,1),"Wrong token rejected");
   check(token.equals(molder.token()),"Rejected requests retain capability");
   // Cancellation and failed matching send no exchange; opening alone leaves the token valid.
   check(molder.matches(stack,off,CanvasTier.MOLTEN,2),"Cancellation retains held canvas");
   check(request(molder,stack,off,CanvasTier.MOLTEN,1,token,p,true,2),"One successful Molten exchange");
   check(molder.token()==null,"Molten capability consumed");
   check(!request(molder,stack,off,CanvasTier.MOLTEN,1,token,p,true,2),"Molten duplicate rejected");
   var infinity=new ExchangeSession<>(stack,off,CanvasTier.INFINITY,0);
   UUID first=infinity.token();int rewards=0;
   for(int i=0;i<1000;i++){
    UUID current=infinity.token();
    if(request(infinity,stack,off,CanvasTier.INFINITY,1,current,p,true,i))rewards++;
    check(!request(infinity,stack,off,CanvasTier.INFINITY,1,current,p,true,i),"Immediate duplicate rejected");
    check(!request(infinity,stack,off,CanvasTier.INFINITY,1,first,p,true,i),"Old replay rejected");
    check(infinity.matches(stack,off,CanvasTier.INFINITY,i),"Infinity held stack retained");
   }
   check(rewards==1000,"Exactly one authorization per Infinity request");
   UUID last=infinity.token();check(!request(infinity,stack,off,CanvasTier.INFINITY,1,last,p,true,6001),"Expired session rejected");
   check(last.equals(infinity.token()),"Expiry does not consume canvas");
   check(!request(null,stack,off,CanvasTier.INFINITY,1,last,p,true,1),"Missing session rejected");
  }
  System.out.println("PASS server authorization: both hands, Blank locked, Molten one use, Infinity 1000 uses, duplicate/replay, wrong hand/stack/tier/count/token, expiry, invalid drawing/item and cancellation retention.");
 }
}
