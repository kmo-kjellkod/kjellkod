package doff.email;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class GMailSend
{
    private final String emailPort = "587";// gmail's smtp port
    private final String smtpAuth = "true";
    private final String starttls = "true";
    private final String emailHost = "smtp.gmail.com";

    private String fromEmail;
    private String fromPassword;
    private List<String> toEmailList;
    private String emailSubject;
    private String emailBody;

    private Properties emailProperties;
    private Session mailSession;
    private MimeMessage emailMessage;
    private Multipart emailMultipart;

    public GMailSend()
    {
    }

    public GMailSend(String fromEmail, String fromPassword, List<String> toEmailList, String emailSubject, String emailBody)
    {
        this.fromEmail = fromEmail;
        this.fromPassword = fromPassword;
        this.toEmailList = toEmailList;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;

        emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", emailPort);
        emailProperties.put("mail.smtp.auth", smtpAuth);
        emailProperties.put("mail.smtp.starttls.enable", starttls);
        Log.d("doff-email", "Mail server properties set: " + emailProperties.toString());
    }

    public MimeMessage createEmailMessage() throws AddressException, MessagingException, UnsupportedEncodingException
    {
        //mailSession = Session.getDefaultInstance(emailProperties, null);
        mailSession = Session.getInstance(emailProperties, null);
        mailSession.setDebug(true);
        emailMessage = new MimeMessage(mailSession);

        emailMessage.setFrom(new InternetAddress(fromEmail, fromEmail));
        for (String toEmail : toEmailList)
        {
            Log.d("doff-email", "toEmail: " + toEmail);
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        }

        emailMessage.setSubject(emailSubject);
        //emailMessage.setContent(emailBody, "text/html");// for a html email
        emailMessage.setText(emailBody);// for a text email
        Log.d("doff-email", "Email Message created.");

        return emailMessage;
    }

    public void sendEmail() throws AddressException, MessagingException
    {
        if ( emailMultipart != null )
        {
            emailMessage.setContent(emailMultipart);
        }

        Log.d("doff-email", "fromEmail=" + fromEmail + ", fromPassword=" + fromPassword);

        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromEmail, fromPassword);

        Log.d("doff-email", "allrecipients: " + emailMessage.getAllRecipients());

        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();

        Log.d("doff-email", "Email sent successfully.");
    }

    public void addAttachment(String filename) throws Exception {

        if (emailMultipart == null )
        {
            emailMultipart = new MimeMultipart();

            // There is something wrong with MailCap, javamail can not find a
            // handler for the multipart/mixed part, so this bit needs to be added.
//            MailcapCommandMap mc = (MailcapCommandMap) CommandMap .getDefaultCommandMap();
//            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
//            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
//            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
//            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
//            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
//            CommandMap.setDefaultCommandMap(mc);

        }

        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        emailMultipart.addBodyPart(messageBodyPart);
        Log.d("doff-email", "addAttachment: " + filename);
    }

    @Override
    public String toString()
    {
        String s = "";
        s += emailProperties.toString();
        s += ", " + fromEmail;
        s += ", " + fromPassword;

        return s;
    }


    public static void sendWithNoAttachment(final String fromEmail, final String fromPassword, final String toEmail, final String subject, final String body) {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    List<String> rec = new ArrayList<String>();
                    rec.add(toEmail);
                    GMailSend gmail = new GMailSend(fromEmail, fromPassword, rec, subject, body);
                    gmail.createEmailMessage();
                    gmail.sendEmail();
                } catch (AddressException ae)
                {
                    Log.e("doff-email", "AddressException: " + ae.toString());
                } catch (UnsupportedEncodingException uee)
                {
                    Log.e("doff-email", "UnsupportedEncodingException: " + uee.toString());
                } catch (MessagingException me)
                {
                    Log.e("doff-email", "MessagingException: " + me.toString());
                } catch (Exception e)
                {
                    Log.e("doff-email", "Exception: " + e.toString());
                }
            }
        }).start();
        ;
    }

    public static void testWithAttachment() {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    List<String> rec = new ArrayList<String>();
                    rec.add("kjell.mardensjo@oru.se");
                    GMailSend gmail = new GMailSend("kjellkod.kurs@gmail.com", "kmo123st", rec, "Ämne", "Kropp");
                    gmail.createEmailMessage();
                    gmail.addAttachment("/data/user/0/com.doffs.skorsten/files/pdftest.pdf");
                    gmail.addAttachment("/data/user/0/com.doffs.skorsten/files/position.txt");
                    gmail.sendEmail();
                } catch (AddressException ae)
                {
                    Log.e("doff-email", "AddressException: " + ae.toString());
                } catch (UnsupportedEncodingException uee)
                {
                    Log.e("doff-email", "UnsupportedEncodingException: " + uee.toString());
                } catch (MessagingException me)
                {
                    Log.e("doff-email", "MessagingException: " + me.toString());
                } catch (Exception e)
                {
                    Log.e("doff-email", "Exception: " + e.toString());
                }
            }
        }).start();
        ;
    }
    public static void testWithNoAttachment() {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    List<String> rec = new ArrayList<String>();
                    rec.add("kjell.mardensjo@oru.se");
                    GMailSend gmail = new GMailSend("kjellkod.kurs@gmail.com", "kmo123st", rec, "Ämne", "Kropp");
                    gmail.createEmailMessage();
                    gmail.sendEmail();
                } catch (AddressException ae)
                {
                    Log.e("doff-email", "AddressException: " + ae.toString());
                } catch (UnsupportedEncodingException uee)
                {
                    Log.e("doff-email", "UnsupportedEncodingException: " + uee.toString());
                } catch (MessagingException me)
                {
                    Log.e("doff-email", "MessagingException: " + me.toString());
                } catch (Exception e)
                {
                    Log.e("doff-email", "Exception: " + e.toString());
                }
            }
        }).start();
        ;
    }
}
