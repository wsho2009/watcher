package sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

//https://qiita.com/MikuriyaHiroshi/items/4f213595b7b69e87eb01
public class Uploader {
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

    public static int Send(String filepath, String url, String method) throws IOException {
        Path p = Paths.get(filepath);
        // パスの末尾（ファイル名）を取得
        String filename = p.getFileName().toString();
        System.out.println( filename );
        try (FileInputStream file = new FileInputStream(filepath)) {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            final String boundary = UUID.randomUUID().toString();
            con.setDoOutput(true);
            con.setRequestMethod(method);
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
                return con.getResponseCode();
            } finally {
                con.disconnect();
            }
        }
    }
}
