package ai.mcpdirect.backend.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Mail {
	public static class Setting{
		public String address;
		public String account;
		public String password;
		public String name;
		public String server;
		public int port;
		public boolean ssl;
		public boolean tls;
	}
	private static final ExecutorService POOL = Executors.newFixedThreadPool(10);
	private static final String sslFactory = "javax.net.ssl.SSLSocketFactory";
	private String mailServerHost = null;
	private int mailServerPort;
	private String fromAddress = null;
	private String[] toAddress = null;
	private String[] ccAddress = null;
	private String[] bccAddress = null;
	private String userName = null;
	private String password = null;
	private boolean validate = false;
	private String subject = null;
	private String content = null;
	private String[] attachFileNames = null;

	private boolean isSSL = false;
	private boolean isTLS = false;
	public Properties getProperties() {
		Properties p = new Properties();
		p.put("mail.debug", "true");
		p.put("mail.smtp.host", this.mailServerHost);
		p.put("mail.transport.protocol", "smtp");
		p.put("mail.smtp.auth", validate ? "true" : "false");

		if (isSSL){
			p.put("mail.smtp.socketFactory.class", sslFactory);
			p.put("mail.smtp.socketFactory.fallback", "false");
			p.put("mail.smtp.socketFactory.port", this.mailServerPort);
			p.put("mail.smtp.port", this.mailServerPort);
		}else{
			p.put("mail.smtp.port", this.mailServerPort);
		}
		if(isTLS) {
			p.put("mail.smtp.starttls.enable", "true");
		}
		return p;
	}


	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public int getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(int mailServerPort) {
        isSSL = mailServerPort == 465;
		this.mailServerPort = mailServerPort;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public String[] getAttachFileNames() {
		return attachFileNames;
	}

	public void setAttachFileNames(String[] fileNames) {
		this.attachFileNames = fileNames;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress){
		this.fromAddress = fromAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getToAddress() {
		return toAddress;
	}

	public void setToAddress(String[] toAddress) {
		this.toAddress = toAddress;
	}

	public String[] getCcAddress() {
		return ccAddress;
	}

	public String[] getBccAddress() {
		return bccAddress;
	}

	public void setCcAddress(String[] ccAddress) {
		this.ccAddress = ccAddress;
	}

	public void setBccAddress(String[] bccAddress) {
		this.bccAddress = bccAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getHtmlContent() {
		String co = content;
		if (co.contains("\n\r")){
			co = co.replaceAll("\n\r", "<br/>");
		}
		if (co.contains("\r\n")){
			co = co.replaceAll("\r\n", "<br/>");
		}
		if (co.contains("\n")){
			co = co.replaceAll("\n", "<br/>");
		}
		if (co.contains("\r")){
			co = co.replaceAll("\r", "<br/>");
		}
		if (co.contains("\t")){
			co = co.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		return co;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String textContent) {
		this.content = textContent;
	}
	String mailSenderName = "";

	public String getMailSenderName() {
		return mailSenderName;
	}

	public void setMailSenderName(String mailSenderName) {
		this.mailSenderName = mailSenderName;
	}


	public boolean isSSL() {
		return isSSL;
	}
	public void setSSL(boolean isSSL) {
		this.isSSL = isSSL;
	}

	public boolean isTLS() {
		return isTLS;
	}

	public void setTLS(boolean isTLS) {
		this.isTLS = isTLS;
	}
	public boolean send() throws Exception{
		Authenticator authenticator = null;
		Properties pro = getProperties();
		if (isValidate()) {
			authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication(){
					return new PasswordAuthentication(userName, password);
				}
			};
		}
		Session sendMailSession = Session.getInstance(pro, authenticator);

		Message mailMessage = new MimeMessage(sendMailSession);
		Address from = new InternetAddress(getFromAddress(),mailSenderName);
		mailMessage.setFrom(from);
		Address[] to = getAddresses(getToAddress());
		mailMessage.addRecipients(Message.RecipientType.TO, to);

		if (getCcAddress() != null) {
			Address[] cc = getAddresses(getCcAddress());
			mailMessage.addRecipients(Message.RecipientType.CC, cc);
		}
		if (getBccAddress() != null) {
			Address[] bcc = getAddresses(getBccAddress());
			mailMessage.addRecipients(Message.RecipientType.BCC, bcc);
		}
		mailMessage.setSubject(MimeUtility.encodeText(getSubject(), "UTF-8", "B"));

		mailMessage.setSentDate(new Date());
		Multipart mainPart = new MimeMultipart();
		BodyPart html = new MimeBodyPart();
		html.setContent(getHtmlContent(), "text/html; charset=utf-8");
		mainPart.addBodyPart(html);
		mailMessage.setContent(mainPart);

		Transport.send(mailMessage);
		return true;
	}
	public Future<Exception> submit(){
		return POOL.submit((Callable<Exception>) () -> {
            try {
                send();
            } catch (Exception e) {
                return e;
            }
            return null;
        });
	}

	public static List<String> emails(String str){
		List<String> list = new ArrayList<>();
		if (str!=null){
			String[] sd = str.split(",");
			for(String s:sd){
				String[] sf = s.split(";");
				for (String s2:sf){
					if (!s2.trim().isEmpty()){
						list.add(s2.trim());
					}
				}
			}
		}
		return list;
	}
	public static boolean isEmail(String email){
		String regex = "[\\w\\-]+(\\.\\w+)*@[\\w\\-]+(\\.\\w{2,}){1,3}";
	    return email.matches(regex);
	}
	private static Address[] getAddresses(String[] list) throws AddressException {
		Address[] addresses = new Address[list.length];
		for (int i = 0; i < list.length; i++) {
			addresses[i] = new InternetAddress(list[i]);
		}
		return addresses;
	}
	public static Mail create(Setting setting,String subject,String content,String... to){
		Mail mail = new Mail();
		mail.setMailServerHost(setting.server);
		mail.setMailServerPort(setting.port);
		mail.setValidate(true);
		mail.setUserName(setting.account);
		mail.setPassword(setting.password);
		mail.setFromAddress(setting.address);
		mail.setMailSenderName(setting.name);

		mail.setToAddress(to);

		mail.setSubject(subject);
		mail.setContent(content);
		mail.setSSL(setting.ssl);
		mail.setTLS(setting.tls);
		return mail;
	}
}