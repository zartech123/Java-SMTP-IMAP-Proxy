import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class IMAP {
	
	private ConnectionImpl connDb = new ConnectionImpl();
	private Properties propXML = new Properties();
	private static Logger logger;
	private String host = "";
	private String userName = "";
	private String password = "";
	private int port = 0;
	private String proxyHost = "";
	private String proxyUserName = "";
	private SMTP smtp;
	private String proxyPassword = "";
	private String proxyPort = "";
	private int proxy = 0;
	private int maxMessage = 0;
	private String sender = "";
	private String idUser = "";
	private String idUser2 = "1";
	private Object[][] results_inquiry = new Object[50][21];
	private Map <Integer, String> field_inquiry = new HashMap<Integer, String>();
	private Object[][] results_inquiry2 = new Object[10][2];
	private Map <Integer, String> field_inquiry2 = new HashMap<Integer, String>();
//	private Object[][] results_inquiry3 = new Object[1][1];
//	private Map <Integer, String> field_inquiry3 = new HashMap<Integer, String>();
	private int idCampaign = 0;
	private String nameCampaign = "";
	private int type = 0;
	private String totalRecipient = "";
	private String startDateCampaign = "";
	private String startTimeCampaign = "";
	private String email = "";
	private String name = "";
	private String path3 = "";
	private String tone_title = ""; 
	private int tone_duration = 0;
	private int sti = 0;
	private String sti_text = ""; 
	private String layer1_text = ""; 
	private String layer2_text = "";
	private String layer3_text = "";
	private String name_action = "";
	private String action_destination = ""; 
	private String sms_text = "";
	private String email_approval = "";
	private String approval = "";
	
	public IMAP()
	{
    	System.setProperty("log4j.configurationFile",  System.getProperty("user.dir")+"/config/log4j.xml");
    	logger = LogManager.getLogger(IMAP.class);
		try
		{
			propXML.load(new FileInputStream(System.getProperty("user.dir")+"/conf/properties.prop"));
			
//			PropertyConfigurator.configure(System.getProperty("user.dir")+"/config/log4j.properties");    			
			int k = 0;
			while(connDb.isConnected()==false && k<10)
			{	
				connDb.setProperties(propXML);
				connDb.setUrl();
				connDb.setConnection();
				Thread.sleep(5000);
				k = k + 1;
			}				
			this.path3=this.propXML.getProperty("notif.path_email").trim();
			this.host=this.propXML.getProperty("imap.host").trim();
			this.userName=this.propXML.getProperty("imap.userName").trim();
			this.password=this.propXML.getProperty("imap.password").trim();
			this.port = Integer.parseInt(this.propXML.getProperty("imap.port").trim());
			this.maxMessage = Integer.parseInt(this.propXML.getProperty("imap.max-message").trim());
			this.proxyHost=this.propXML.getProperty("imap.proxy-host").trim();
			this.proxyUserName=this.propXML.getProperty("imap.proxy-UserName").trim();
			this.proxyPassword=this.propXML.getProperty("imap.proxy-password").trim();
			this.proxyPort = this.propXML.getProperty("imap.proxy-port").trim();
			this.proxy = Integer.parseInt(this.propXML.getProperty("imap.proxy").trim());
			field_inquiry = new TreeMap<Integer, String>();
			field_inquiry.put(0, "secure_id_campaign");
			field_inquiry.put(1, "name_campaign");
			field_inquiry.put(2, "id_campaign");
			field_inquiry.put(3, "type");
			field_inquiry.put(4, "total_recipient");
			field_inquiry.put(5, "start_date_campaign");
			field_inquiry.put(6, "start_time_campaign");
			field_inquiry.put(7, "email");
			field_inquiry.put(8, "name");
			field_inquiry.put(9, "tone_title");
			field_inquiry.put(10, "tone_duration");
			field_inquiry.put(11, "sti");
			field_inquiry.put(12, "sti_text");
			field_inquiry.put(13, "layer1_text");
			field_inquiry.put(14, "layer2_text");
			field_inquiry.put(15, "layer3_text");
			field_inquiry.put(16, "id_action");
			field_inquiry.put(17, "action_destination");
			field_inquiry.put(18, "sms_text");
			field_inquiry.put(19, "approval");
			field_inquiry.put(20, "email_approval");

			field_inquiry2 = new TreeMap<Integer, String>();
			field_inquiry2.put(0, "email");
			field_inquiry2.put(1, "id_user");

//			field_inquiry3 = new TreeMap<Integer, String>();
//			field_inquiry3.put(0, "jumlah");

			if(connDb.isConnected())
			{	
				results_inquiry2=connDb.getQuery("SELECT email, id_user from user where active=1 and id_group=2", new Object[]{"",""}, field_inquiry2, new Object[]{},0);
				this.sender = "";
				if(connDb.getRowCount(0)>0)
				{    	
					for(int j=0;j<connDb.getRowCount(0);j++)
					{
						this.sender = this.sender+results_inquiry2[j][0].toString()+",";
						this.idUser = this.sender+results_inquiry2[j][1].toString()+",";
					}
				}	
							
				if(this.sender.trim().compareTo("")!=0)
				{	
					this.sender.substring(0, this.sender.length()-1);
					this.idUser.substring(0, this.idUser.length()-1);
					receiveMail();
				}	
			}	
		}	
		catch (InterruptedException e) 
		{
			logger.error(this.getClass().getName()+" "+e.getMessage());
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
	
	public void setSender(String sender)
	{
		this.sender = sender;		
	}
	
    public void receiveMail() 
    {
        try 
        {
            Properties prop = new Properties();
            if(this.proxy==1)
            {	
	            prop.setProperty("mail.imaps.proxy.host", this.proxyHost);
	            prop.setProperty("mail.imaps.proxy.port", this.proxyPort);
	            prop.setProperty("mail.imaps.proxy.user", this.proxyUserName);
	            prop.setProperty("mail.imaps.proxy.password", this.proxyPassword);
            }    

    		String[] type = {"Manual","Automatic"};
            Session eSession = Session.getInstance(prop);

            Store eStore = eSession.getStore("imaps");
            eStore.connect(this.host, this.userName, this.password);

            Folder eFolder = eStore.getFolder("Inbox");
            eFolder.open(Folder.READ_WRITE);
            Message messages[] = eFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			String[] senders = new String[this.sender.split(",").length];
			String[] idUsers = new String[this.idUser.split(",").length];
			senders = this.sender.split(",");
			idUsers = this.idUser.split(",");
            
			int k = 0;
			String subject = "";
			String[] subjects; 
			maxMessage = messages.length;
			if(maxMessage>0)	maxMessage=10;
			
            for (int i = 0; i < messages.length; i++) 
            {
                Message message = messages[i];

                k = 0;
    			for(int j=0;j<senders.length;j++)
    			{	
	                if(message.getFrom()[0].toString().toUpperCase().contains(senders[j].toUpperCase()))
	                {	
	                	this.idUser2 = idUsers[j];
	                	k = k + 1;
	                	break;
	                }
    			}    
    			if(k>0)
    			{	
    				subject = message.getSubject();
    				subject = subject.replaceAll("  ", " ");
    				if(subject.contains("is Approved") && subject.contains("Campaign "))
    				{
    					subjects = subject.split(" ");
    					if(subjects.length==4)
    					{	
	    					results_inquiry=connDb.getQuery("SELECT upper(md5(id_campaign)) as secure_id_campaign, upper(name_campaign) as name_campaign, id_campaign, TYPE, FORMAT(total_recipient,0) AS total_recipient, start_date_campaign, start_time_campaign, c.email, c.name, tone_title, tone_duration, sti, sti_text, layer1_text, layer2_text, layer3_text, name_action, action_destination, sms_text, a.name as approval, a.email as email_approval from campaign b, user c, user a, action d WHERE b.id_action = d.id_action and b.approved_by = a.id_user and b.created_by = c.id_user and id_state=2 limit 50", new Object[]{"","",0,0,"","","","","","",0,0,"","","","","","","","",""}, field_inquiry, new Object[]{},0);
	    					if(connDb.getRowCount(0)>0)
	    					{    	
	    						for(int j=0;j<connDb.getRowCount(0);j++)
	    						{
	    							if(subjects[1].toUpperCase().compareTo(results_inquiry[j][1].toString()+"|"+results_inquiry[j][0].toString())==0)
	    							{
	    								this.type = Integer.parseInt(results_inquiry[j][3].toString());
	    								if(this.type==1)
	    								{
	    									connDb.updateQuery("UPDATE campaign SET id_state=4, approved_date=now(), approved_by='"+this.idUser2+"' WHERE id_state<4 and id_campaign in (select distinct id_campaign from file) and id_campaign="+results_inquiry[j][2].toString(),new Object[]{});   									    									
	    									connDb.updateQuery("UPDATE campaign SET id_state=3, approved_date=now(), approved_by='"+this.idUser2+"' WHERE id_state<4 and id_campaign NOT in (select distinct id_campaign from file) and id_campaign="+results_inquiry[j][2].toString(),new Object[]{});   									    									
	    								}
	    								else
	    								{	
	    									connDb.updateQuery("UPDATE campaign SET id_state=4, approved_date=now(), approved_by='"+this.idUser2+"' WHERE id_state<4 and id_campaign="+results_inquiry[j][2].toString(),new Object[]{});   								
	    								}	
	    								String emailApprover2 = "";

	    								smtp = new SMTP();
	    								this.idCampaign = Integer.parseInt(results_inquiry[j][2].toString());
	    								this.nameCampaign = results_inquiry[j][1].toString();
	    								this.totalRecipient = results_inquiry[j][4].toString();
	    								this.startDateCampaign = results_inquiry[j][5].toString();
	    								this.startTimeCampaign = results_inquiry[j][6].toString();
	    								this.email = results_inquiry[j][7].toString();
	    								this.name = results_inquiry[j][8].toString();	    								
	    								this.tone_title = results_inquiry[i][9].toString();
	    								this.tone_duration = Integer.parseInt(results_inquiry[i][10].toString());
	    								this.sti = Integer.parseInt(results_inquiry[i][11].toString());
	    								this.sti_text = results_inquiry[i][12].toString();
	    								this.layer1_text = results_inquiry[i][13].toString();
	    								this.layer2_text = results_inquiry[i][14].toString();
	    								this.layer3_text = results_inquiry[i][15].toString();
	    								this.name_action = results_inquiry[i][16].toString();
	    								this.action_destination = results_inquiry[i][17].toString();
	    								this.sms_text = results_inquiry[i][18].toString();
	    								this.approval = results_inquiry[i][19].toString();
	    								this.email_approval = results_inquiry[i][20].toString();
	    								
	    								try
	    								{
	    			    					String content = "";
		    								BufferedReader reader = new BufferedReader(new FileReader(this.path3+"assets/approval_response.html"));
		    								StringBuilder sb = new StringBuilder();
		    						        String line = reader.readLine();
		    				
		    						        while (line != null) {
		    						            sb.append(line);
		    						            sb.append("\n");
		    						            line = reader.readLine();
		    						        }
		    						        content = sb.toString();

		    						        emailApprover2=this.sender+this.email;
	    							        smtp.setRecipient(emailApprover2);
	    							        smtp.setSubject("Approval Response for Campaign : "+this.nameCampaign);
		    									
	    									content = content.replaceAll("_admin_email", "dpwibpushapp@xl.co.id");
	    									content = content.replaceAll("_name_campaign", this.nameCampaign);
	    									content = content.replaceAll("_id_campaign", new Integer(this.idCampaign).toString());
	    									content = content.replaceAll("_email_approval", this.email_approval);
	    									content = content.replaceAll("_approval", this.approval);
	    									content = content.replaceAll("_email", this.email);
	    									content = content.replaceAll("_name", this.name);
	    									content = content.replaceAll("_type", type[this.type]);
	    									content = content.replaceAll("_total_recipient", this.totalRecipient).toString();
	    									content = content.replaceAll("_start_date_campaign", this.startDateCampaign);
	    									content = content.replaceAll("_start_time_campaign", this.startTimeCampaign);
	    									content = content.replaceAll("_tone_title", this.tone_title);
	    									if(this.tone_title.compareTo("")==0)
	    									{	
	    										content = content.replaceAll("_tone_duration", new Integer(this.tone_duration).toString());
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_tone_duration", "");
	    									}
	    									if(this.sti==1)
	    									{	
	    										content = content.replaceAll("_sti_text", this.sti_text);
	    									}	
	    									else
	    									{
	    										content = content.replaceAll("_sti_text", "");
	    									}
	    									content = content.replaceAll("_layer1_text", this.layer1_text);
	    									content = content.replaceAll("_layer2_text", this.layer2_text);
	    									content = content.replaceAll("_layer3_text", this.layer3_text);
	    									content = content.replaceAll("_id_action", this.name_action);
	    									if(this.name_action.compareTo("None")==0)
	    									{	
	    										content = content.replaceAll("_action_destination", this.action_destination);
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_action_destination", "");
	    									}
	    									if(this.name_action.compareTo("SMS")==1)
	    									{	
	    										content = content.replaceAll("_sms_text", this.sms_text);
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_sms_text", "");
	    									}
	    									smtp.setContent(content);
		    									
	    									smtp.sendSMTP();
	    								
	    								}	
		    							catch (FileNotFoundException e) 
		    							{
		    								logger.error(this.getClass().getName()+" "+e.getMessage());
		    							}	
		    							catch (IOException e) 
		    							{
		    								logger.error(this.getClass().getName()+" "+e.getMessage());
		    							}
	    								
	    								break;	
	    							}
	    						}
	    					}	
    					}	
    				}
    				else if(subject.contains("is Rejected") && subject.contains("Campaign "))
    				{
    					subjects = subject.split(" ");    					
    					if(subjects.length==4)
    					{	
	    					results_inquiry=connDb.getQuery("SELECT upper(md5(id_campaign)) as secure_id_campaign, upper(name_campaign) as name_campaign, id_campaign, TYPE, FORMAT(total_recipient,0) AS total_recipient, start_date_campaign, start_time_campaign, c.email, c.name, tone_title, tone_duration, sti, sti_text, layer1_text, layer2_text, layer3_text, name_action, action_destination, sms_text, a.name as approval, a.email as email_approval from campaign b, user c, user a, action d WHERE b.id_action = d.id_action and b.approved_by = a.id_user and b.created_by = c.id_user and id_state=2 limit 50", new Object[]{"","",0,0,"","","","","","",0,0,"","","","","","","","",""}, field_inquiry, new Object[]{},0);
	    					if(connDb.getRowCount(0)>0)
	    					{    	
	    						for(int j=0;j<connDb.getRowCount(0);j++)
	    						{
	    							if(subjects[1].toUpperCase().compareTo(results_inquiry[j][1].toString()+"|"+results_inquiry[j][0].toString())==0)
	    							{
	    								connDb.updateQuery("UPDATE campaign SET id_state=8, approved_date=now(), approved_by='"+this.idUser2+"' WHERE id_state<4 and id_campaign="+results_inquiry[j][2].toString(),new Object[]{});   								

	    								String emailApprover2 = "";

	    								smtp = new SMTP();
	    								this.idCampaign = Integer.parseInt(results_inquiry[j][2].toString());
	    								this.nameCampaign = results_inquiry[j][1].toString();
	    								this.type = Integer.parseInt(results_inquiry[j][3].toString());
	    								this.totalRecipient = results_inquiry[j][4].toString();
	    								this.startDateCampaign = results_inquiry[j][5].toString();
	    								this.startTimeCampaign = results_inquiry[j][6].toString();
	    								this.email = results_inquiry[j][7].toString();
	    								this.name = results_inquiry[j][8].toString();	    								
	    								this.tone_title = results_inquiry[i][9].toString();
	    								this.tone_duration = Integer.parseInt(results_inquiry[i][10].toString());
	    								this.sti = Integer.parseInt(results_inquiry[i][11].toString());
	    								this.sti_text = results_inquiry[i][12].toString();
	    								this.layer1_text = results_inquiry[i][13].toString();
	    								this.layer2_text = results_inquiry[i][14].toString();
	    								this.layer3_text = results_inquiry[i][15].toString();
	    								this.name_action = results_inquiry[i][16].toString();
	    								this.action_destination = results_inquiry[i][17].toString();
	    								this.sms_text = results_inquiry[i][18].toString();
	    								this.approval = results_inquiry[i][19].toString();
	    								this.email_approval = results_inquiry[i][20].toString();
	    								
	    								try
	    								{
	    			    					String content = "";
		    								BufferedReader reader = new BufferedReader(new FileReader(this.path3+"assets/rejection_response.html"));
		    								StringBuilder sb = new StringBuilder();
		    						        String line = reader.readLine();
		    				
		    						        while (line != null) {
		    						            sb.append(line);
		    						            sb.append("\n");
		    						            line = reader.readLine();
		    						        }
		    						        content = sb.toString();

		    						        emailApprover2=this.sender+this.email;
	    							        smtp.setRecipient(emailApprover2);
	    							        smtp.setSubject("Rejection Response for Campaign : "+this.nameCampaign);
		    									
	    									content = content.replaceAll("_admin_email", "dpwibpushapp@xl.co.id");
	    									content = content.replaceAll("_name_campaign", this.nameCampaign);
	    									content = content.replaceAll("_id_campaign", new Integer(this.idCampaign).toString());
	    									content = content.replaceAll("_email_approval", this.email_approval);
	    									content = content.replaceAll("_approval", this.approval);
	    									content = content.replaceAll("_email", this.email);
	    									content = content.replaceAll("_name", this.name);
	    									content = content.replaceAll("_type", type[this.type]);
	    									content = content.replaceAll("_total_recipient", this.totalRecipient).toString();
	    									content = content.replaceAll("_start_date_campaign", this.startDateCampaign);
	    									content = content.replaceAll("_start_time_campaign", this.startTimeCampaign);
	    									content = content.replaceAll("_tone_title", this.tone_title);
	    									if(this.tone_title.compareTo("")==0)
	    									{	
	    										content = content.replaceAll("_tone_duration", new Integer(this.tone_duration).toString());
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_tone_duration", "");
	    									}
	    									if(this.sti==1)
	    									{	
	    										content = content.replaceAll("_sti_text", this.sti_text);
	    									}	
	    									else
	    									{
	    										content = content.replaceAll("_sti_text", "");
	    									}
	    									content = content.replaceAll("_layer1_text", this.layer1_text);
	    									content = content.replaceAll("_layer2_text", this.layer2_text);
	    									content = content.replaceAll("_layer3_text", this.layer3_text);
	    									content = content.replaceAll("_id_action", this.name_action);
	    									if(this.name_action.compareTo("None")==0)
	    									{	
	    										content = content.replaceAll("_action_destination", this.action_destination);
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_action_destination", "");
	    									}
	    									if(this.name_action.compareTo("SMS")==1)
	    									{	
	    										content = content.replaceAll("_sms_text", this.sms_text);
	    									}
	    									else
	    									{
	    										content = content.replaceAll("_sms_text", "");
	    									}
	    									smtp.setContent(content);
		    									
	    									smtp.sendSMTP();
	    								
	    								}	
		    							catch (FileNotFoundException e) 
		    							{
		    								logger.error(this.getClass().getName()+" "+e.getMessage());
		    							}	
		    							catch (IOException e) 
		    							{
		    								logger.error(this.getClass().getName()+" "+e.getMessage());
		    							}
	    								
	    								break;	
	    							}
	    						}
	    					}	
    					}	
    				}
    			}    

                message.setFlag(Flag.SEEN, true);
            }
            eFolder.close(true);
            eStore.close();

        } 
        catch (NoSuchProviderException e) 
        {
        	logger.error(this.getClass().getName()+" "+e.getMessage());
        } 
        catch (MessagingException e) 
        {
        	logger.error(this.getClass().getName()+" "+e.getMessage());
        } 

    }

    public static void main(String[] args) 
    {
    	IMAP imap = new IMAP();
    }

}