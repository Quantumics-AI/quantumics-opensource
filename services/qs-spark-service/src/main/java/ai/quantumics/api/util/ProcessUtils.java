package ai.quantumics.api.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.Instant;

public class ProcessUtils {
  
  public static String runCommand() throws Exception{
    
    long startTime = System.currentTimeMillis();
    
    List<String> command = new ArrayList<>();
    command.add("CMD");
    command.add("/C");
    command.add("c://murali//python38//python");
    command.add("c://murali//PII_detection.py");
    
    ProcessBuilder builder = new ProcessBuilder(command);
    builder.redirectErrorStream(true);
    
    System.out.println(builder.command());
    Process process = builder.start();
    
    StringBuilder out = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      
      String line = null;
      while ((line = reader.readLine()) != null) {
        out.append(line);
        out.append("\n");
      }
      System.out.println(out);
    }
    
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      
      String line = null;
      while ((line = reader.readLine()) != null) {
        out.append(line);
        out.append("\n");
      }
      System.out.println(out);
    }
    
    process.waitFor();
    
    System.out.println("Elapsed time: " +(System.currentTimeMillis() - startTime) + "msecs");
    
    return null;
  }

  public static void main(String[] args) {
    try {
      runCommand();
    }catch(Exception ioe) {
      ioe.printStackTrace();
    }
    
  }

}
