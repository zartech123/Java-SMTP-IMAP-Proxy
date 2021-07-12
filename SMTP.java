import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.email.Recipient;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.ServerConfig;
import org.simplejavamail.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.internal.mailsender.ProxyConfig;

public class SMTP {

	private ConnectionImpl connDb = new ConnectionImpl();
	private Properties propXML = new Properties();
	private static Logger logger;
	private String host = "";
	private String userName = "";
	private String password = "";
	private int port = 0;
	private String proxyHost = "";
	private String proxyUserName = "";
	private String proxyPassword = "";
	private int proxyPort = 0;
	private String security = "";
	private String proxy = "";
	private String recipient;
	private String content;
	private String subject;
	
	public static void main(String[] args) 
	{
    	System.setProperty("log4j.configurationFile",  System.getProperty("user.dir")+"/config/log4j2.xml");
    	logger = LogManager.getLogger(SMTP.class);
		SMTP smtp = new SMTP();
		// TODO Auto-generated method stub
	}
	

	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
	}
	
	public void setContent(String content)
	{
		this.content = content;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public void sendSMTP()
	{
		try
		{
			propXML.load(new FileInputStream(System.getProperty("user.dir")+"/conf/properties2.prop"));
	
//			PropertyConfigurator.configure(System.getProperty("user.dir")+"/config/log4j2.properties");    			
			/*while(connDb.isConnected()==false)
			{	
				connDb.setProperties(propXML);
				connDb.setUrl();
				connDb.setConnection();
			}*/				
			this.security=this.propXML.getProperty("smtp.security").trim();
			this.host=this.propXML.getProperty("smtp.host").trim();
			this.userName=this.propXML.getProperty("smtp.userName").trim();
			this.password=this.propXML.getProperty("smtp.password").trim();
			this.port = Integer.parseInt(this.propXML.getProperty("smtp.port").trim());
			this.proxyHost=this.propXML.getProperty("smtp.proxy-host").trim();
			this.proxyUserName=this.propXML.getProperty("smtp.proxy-UserName").trim();
			this.proxyPassword=this.propXML.getProperty("smtp.proxy-password").trim();
			this.proxyPort = Integer.parseInt(this.propXML.getProperty("smtp.proxy-port").trim());
			this.proxy = this.propXML.getProperty("smtp.proxy");
			
			if(this.recipient.trim().compareTo("")!=0)
			{	
				String[] recipients = new String[this.recipient.split(",").length];
				recipients = this.recipient.split(",");
//				Recipient r[] = new Recipient[this.recipient.split(",").length];
				List<Recipient> list = new ArrayList<>();
				
				for(int i=0;i<recipients.length;i++)
				{	
//					r[i] = new Recipient(recipients[i],recipients[i],Message.RecipientType.TO);
					list.add(new Recipient(recipients[i],recipients[i],Message.RecipientType.TO));
				}	

				Email email = EmailBuilder.startingBlank()
					    .from("WIBPUSH Administrator", this.userName)
	//				    .to(this.recipient, this.recipient)
					    .withRecipients(list)
					    .withSubject(this.subject)
					    .withHTMLText(this.content)
					    .buildEmail();			
				
			
				Mailer mailer;
		          if(this.proxy.compareTo("0")==0)
		          {	  
		        	  if(this.security.compareTo("tls")==0)
		        	  {	  
		        	  		mailer = MailerBuilder
				          .withSMTPServer(this.host, this.port, this.userName, this.password)
				          .withTransportStrategy(TransportStrategy.SMTP_TLS)
				          .withSessionTimeout(10 * 1000)
				          .clearEmailAddressCriteria() // turns off email validation
				          .withDebugLogging(true)
				          .buildMailer();
		        			mailer.sendMail(email);
		        	  }	
		        	  else if(this.security.compareTo("ssl")==0)
		        	  {	  
		        	  		mailer = MailerBuilder
				          .withSMTPServer(this.host, this.port, this.userName, this.password)
				          .withTransportStrategy(TransportStrategy.SMTPS)
				          .withSessionTimeout(10 * 1000)
				          .clearEmailAddressCriteria() // turns off email validation
				          .withDebugLogging(true)
				          .buildMailer();
		        			mailer.sendMail(email);			
		        	  }	
		        	  else
		        	  {
		        	  		mailer = MailerBuilder
				          .withSMTPServer(this.host, this.port, this.userName, this.password)
				          .withTransportStrategy(TransportStrategy.SMTP)
				          .withSessionTimeout(10 * 1000)
				          .clearEmailAddressCriteria() // turns off email validation
				          .withDebugLogging(true)
				          .buildMailer();
		        			mailer.sendMail(email);				        		  
		        	  }
				}       
	
	  			if(this.proxy.compareTo("1")==0)
	            {	  
				        	  
				    if(this.security.compareTo("tls")==0)
				    {	  
				    	  	mailer = MailerBuilder
					       .withSMTPServer(this.host, this.port, this.userName, this.password)
					       .withTransportStrategy(TransportStrategy.SMTP_TLS)
					       .withProxy(this.proxyHost, this.proxyPort, this.proxyUserName, this.proxyPassword)
					       .withSessionTimeout(10 * 1000)
					       .clearEmailAddressCriteria() // turns off email validation
					       .withDebugLogging(true)
					       .buildMailer();
				    	  	mailer.sendMail(email);
				     }
				     else if(this.security.compareTo("ssl")==0)
				     {
							mailer = MailerBuilder
					        .withSMTPServer(this.host, this.port, this.userName, this.password)
					        .withTransportStrategy(TransportStrategy.SMTPS)
					        .withProxy(this.proxyHost, this.proxyPort, this.proxyUserName, this.proxyPassword)
					        .withSessionTimeout(10 * 1000)
					        .clearEmailAddressCriteria() // turns off email validation
					        .withDebugLogging(true)
					        .buildMailer();
							mailer.sendMail(email);			
		        	 }
				     else
				     {
							mailer = MailerBuilder
					       .withSMTPServer(this.host, this.port, this.userName, this.password)
					       .withTransportStrategy(TransportStrategy.SMTP)
					       .withProxy(this.proxyHost, this.proxyPort, this.proxyUserName, this.proxyPassword)
					       .withSessionTimeout(10 * 1000)
					       .clearEmailAddressCriteria() // turns off email validation
					       .withDebugLogging(true)
					       .buildMailer();
							mailer.sendMail(email);			
		        	  }
		          }	
							          
	  			if(this.proxy.compareTo("2")==0)
	            {	  
				        	  
				    if(this.security.compareTo("tls")==0)
				    {	  
				    	  	mailer = MailerBuilder
					       .withSMTPServer(this.host, this.port, this.userName, this.password)
					       .withTransportStrategy(TransportStrategy.SMTP_TLS)
					       .withProxyHost(this.proxyHost)
					       .withProxyPort(this.proxyPort)
					       .withSessionTimeout(10 * 1000)
					       .clearEmailAddressCriteria() // turns off email validation
					       .withDebugLogging(true)
					       .buildMailer();
				    	  	mailer.sendMail(email);
				     }
				     else if(this.security.compareTo("ssl")==0)
				     {
							mailer = MailerBuilder
					        .withSMTPServer(this.host, this.port, this.userName, this.password)
					        .withTransportStrategy(TransportStrategy.SMTPS)
					        .withProxyHost(this.proxyHost)
					        .withProxyPort(this.proxyPort)
					        .withSessionTimeout(10 * 1000)
					        .clearEmailAddressCriteria() // turns off email validation
					        .withDebugLogging(true)
					        .buildMailer();
							mailer.sendMail(email);			
		        	 }
				     else
				     {
							mailer = MailerBuilder
					       .withSMTPServer(this.host, this.port, this.userName, this.password)
					       .withTransportStrategy(TransportStrategy.SMTP)
					        .withProxyHost(this.proxyHost)
					        .withProxyPort(this.proxyPort)
					       .withSessionTimeout(10 * 1000)
					       .clearEmailAddressCriteria() // turns off email validation
					       .withDebugLogging(true)
					       .buildMailer();
							mailer.sendMail(email);			
		        	  }
		          }	
			}	
		}	
		catch (FileNotFoundException e) 
		{
			logger.error(this.getClass().getName()+" "+e.getMessage());
		}	
		catch (IOException e) 
		{
			logger.error(this.getClass().getName()+" "+e.getMessage());
		}	
	}
	
}
