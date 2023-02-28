package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Java HTTP クライアントサンプル - HttpURLConnection + 認証編 -
 *
 * @author 68user http://X68000.q-e-d.net/~68user/
 */
//http://x68000.q-e-d.net/~68user/net/java-http-url-connection-2.html
public class HttpClientHttpURLConnectionAuth {
    public static void main(String[] args)
        throws MalformedURLException, ProtocolException, IOException {

        URL url = new URL("http://X68000.q-e-d.net/~68user/net/sample/http-auth/secret.html");
        //URL url = new URL("http://X68000.q-e-d.net/~68user/net/sample/http-auth-digest/secret.html");
        String username = "hoge";
        String password = "fuga";

        HttpAuthenticator http_authenticator = new HttpAuthenticator(username, password);
        Authenticator.setDefault(http_authenticator);

        HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
        urlconn.setRequestMethod("GET");
        urlconn.setInstanceFollowRedirects(false);
        urlconn.setRequestProperty("Accept-Language", "ja;q=0.7,en;q=0.3");

        urlconn.connect();

        Map headers = urlconn.getHeaderFields();
        Iterator it = headers.keySet().iterator();
        System.out.println("レスポンスヘッダ:");
        while (it.hasNext()){
            String key= (String)it.next();
            System.out.println("  " + key + ": " + headers.get(key));
        }

        System.out.println
            ("レスポンスコード[" + urlconn.getResponseCode() + "] " +
             "レスポンスメッセージ[" + urlconn.getResponseMessage() + "]");
        System.out.println
            ("プロンプト(realm)[" + http_authenticator.myGetRequestingPrompt() + "]");
        System.out.println("\n---- ボディ ----");


        BufferedReader reader =
            new BufferedReader(new InputStreamReader(urlconn.getInputStream()));

        while (true){
            String line = reader.readLine();
            if ( line == null ){
                break;
            }
            System.out.println(line);
        }

        reader.close();
        urlconn.disconnect();
    }
}

class HttpAuthenticator extends Authenticator {
    private String username;
    private String password;
    public HttpAuthenticator(String username, String password){
        this.username = username;
        this.password = password;
    }
    protected PasswordAuthentication getPasswordAuthentication(){
        return new
            PasswordAuthentication(username, password.toCharArray());
    }
    public String myGetRequestingPrompt(){
        return super.getRequestingPrompt();
    }
}
