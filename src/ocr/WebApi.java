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
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebApi {
	//request
	String url;
	String method;
	String proxy_host;
	int proxy_port;
	String[] header_key;
	String[] header_value;
	
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
    FormData formData;	//for Upload
	public String saveFile;	//for Download
    
    private static final String EOL = "\r\n";
    public WebApi() {
    	String[] header_key = new String[5];
    	String[] header_value = new String[5];
		WebApi.FormData form = new WebApi.FormData();
	}

	public int upload(String filename) throws IOException {
		
        try (FileInputStream file = new FileInputStream(filename)) {
    		//http://www.mwsoft.jp/programming/java/http_proxy.html
    		HttpURLConnection con = null;
    		if (this.proxy_host.equals("") != true) {
    			Proxy proxy = new Proxy(Proxy.Type.HTTP, 
    					new InetSocketAddress(this.proxy_host, this.proxy_port));
                con = (HttpURLConnection) new URL(url).openConnection(proxy);
    		} else {
                con = (HttpURLConnection) new URL(url).openConnection();
    		}
            con.setDoOutput(true);
            con.setRequestMethod(method);
		    con.connect(); // URL接続
		    
            //add request header
            for (int i=0; i<5; i++) {
            	if (this.header_key[i].equals("") == true) 
            		break;
        		con.setRequestProperty(this.header_key[i], this.header_value[i]);
            }
            
            final String boundary = UUID.randomUUID().toString();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream out = con.getOutputStream()) {
                out.write(("--" + boundary + EOL +
                    "Content-Disposition: form-data; name=\"file\"; " +
                    "filename=\"" + filename + "\"" + EOL +
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
                
                responseCode = con.getResponseCode();
                responseMessage = con.getResponseMessage();
                
                return responseCode;
            } finally {
                con.disconnect();
            }
        }
	}

	public int sendGet() throws Exception {

		//http://www.mwsoft.jp/programming/java/http_proxy.html
		URL obj = new URL(url);
		HttpURLConnection con = null;
		if (this.proxy_host.equals("") != true) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, 
					new InetSocketAddress(this.proxy_host, this.proxy_port));
	        con = (HttpURLConnection) obj.openConnection(proxy);
		} else {
	        con = (HttpURLConnection) obj.openConnection();
		}
        //optional default is GET
        con.setRequestMethod(method);

        //add request header
        for (int i=0; i<5; i++) {
        	if (this.header_key[i].equals("") == true) 
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

	public int download(String filePath) throws IOException {
		int httpStatusCode = 0;
		try {
			URL obj = new URL(url);
			HttpURLConnection urlConnection = null;
			//http://www.mwsoft.jp/programming/java/http_proxy.html
			if (this.proxy_host.equals("") != true) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, 
						new InetSocketAddress(this.proxy_host, this.proxy_port));
				urlConnection = (HttpURLConnection) obj.openConnection(proxy);
			} else {
				urlConnection = (HttpURLConnection) obj.openConnection();
			}
			// false の場合、ユーザーとの対話処理は許可されていません。
			urlConnection.setAllowUserInteraction(false);
			// true の場合、プロトコルは自動的にリダイレクトに従います
			urlConnection.setInstanceFollowRedirects(true);
			// URL 要求のメソッドを"GET"に設定
			urlConnection.setRequestMethod("GET");

	        //add request header
	        for (int i=0; i<5; i++) {
	        	if (this.header_key[i].equals("") == true) 
	        		break;
	        	urlConnection.setRequestProperty(this.header_key[i], this.header_value[i]);
	        }
			
			urlConnection.connect();
			
			// HTTP 応答メッセージから状態コードを取得します
			this.responseCode = urlConnection.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				throw new Exception();
			}
	        //レスポンスボディの読み出し
			writeStream(urlConnection.getInputStream(), filePath);
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
