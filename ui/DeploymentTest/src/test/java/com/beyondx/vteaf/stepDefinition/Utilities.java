package com.beyondx.vteaf.stepDefinition;

import com.beyondx.vteaf.Hooks.Hooks;
import com.beyondx.vteaf.Runner.Test_Runner;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import cucumber.api.java.en.When;
import cucumber.runtime.CucumberException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utilities {

    static Logger log = Logger.getLogger(Utilities.class.getName());

    public static void MailSend(String mailId,String ccMail){
        try {
            String host = "smtp.office365.com";
//            String host = "smtp.gmail.com";
            //change accordingly
//            final String user = "vishalcool.vaitheeswaran@gmail.com";
            final String user = "vijaysowmy@hotmail.com";
            //Google Password
//            final String password = "qovtbndwdntziiaa";
            // Outlook Password
            final String password = "euuaumxshqcpogub";
//            HxFPAQQFuP56Zjg5n7Mx
//            git clone https://vishalvrk97@bitbucket.org/vishalvrk97/deploymenttestpoc.git
            //Get the session object
            Properties props = new Properties();
//            props.put("mail.smtp.host", host);
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.port", "587");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.ssl.trust", host);

            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
//            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");


            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailId));
            message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccMail));

            String lDate = LocalDate.now().toString();
            String lTime = LocalTime.now().toString();
            Collection<String> values = Hooks.scenarioStatus.values();
            String flag;
            int failCount = 0;
            int passCount = 0;
            int skipCount = 0;
            for (String x : values) {
                if (x.equalsIgnoreCase("Failed")) {
                    failCount++;
                } else if (x.equalsIgnoreCase("Passed")) {
                    passCount++;
                } else {
                    skipCount++;
                }
            }

            System.out.println(failCount);
            System.out.println(passCount);
            System.out.println(skipCount);

            if (values.contains("FAILED") || values.contains("SKIPPED")) {
                flag = "Build Failed";
            } else {
                flag = "Build Passed";
            }

            String subject = "Deployment Automation Test Status: <"+flag.toUpperCase()+">";
            message.setSubject(subject);
            MimeMultipart multipart = new MimeMultipart("related");

            // Create object to add multimedia type content
            BodyPart messageBodyPart1 = new MimeBodyPart();
            StringBuffer ScenarioTable = new StringBuffer();
            LinkedHashMap<String, String> mp = new LinkedHashMap();
            mp.putAll(Hooks.scenarioStatus);
            Set<Map.Entry<String, String>> entrySet = mp.entrySet();
            Iterator signature = entrySet.iterator();
            Map.Entry entry;

            while(signature.hasNext()) {
                entry = (Map.Entry)signature.next();
                if (((String)entry.getValue()).equalsIgnoreCase("passed")) {
                    ScenarioTable.append("<TR ALIGN='CENTER' bgcolor ='#f2f2f2'><TD>" + (String)entry.getKey() + "</TD><TD bgcolor= '#94B49F' >" + (String)entry.getValue() + "</TD> " + "</TR>");
                } else {
                    ScenarioTable.append("<TR ALIGN='CENTER' bgcolor ='#f2f2f2'><TD>" + (String)entry.getKey() + "</TD><TD bgcolor= '#D82148' >" + (String)entry.getValue() + "</TD> " + "</TR>");
                }
            }

            File f = new File(System.getProperty("user.dir") + "/src/test/java/com/TestResults/cucumber-report/cucumber_1.json");
            List listSteps = (List) JsonPath.read(f, "$..steps[*].result.status", (com.jayway.jsonpath.Predicate[]) new Predicate[0]);
            int stepPassed = 0;
            int stepFailed = 0;
            int stepSkipped = 0;
            Iterator var26 = listSteps.iterator();

            String x;
            while(var26.hasNext()) {
                x = (String)var26.next();
                if (x.toLowerCase().contains("pass")) {
                    ++stepPassed;
                } else if (x.toLowerCase().contains("fail")) {
                    ++stepFailed;
                } else if (x.toLowerCase().contains("skip")) {
                    ++stepSkipped;
                }
            }
            // Set the body of email
            String htmlText = "<img src=\"cid:image\" width=83 height= auto> <BR><H2>Script Execution Summary | Email Report </H2>"
                    +"<BR><H4>Test Environment: "+ Test_Runner.Environment+"</H4></BR>"
                    + "<TABLE WIDTH='60%' CELLPADDING='4' CELLSPACING='1'>"
                    + "<TR> <TH bgcolor = '#EFFFFD' COLSPAN='7'><H4>Test Execution Summary</H4></TH></TR><TR><TH>Total Test Cases: </TH><TH>Total Steps Passed: </TH><TH>Total Steps Failed: </TH><TH>Total Steps Skipped: </TH><TH>Total TCs Passed: </TH><TH>Total TCs Failed: </TH><TH>Total TCs Skipped: </TH>"
                    + "</TR>" + "<TR ALIGN='CENTER' bgcolor ='#6bbbc7'><TD>" + values.size()
                    + "</TD><TD bgcolor= '#94B49F' >" + stepPassed + "</TD><TD bgcolor= '#D82148' >" + stepFailed+"</TD><TD bgcolor= '#6bbbc7' >" + stepSkipped +"</TD><TD bgcolor= '#94B49F' >" + passCount + "</TD><TD bgcolor= '#D82148' >" + failCount
                    + "</TD><TD bgcolor= '#6bbbc7' >" + skipCount + "</TD>" + "</TR>"
                    + "</TABLE> "+"\n"
                    + "\n"
                    +"<BR/>"
                    +"<BR/>"
                    +"<H3>Test Scenarios executed:</H3>"+
                    "<TABLE WIDTH='60%' CELLPADDING='4' CELLSPACING='1'>"+"<TR ALIGN='CENTER' bgcolor ='#DFF6FF'><TD color: 'white'> <B> Scenario Name </B></TD><TD bgcolor ='#DFF6FF' color: 'white'><B> Result </B></TD></TR>"+
                    ScenarioTable+"</TABLE>"+"\n"+"\n"+

                    "Thanks, " + "\n" +
                    "\n"
                    + "Quantumics QA";

            messageBodyPart1.setContent(htmlText, "text/html");
            multipart.addBodyPart(messageBodyPart1);
            messageBodyPart1 = new MimeBodyPart();
            DataSource fds = new FileDataSource("src/test/java/com/beyondx/vteaf/resources/logo.png");

            messageBodyPart1.setDataHandler(new DataHandler(fds));
            messageBodyPart1.setHeader("Content-ID", "<image>");
            message.setContent(multipart);

            // Create another object to add another content
            MimeBodyPart messageBodyPart3 = new MimeBodyPart();
            String fUI = "TestExecution_Report.zip";
            multipart.addBodyPart(messageBodyPart1);
            if (java.nio.file.Files.exists(Paths.get(fUI), LinkOption.NOFOLLOW_LINKS)) {
                DataSource source1 = new FileDataSource(fUI);
                messageBodyPart3.setDataHandler(new DataHandler(source1));
                messageBodyPart3.setFileName(fUI);
                multipart.addBodyPart(messageBodyPart3);

            }
            message.setContent(multipart);
            Transport.send(message);
            System.out.println("=====Reports Sent through Email from =====");

        }
        catch (Exception e){
           System.out.println(e);
           e.printStackTrace();
        }

    }

    public static void reportstoZipFile(String srcfolder, String desFN) {
        try {
            // Use the following paths for windows
            String folderToZip = System.getProperty("user.dir") + "/" + srcfolder;
            String zipName = System.getProperty("user.dir") + "/" + desFN + ".zip";
            File f = new File(folderToZip);
            if (f.isDirectory()) {
                final Path sourceFolderPath = Paths.get(folderToZip);
                Path zipPath = Paths.get(zipName);
                final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
                Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("Zip File Created "+desFN);
                zos.close();
            }
        } catch (Exception e) {
            throw new CucumberException(e);
        }
    }

    public static void deleteZipFiles(String desFN) {
        try {
            String zipName = System.getProperty("user.dir") + "/" + desFN + ".zip";
            String folderName = System.getProperty("user.dir") + "/" + desFN + ".zip";

            FileUtils.forceDelete(new File(zipName));
            FileUtils.forceDelete(new File(folderName));
            System.out.println("Zip folder found to delete" + desFN);
        } catch (Exception e) {
             System.out.println("No zip folder found to delete" + desFN);
        }
    }

   @When("I test Email")
    public static void i_test_email(){
        MailSend("vishal.vaitheeswaran@quantumics.ai","");
    }

    public static void main(String[] args){
//        reportstoZipFile("src/test/java/com/TestResults/execution_reports","TestExecution_ExtentReports");
//        System.out.println("Zip created");
//        MailSend("vishal.vaitheeswaran@quantumics.ai,ganesh.kumar_testleaf@quantumics.ai","");
//        deleteZipFiles("TestExecution_ExtentReports");
//        System.out.println("Zip Deleted");
                MailSend("vishal.vaitheeswaran@quantumics.ai","");
    }

}
