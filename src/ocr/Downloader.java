package ocr;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

	//public Downloader() {
	//}

	// ★ここは自分で書き直してください★
	private static final String InputData = "http://k.yimg.jp/images/top/sp2/cmn/logo-ns-131205.png";
	private static final String OutputData = "C:\\Temp\\yahoobloglogo.png";
	private static final int BufferSize = 4096;
	
	public static void main(String[] args) {
		try {
			URL url = new URL(InputData);
			HttpURLConnection urlConnection = 
					(HttpURLConnection) url.openConnection();
			// false の場合、ユーザーとの対話処理は許可されていません。
			urlConnection.setAllowUserInteraction(false);
			// true の場合、プロトコルは自動的にリダイレクトに従います
			urlConnection.setInstanceFollowRedirects(true);
			// URL 要求のメソッドを"GET"に設定
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			
			// HTTP 応答メッセージから状態コードを取得します
			int httpStatusCode = urlConnection.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				throw new Exception();
			}

			writeStream(urlConnection.getInputStream(), OutputData);
			System.out.println("Completed!!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private static void writeStream(InputStream inputStream, String outputPath)
		   throws Exception {
		int availableByteNumber;
		byte[] buffers = new byte[BufferSize];
		try (DataInputStream dataInputStream = new DataInputStream(inputStream);
		      DataOutputStream outputStream = new DataOutputStream(
		            new BufferedOutputStream(new FileOutputStream(outputPath)))) {
		   while ((availableByteNumber = dataInputStream.read(buffers)) > 0) {
		      outputStream.write(buffers, 0, availableByteNumber);
		   }
		} catch (Exception ex) {
		   throw ex;
		}
	}
}
