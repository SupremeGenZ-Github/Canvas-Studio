import studio.canvas.*;
import com.mojang.serialization.JsonOps;
public class UsageChecks {
 static void check(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  for(int cap:new int[]{0,1,5,20}){
   long a=0,b=0;
   for(int n=0;n<100;n++){
    check(CanvasUsage.allowed(CanvasTier.MOLTEN,1000,cap));
    check(CanvasUsage.afterDelivery(a,false)==a);
    if(CanvasUsage.allowed(CanvasTier.INFINITY,a,cap))a=CanvasUsage.afterDelivery(a,true);
   }
   check(a==(cap==0?100:cap));check(b==0);
   check(CanvasUsage.allowed(CanvasTier.INFINITY,a,0));
  }
  check(!CanvasUsage.allowed(CanvasTier.BLANK,0,0));
  check(!CanvasUsage.allowed(CanvasTier.INFINITY,20,5));
  check(CanvasUsage.afterDelivery(Long.MAX_VALUE,true)==Long.MAX_VALUE);
  var codec=CanvasUsage.TYPE.codec();var json=codec.encodeStart(JsonOps.INSTANCE,20L).getOrThrow();
  check(codec.parse(JsonOps.INSTANCE,json).getOrThrow()==20L);
  check(codec.parse(JsonOps.INSTANCE,new com.google.gson.JsonPrimitive(-1)).error().isPresent());
  for(boolean off:new boolean[]{false,true}){
   Object held=new Object();var session=new ExchangeSession<>(held,off,CanvasTier.INFINITY,0);long[] uses={0};int[] rewards={0};byte[] drawing=new byte[16384];java.util.Arrays.fill(drawing,(byte)116);
   for(int i=0;i<12;i++){var token=session.token();var result=CanvasExchange.exchange(session,held,off,CanvasTier.INFINITY,1,token,drawing,true,i,uses[0],5,()->{rewards[0]++;return true;},n->uses[0]=n);
    check(result==(i<5?CanvasExchange.Result.DELIVERED:CanvasExchange.Result.LIMIT_REACHED));
    var replay=CanvasExchange.exchange(session,held,off,CanvasTier.INFINITY,1,token,drawing,true,i,uses[0],5,()->{throw new AssertionError("Duplicate delivered");},n->uses[0]=n);check(replay!=CanvasExchange.Result.DELIVERED);
   }check(uses[0]==5&&rewards[0]==5);
   var failed=CanvasExchange.exchange(session,held,off,CanvasTier.INFINITY,1,session.token(),drawing,true,20,uses[0],0,()->false,n->uses[0]=n);check(failed==CanvasExchange.Result.DELIVERY_FAILED&&uses[0]==5);
   var molten=new ExchangeSession<>(held,off,CanvasTier.MOLTEN,0);check(CanvasExchange.exchange(molten,held,off,CanvasTier.MOLTEN,1,molten.token(),drawing,true,1,999,1,()->true,n->{throw new AssertionError("Molten counted");})==CanvasExchange.Result.DELIVERED);
  }
  System.out.println("PASS persistent lifetime counter codec, finite/unlimited limits, failed delivery, independent counters, cap changes, overflow and Molten independence");
 }
}
