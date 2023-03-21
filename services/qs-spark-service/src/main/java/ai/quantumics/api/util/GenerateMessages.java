/*
 * Copyright (c) 2020. Quantumspark.ai, http://quantumspark.ai.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 *//*


   package ai.quantumspark.api.util;

   import ai.quantumspark.api.vo.QsSocketAck;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.messaging.simp.SimpMessageSendingOperations;
   import org.springframework.stereotype.Component;

   @Component
   public class GenerateMessages {

     String enggDestination = "/topic/engg";
     String runJobDestination = "/topic/runjob";
     String fileUploadDestination = "/topic/upload";
     String fileDataDestination = "/topic/filedata";

     @Autowired private SimpMessageSendingOperations messageTemplate;

     public void generateEnggMessages(final String msg) {
       messageTemplate.convertAndSend(enggDestination, new QsSocketAck(msg));
     }

     public void generateEnggMessages(final String user, final String msg) {
       messageTemplate.convertAndSendToUser(user, enggDestination, new QsSocketAck(msg));
     }

     public void generateFileDataMessages(final String msg) {
       messageTemplate.convertAndSend(fileDataDestination, new QsSocketAck(msg));
     }

     public void generateFileDataMessages(final String user, final String msg) {
       messageTemplate.convertAndSendToUser(user, fileDataDestination, new QsSocketAck(msg));
     }

     public void generateFileUploadMessages(final String msg) {
       messageTemplate.convertAndSend(fileUploadDestination, new QsSocketAck(msg));
     }

     public void generateFileUploadMessages(final String user, final String msg) {
       messageTemplate.convertAndSendToUser(user, fileUploadDestination, new QsSocketAck(msg));
     }

     public void generateRunJobMessages(final String msg) {
       messageTemplate.convertAndSend(runJobDestination, new QsSocketAck(msg));
     }

     public void generateRunJobMessages(final String user, final String msg) {
       messageTemplate.convertAndSendToUser(user, runJobDestination, new QsSocketAck(msg));
     }
   }
   */
