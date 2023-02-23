package ocr;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebApi {
	//request
	String url;
	String method;
    static class Proxy {
    	String host;
    	String port;
    	String user;
    	String password;
    }
    static Proxy proxy = new WebApi.Proxy();
	String[] header_key = new String[5];
	String[] header_value = new String[5];
	int headerCnt = 0;
	
	//response
    String responseMessage;
    int responseCode;
    String responseStr;
    JsonNode responseJson;
   
    static class FormData {
    	String userId;
    	String documentId;
    	String file;
    }
    FormData formData = new WebApi.FormData();	//for Upload
	public String saveFile;	//for Download
    
    private static final String EOL = "\r\n";
	//private static final String EOL = "\n";
    public WebApi() {
	}

	public void setProxy(String host, String port, String user, String password) {
    	WebApi.proxy.host = host;
    	WebApi.proxy.port = port;
    	WebApi.proxy.user = user;
    	WebApi.proxy.password = password;
	}
	
    public void putRequestHeader(String key, String value) {
		this.header_key[headerCnt] = key;
		this.header_value[headerCnt] = value;
    	headerCnt++;
    }

	public int upload() throws IOException {
		if (this.formData.file == null)
            return -1;
        Path p = Paths.get(this.formData.file);
        // パスの末尾（ファイル名）を取得
        String fileName = p.getFileName().toString();
        System.out.println( fileName );
           
        try (FileInputStream file = new FileInputStream(this.formData.file)) {
    		//http://www.mwsoft.jp/programming/java/http_proxy.html
    		HttpURLConnection con = null;
    		if (WebApi.proxy.host != null) {
                System.setProperty("proxySet", "true");
                System.setProperty("proxyHost", WebApi.proxy.host);
                System.setProperty("proxyPort", WebApi.proxy.port);
                Authenticator.setDefault(new Authenticator() {
					@Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                    }
                });
    		}
            con = (HttpURLConnection) new URL(this.url).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(method);
		    
            //add request header
            for (int i=0; i<5; i++) {
            	if (this.header_key[i] == null) 
            		break;
        		con.setRequestProperty(this.header_key[i], this.header_value[i]);
            }
            
            final String boundary = UUID.randomUUID().toString();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream out = con.getOutputStream()) {
            	if (formData.userId != null) {
                out.write(("--" + boundary + EOL +
                        "Content-Disposition: form-data; name=\"userId\"" + EOL + 
                        formData.userId + EOL)
                        .getBytes(StandardCharsets.UTF_8)
                    );
            	}
            	if (formData.documentId != null) {
                out.write(("--" + boundary + EOL +
                        "Content-Disposition: form-data; name=\"documentId\"" + EOL + 
                        formData.documentId + EOL)
                        .getBytes(StandardCharsets.UTF_8)
                    );
            	}
                out.write(("--" + boundary + EOL +
                    "Content-Disposition: form-data; name=\"file\"; " +
                    "filename=\"" + fileName + "\"" + EOL +
                    //"filename=\"A0001.pdf\"" + EOL +
                    "Content-Type: application/octet-stream" + EOL + EOL)
                    .getBytes(StandardCharsets.UTF_8)
                );
                byte[] buffer = new byte[128];
                int size = -1;
                while (-1 != (size = file.read(buffer))) {
                    out.write(buffer, 0, size);
                }
                out.write((EOL + "--" + boundary + "--" + EOL).getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.err.println(con.getResponseMessage());
                
                this.responseCode = con.getResponseCode();
                this.responseMessage = con.getResponseMessage();

                //resuponse Code と Bodyを解析
                ObjectMapper mapper = new ObjectMapper();
                this.responseJson = mapper.readTree(this.responseMessage.toString());
                this.responseStr = this.responseMessage.toString();
                
                return this.responseCode;
            } finally {
                con.disconnect();
            }
        }
	}

	public int sendGet() throws Exception {

		//http://www.mwsoft.jp/programming/java/http_proxy.html
		URL obj = new URL(this.url);
		HttpURLConnection con = null;
		if (WebApi.proxy.host != null) {
            System.setProperty("proxySet", "true");
            System.setProperty("proxyHost", WebApi.proxy.host);
            System.setProperty("proxyPort", WebApi.proxy.port);
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                }
            });
		}
        con = (HttpURLConnection) obj.openConnection();
        
        //optional default is GET
        con.setRequestMethod(this.method);

        //add request header
        for (int i=0; i<5; i++) {
            if (this.header_key[i] == null) 
        		break;
    		con.setRequestProperty(this.header_key[i], this.header_value[i]);
        }

        this.responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + this.responseCode);
        
        //レスポンスボディの読み出し
        BufferedReader in = new BufferedReader( 
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

		//resuponse Code と Bodyを解析
	    ObjectMapper mapper = new ObjectMapper();
	    this.responseJson = mapper.readTree(response.toString());
	    this.responseStr = response.toString();
	    
        return responseCode;

    }

	public int download() throws IOException {
		if (this.saveFile == null)
            return -1;
		
		int httpStatusCode = 0;
		try {
			URL obj = new URL(this.url);
			HttpURLConnection con = null;
			//http://www.mwsoft.jp/programming/java/http_proxy.html
    		if (WebApi.proxy.host != null) {
                System.setProperty("proxySet", "true");
                System.setProperty("proxyHost", WebApi.proxy.host);
                System.setProperty("proxyPort", WebApi.proxy.port);
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(WebApi.proxy.user, WebApi.proxy.password.toCharArray());
                    }
                });
    		}
			con = (HttpURLConnection) obj.openConnection();
			
			// false の場合、ユーザーとの対話処理は許可されていません。
			con.setAllowUserInteraction(false);
			// true の場合、プロトコルは自動的にリダイレクトに従います
			con.setInstanceFollowRedirects(true);
			// URL 要求のメソッドを"GET"に設定
			con.setRequestMethod(method);

	        //add request header
	        for (int i=0; i<5; i++) {
            	if (this.header_key[i] == null) 
	        		break;
                con.setRequestProperty(this.header_key[i], this.header_value[i]);
	        }
			
			con.connect();
			
			// HTTP 応答メッセージから状態コードを取得します
			this.responseCode = con.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				throw new Exception();
			}
	        //レスポンスボディの読み出し
			writeStream(con.getInputStream(), saveFile);
			System.out.println("Completed!!");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return this.responseCode;
	}
	
	private void writeStream(InputStream inputStream, String outputPath) throws Exception {
		final int BufferSize = 4096;
		
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
