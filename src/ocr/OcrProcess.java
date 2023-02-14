package ocr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class OcrProcess {

	static boolean ocrExlusiveFlag;
	static String DX_URL = "DX_URL";
	static String DX_PROXY_HOST = "DX_PROXY_HOST";
	static int DX_PROXY_PORT = 8080;
	static String API_KEY = "API_KEY";
	static String USER_ID = "USER_ID";

	public OcrProcess() {
	}
	
	public static void main(String[] args) {
        Timer timer = new Timer(); // 今回追加する処理
        ocrExlusiveFlag = false;
        TimerTask task = new TimerTask() {
            int count = 0;
            public void run() {
                // 定期的に実行したい処理
                count++;
                System.out.println(count + "回目のタスクが実行されました。");
                if (ocrExlusiveFlag == true ) {
                	System.out.println("wait...");
                	return;
                }
                OcrProcess process = new OcrProcess();
                ocrExlusiveFlag = true;
                process.pollingReadingUnit();
                ocrExlusiveFlag = false;
                //---------------------------------
                //watchdog 書き込み処理
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
                System.out.println(sdf.format(date));
                try {
                	File file = new File(".\\data\\ocr_watchdog.dat");
                	FileWriter filewriter = new FileWriter(file);
                	filewriter.write(sdf.format(date));
                	filewriter.close();
            	}catch(IOException e){
            	  System.out.println(e);
            	}                
                //---------------------------------
        	}
    	};
        timer.scheduleAtFixedRate(task,0,3000); // 今回追加する処理（3000ms間隔）
    }
	
	public void pollingReadingUnit() {
		try {
			ArrayList<OcrDataFormBean> list = OcrDataFormDAO.getInstance().queryNotComplete();
			int count = list.size();
			if (count != 0) {
				System.out.println("  find data: " + count);
			}
			for (int o=0; o<count; o++) {
				OcrDataFormBean ocrDataForm = (OcrDataFormBean)list.get(o);
				if (ocrDataForm.status.equals("REGIST") == true) {
					System.out.println("  " + o + " addReadingPage");
					addReadingPage(ocrDataForm.documentId, ocrDataForm.uploadFilePath);
					break;
				} else if (ocrDataForm.status.equals("REGISTED") == true) {
					System.out.println("  " + o + " searchReadingUnit");
					searchReadingUnit(ocrDataForm, false);
				} else if (ocrDataForm.status.equals("OCR") == true || ocrDataForm.status.equals("ENTRY") == true) {
					System.out.println("  " + o + " proecssReadingUnit");
					proecssReadingUnit(ocrDataForm);
				}
			}
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	//---------------------------------------
	//読取ページ追加（処理）
	//---------------------------------------
	private void addReadingPage(String documentId, String uploadFilePath) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.url = DX_URL +"xxxx";
		api.method = "POST";
		api.proxy_host = DX_PROXY_HOST;
		api.proxy_port = DX_PROXY_PORT;
		api.header_key[0] = "X-xxxx";
		api.header_value[0] = API_KEY;
		//---------------------------------------
		api.formData.userId = USER_ID;
		api.formData.documentId = documentId;
		api.formData.file = uploadFilePath;
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.upload(uploadFilePath);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			res = -1;
			e.printStackTrace();
		}
     	OcrDataFormBean ocrData = new OcrDataFormBean();
     	//30msにrequestのレスポンスが返ってい来ない時がある対策
        //---------------------------------------
        ocrData.uploadFilePath = uploadFilePath;
        ocrData.status = "REGISTED";
		//Date date = new Date();        
		String dateToStr = dateFormat.format(new Date());	            
        ocrData.createdAt = dateToStr;
        
        ocrData.updateFromUploadFilePath();
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
            System.err.printf("Failed %d\n", res);
        } else {
            System.err.println("Success!");
         	String status = api.responseJson.get("status").asText();;
         	int errorCode = api.responseJson.get("errorCode").asInt();;
         	String message = api.responseJson.get("message").asText();;
         	String unitId = api.responseJson.get("unitId").asText();;
         	
         	if (errorCode != 0) {
         		System.err.println("  addReadingPage: HTTPレスポンスエラー: " + errorCode);
	            ocrData.uploadFilePath = uploadFilePath;
	            ocrData.unitName = "登録不可";
	            ocrData.status = "COMPLETE";
	            //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		//Date date2 = new Date();        
	    		String dateToStr2 = dateFormat.format(new Date());	            
	            ocrData.createdAt = dateToStr2;
	            
	            ocrData.updateFromUploadFilePath();
	            return;
         	}
            
    		//unitId,statusの更新
        	ocrData.uploadFilePath = uploadFilePath;
        	ocrData.unitId = unitId;
        	ocrData.status = "OCR";
        	ocrData.updateFromUploadFilePath();	//更新
        	
        	//ReadingUnit情報の崇徳(2000ms後に1回実行)
        	searchReadingUnit(ocrData, false);
        	
        	return;
        }
        
		
	}
	
	//---------------------------------------
	//読取ユニット検索
	//---------------------------------------
	private void searchReadingUnit(OcrDataFormBean ocrData, boolean sortFlag) {
		if (ocrData.unitId.equals("") == true) {
			System.err.println("■searchReadingUnit: unitId不正エラー");
			//add後、まだ、レスポンスが返ってきていないケースがあるので、終了する。
			return;
		}
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.url = DX_URL + String.format("xxxx%s", ocrData.unitId);
		api.method = "GET";
		api.proxy_host = DX_PROXY_HOST;
		api.proxy_port = DX_PROXY_PORT;
		api.header_key[0] = "X-xxxxx";
		api.header_value[0] = API_KEY;
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.sendGet();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			res = -1;
		}
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res == HttpURLConnection.HTTP_OK) {
	        System.err.printf("Failed %d\n", res);
	        return;
        } 
     	int errorCode = api.responseJson.get("errorCode").asInt();;
    	if (errorCode != 0) {
    		//リトライ(20090ms後)
    		Timer timer = new Timer(false);
    		TimerTask task = new TimerTask() {
    			 
    			@Override
    			public void run() {
    				searchReadingUnit(ocrData, false);
    				timer.cancel();
    			}
    		};
    		timer.schedule(task, 2000);
    		return;
    	} 
    	String unitId = api.responseJson.get("readingUnits").get(0).get("id").asText();
    	String unitName = api.responseJson.get("readingUnits").get(0).get("name").asText();
    	String unitStatus = api.responseJson.get("readingUnits").get(0).get("status").asText();
    	String docsetId = api.responseJson.get("readingUnits").get(0).get("docsetId").asText();
    	String csvFileName = api.responseJson.get("readingUnits").get(0).get("csvFileName").asText();
    	String documentId = api.responseJson.get("readingUnits").get(0).get("documentId").asText();
    	String documentName = api.responseJson.get("readingUnits").get(0).get("documentName").asText();
    	String createdAt = api.responseJson.get("readingUnits").get(0).get("createdAt").asText();
    	
		//unitId,statusの更新
    	ocrData.status = "OCR";
    	ocrData.unitId = unitId;
    	ocrData.csvFileName = csvFileName;
    	ocrData.docsetId = docsetId;
    	ocrData.createdAt = createdAt.substring(0, createdAt.length()-2);	//語尾の.0(２桁)をとる
    	ocrData.linkUrl = "";	//TBD
    	ocrData.updateFromUploadFilePath();	//更新
    	
    	return;
	}

	//読取ユニット処理
	private void proecssReadingUnit(OcrDataFormBean ocrData) {
		if (ocrData.unitId.equals("") == true) {
			System.err.println("■searchReadingUnit: unitId不正エラー");
			//add後、まだ、レスポンスが返ってきていないケースがあるので、終了する。
			return;
		}
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.url = DX_URL + String.format("xxxx%s", ocrData.unitId);
		api.method = "GET";
		api.proxy_host = DX_PROXY_HOST;
		api.proxy_port = DX_PROXY_PORT;
		api.header_key[0] = "X-xxxxx";
		api.header_value[0] = API_KEY;
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.sendGet();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			res = -1;
		}
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res == HttpURLConnection.HTTP_OK) {
	        System.err.printf("Failed %d\n", res);
	        return;
        } 
     	String status = api.responseJson.get("status").asText();;
     	int errorCode = api.responseJson.get("errorCode").asInt();;
     	String message = api.responseJson.get("message").asText();;
     	if (status != "success") {
	    	if (errorCode == 103) {
	     		System.err.println("  addReadingPage: HTTPレスポンスエラー: " + errorCode);
	            //ocrData.uploadFilePath = uploadFilePath;
	            ocrData.unitName = "削除";
	            ocrData.status = "COMPLETE";
	    		//Date date = new Date();        
	    		String dateToStr = dateFormat.format(new Date());	            
	            ocrData.createdAt = dateToStr;
	            
	            ocrData.updateFromUploadFilePath();
	            return;
	    	}
	    	return;
     	}
     	//unitiId指定なのえｄ、1個しかないはず
    	String unitId = api.responseJson.get("readingUnits").get(0).get("id").asText();
    	String unitName = api.responseJson.get("readingUnits").get(0).get("name").asText();
    	String unitStatus = api.responseJson.get("readingUnits").get(0).get("status").asText();
    	String docsetId = api.responseJson.get("readingUnits").get(0).get("docsetId").asText();
    	String csvFileName = api.responseJson.get("readingUnits").get(0).get("csvFileName").asText();
    	String documentId = api.responseJson.get("readingUnits").get(0).get("documentId").asText();
    	String documentName = api.responseJson.get("readingUnits").get(0).get("documentName").asText();
    	String createdAt = api.responseJson.get("readingUnits").get(0).get("createdAt").asText();
    	
		//unitId,statusの更新
    	ocrData.status = "OCR";
    	ocrData.unitId = unitId;
    	ocrData.csvFileName = csvFileName;
    	ocrData.docsetId = docsetId;
    	ocrData.createdAt = createdAt.substring(0, createdAt.length()-2);	//語尾の.0(２桁)をとる
    	ocrData.linkUrl = "";	//TBD
    	
    	if (unitStatus.equals("13")==true && ocrData.status.equals("ENTRY")==false 
    			&& ocrData.status.equals("SORT")==false) {
    		//CSVエクスポート（メール送信なし）、OCR後処理
    		ocrData.mailFlag = 0;	//結果送付なし
    		exportResultCSV(ocrData);
    	} else if (unitStatus.equals("16")==true && unitStatus.equals("22")==true) {
    		//CSVエクスポート（メール送信あり）、OCR後処理
    		ocrData.mailFlag = 0;	//結果送付あり
    		exportResultCSV(ocrData);
    	}
    	
    	ocrData.updateFromUploadFilePath();	//更新
    	
    	return;
	}

	private void exportResultCSV(OcrDataFormBean ocrData) {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        System.out.println("■exportResultCSV: start");
        //出力先フォルダ取得(なければ作成)
        ocrData.outFoloderPath = gtTgtFolderPath(ocrData);
        
        //DLファイル(ファイルパス)取得
        String csvFilePath = ocrData.outFoloderPath + ocrData.csvFileName;
        //既存ファイルがあれば削除
        Path p = Paths.get(csvFilePath);
        if (Files.exists(p)) {
        	try{
        		Files.delete(p);
    		}catch(IOException e){
    			System.out.println(e);
    		}
        }
        System.out.println("  CSV file: " + csvFilePath); 
		//---------------------------------------
		//HTTP request parametes
		//---------------------------------------
		WebApi api = new WebApi();
		api.url = DX_URL + String.format("xxxx%s", ocrData.unitId);
		api.method = "GET";
		api.proxy_host = DX_PROXY_HOST;
		api.proxy_port = DX_PROXY_PORT;
		api.header_key[0] = "X-xxxxx";
		api.header_value[0] = API_KEY;
		api.saveFile = ocrData.csvFileName;
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.download(ocrData.csvFileName);
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			res = -1;
		}
		//---------------------------------------
		//HTTP response process
		//---------------------------------------
        if (res != HttpURLConnection.HTTP_OK) {
	        System.err.printf("Failed %d\n", res);
	        return;
        } 
     	String status = api.responseJson.get("status").asText();;
     	int errorCode = api.responseJson.get("errorCode").asInt();;
     	String message = api.responseJson.get("message").asText();;
		
     	convertCSV(ocrData);
     	
     	postOcrProcess(ocrData);
     	
        System.out.println("■exportResultCSV: end");
     	return;
	}

	private Object gtTgtFolderPath(OcrDataFormBean ocrData) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private void convertCSV(OcrDataFormBean ocrData) {
		// TODO 自動生成されたメソッド・スタブ
		
	}
	
	private void postOcrProcess(OcrDataFormBean ocrData) {
        System.out.println("■postOcrProcess: start");
        //出力先フォルダ取得（この時点では作成されている）
        String outputFolderPath = ocrData.outFoloderPath;
        
        
        System.out.println("■postOcrProcess: end");
		
	}


	
}
