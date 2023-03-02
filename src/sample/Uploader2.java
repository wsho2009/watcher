package sample;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

//https://qiita.com/MikuriyaHiroshi/items/4f213595b7b69e87eb01
public class Uploader2 {
    private static final String EOL = "\r\n";

	//public Uploader() {
	//}

	public static void main(String[] args) {
        String filename = "D:\\pleiades\\input\\A0001_20230211232606.pdf";
        String url = "http://localhost:8080/upload/upload?type=upload";
        int res;
		try {
			res = Uploader.Send(filename, url, "POST");
	        if (res == HttpURLConnection.HTTP_OK) {
	            System.err.println("Success!");
	        } else {
	            System.err.printf("Failed %d\n", res);
	        }
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

    public static void Send(String filepath, String url, String method) throws IOException {

    	String CRLF = "\r\n";
    	String userId = "12345";
    	String documentId = "54321";
    	File file = new File(filepath);
    	String boundary = UUID.randomUUID().toString();
    	HttpURLConnection con =null;
    	InputStream in = null;

    	try {
    	    // ファイルをbyte配列に変換
    	    byte[] fileByte = Files.readAllBytes(file.toPath());

    	    con = (HttpURLConnection) new URL(url).openConnection();
    	    con.setRequestMethod("POST");
    	    con.setDoOutput(true);
    	    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

    	    DataOutputStream request = new DataOutputStream(con.getOutputStream());

    	    request.writeBytes("--" + boundary + CRLF);
    	    request.writeBytes("Content-Disposition: form-data; name=\"userId\"" + CRLF + userId + CRLF);
    	    request.writeBytes(userId);
    	    request.writeBytes("--" + boundary + CRLF);
    	    request.writeBytes("Content-Disposition: form-data; name=\"documentId\"" + CRLF + documentId + CRLF);
    	    request.writeBytes(documentId);
    	    request.writeBytes("--" + boundary + CRLF);
    	    request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + CRLF + CRLF);
    	    request.write(fileByte);
    	    request.writeBytes(CRLF);
    	    request.writeBytes("--" + boundary + "--" + CRLF);
    	    request.flush();
    	    request.close();

    	    // 結果を取得する
    	    int status = con.getResponseCode();

    	    if(status == HttpURLConnection.HTTP_OK) {
    	        // 正常系の場合はgetInputStream
    	        in = con.getInputStream();
    	    } else {
    	        // 異常系の場合はgetErrorStream
    	        in = con.getErrorStream();
    	    }
    	    // InputStream結果を出力（今回は割愛）
    	    //read(in);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	} finally {
    	    if(con != null) {
    	        con.disconnect();
    	    }
    	}     
    }
}
