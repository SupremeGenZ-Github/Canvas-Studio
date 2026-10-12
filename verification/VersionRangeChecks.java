import java.nio.file.*;
import java.util.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

/** Exercises the loader's Maven version-range semantics including prereleases. */
public class VersionRangeChecks {
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  var nf=VersionRange.createFromVersionSpec("[26.3-alpha,26.4-alpha)");
  var fml=VersionRange.createFromVersionSpec("[12,)");
  for(String v:List.of("26.3.0.0-beta","26.3.0.56-beta","26.3.0.57","26.3.0.88","26.3.0.89","26.3.0.999-beta","26.3.0.999"))check(nf.containsVersion(new DefaultArtifactVersion(v)),"Accept 26.3 "+v);
  for(String v:List.of("26.2.0.999","26.4-alpha","26.4.0.0-alpha","26.4.0.0-beta","26.4.0.1","27.0"))check(!nf.containsVersion(new DefaultArtifactVersion(v)),"Reject other family "+v);
  check(fml.containsVersion(new DefaultArtifactVersion("12.0.8")),"FML 12 accepted");
  check(fml.containsVersion(new DefaultArtifactVersion("12.0.0")),"Future FML metadata accepted; APIs still require verification");
  var p=Path.of("verification/neoforge-26.3-versions.txt");int count=0;
  if(Files.exists(p))for(String line:Files.readAllLines(p)){
   if(line.isBlank())continue;String[] v=line.split(" ");
   check(nf.containsVersion(new DefaultArtifactVersion(v[0])),"Published NeoForge accepted "+v[0]);
   check(fml.containsVersion(new DefaultArtifactVersion(v[1])),"Published loader accepted "+v[1]);count++;
  }
  System.out.println("PASS version ranges: beta/stable/future 26.3 accepted, 26.2/26.4 rejected; published builds checked="+count);
 }
}
