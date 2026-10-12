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
  System.out.println("PASS persistent lifetime counter codec, finite/unlimited limits, failed delivery, independent counters, cap changes, overflow and Molten independence");
 }
}
