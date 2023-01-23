package fax;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

//http://sakusaku-techs.com/java/java-mail-attach/#i-3	★
//https://qiita.com/kiririnyo/items/c5f6d95aa314e877ab25	PDF添付２
public class JavaMail {

//	public JavaMail() {
//		// TODO 自動生成されたコンストラクター・スタブ
//	}

	public static void main(String[] args) {
        System.out.print("start: main\r\n");
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		String targetPath = rb.getString("TARGET_PATH");

		String subject = "テストメール";
		String attach = targetPath + "\\AAAA\\AAAA.pdf";
        sendRawMail(subject, attach);

        System.out.print("end: main\r\n");
    }
	
	public static void sendRawMail(String subject, String attach) {
        // メール送信のプロパティ設定
        Properties properties = new Properties();
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		String mailHost = rb.getString("MAIL_HOST");
		String mailPort = rb.getString("MAIL_PORT");
        properties.put("mail.smtp.host", mailHost);	// ホスト
        properties.put("mail.smtp.port", mailPort);	// ポート指定（サブミッションポート）
        properties.put("mail.smtp.auth", "false");	// 認証
        //properties.put("mail.transport.protocol", "smtp");
        //properties.put("mail.smtp.ssl.trust", "*");
        //properties.put("mail.smtp.starttls.enable", "true");		// STARTTLSによる暗号化
        //properties.put("mail.smtp.starttls.enable", "false");
        properties.put("mail.smtp.debug", "true");
        // タイムアウト
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");

        try {
            // セッションの作成。
            final Session session = Session.getInstance(properties , new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    // Gmailなどを使う場合はユーザ名とパスワードを引数に指定。
                    return new PasswordAuthentication("", "");
                }
            });
            final Message message = new MimeMessage(session);

            // 基本情報
    		String fromAddress = rb.getString("MAIL_FROM");
            message.setFrom(new InternetAddress(fromAddress));
            String toAddress = fromAddress;
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress, false));
            // タイトル
            message.setSubject(subject);

			// メッセージ本文
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("テストメール。");
			
			// １：添付ファイル１を添付するボディパートを取得
			MimeBodyPart attachedFilePart1 = new MimeBodyPart();
			// ２：添付ファイル１のデータソースを取得
			FileDataSource fs1 = new FileDataSource(attach);
			// ３：ボディパート１に添付ファイル１を添付
			attachedFilePart1.setDataHandler(new DataHandler(fs1));
			attachedFilePart1.setFileName(
					MimeUtility.encodeWord(fs1.getName()));

			// ７：メールに、本文・添付１・添付２の３つを添付
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachedFilePart1);
			message.setHeader(
			    "Content-Transfer-Encoding", "base64");
			
			// ８：メールを送信する
			message.setContent(multipart);
			Transport.send(message);

        } catch (Exception e) {
        	System.out.print("例外が発生！\r\n");
            e.printStackTrace();
            //throw new InternalServerErrorException(e);
        }
    }
	public byte[] convertFile(File file) throws IOException {
	    return Files.readAllBytes(file.toPath());
	}
}
