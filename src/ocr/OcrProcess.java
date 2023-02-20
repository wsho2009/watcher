package ocr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class OcrProcess {

	static boolean ocrExlusiveFlag;
	static String DX_URL;
	static String DX_PROXY_HOST;
	static String DX_PROXY_PORT;
	static String DX_PROXY_USER;
	static String DX_PROXY_PASSWORD;
	static String API_KEY;
	static String API_KEY_VALUE;
	static String USER_ID;
	static String OUTPUT_PATH;
	static ResourceBundle rb;
	
	public OcrProcess() {
		ocrExlusiveFlag = false;
		rb = ResourceBundle.getBundle("prop");
		DX_URL = rb.getString("DX_URL");
		DX_PROXY_HOST = rb.getString("DX_PROXY_HOST");
		DX_PROXY_PORT = rb.getString("DX_PROXY_PORT");
		DX_PROXY_USER = rb.getString("DX_PROXY_USER");
		DX_PROXY_PASSWORD = rb.getString("DX_PROXY_PASSWORD");
		API_KEY = rb.getString("API_KEY");
		API_KEY_VALUE = rb.getString("API_KEY_VALUE");
		USER_ID = rb.getString("USER_ID");
		
		OUTPUT_PATH = rb.getString("OUTPUT_PATH");
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
		String DX_ADD_PAGE = rb.getString("DX_ADD_PAGE");
		api.url = DX_URL + DX_ADD_PAGE;
		api.method = "POST";
		api.setProxy(DX_PROXY_HOST, DX_PROXY_PORT, DX_PROXY_USER, DX_PROXY_PASSWORD);
		api.putRequestHeader(API_KEY, API_KEY_VALUE);
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
		api.setProxy(DX_PROXY_HOST, DX_PROXY_PORT, DX_PROXY_USER, DX_PROXY_PASSWORD);
		api.putRequestHeader(API_KEY, API_KEY_VALUE);
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
		api.setProxy(DX_PROXY_HOST, DX_PROXY_PORT, DX_PROXY_USER, DX_PROXY_PASSWORD);
		api.putRequestHeader(API_KEY, API_KEY_VALUE);
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
        ocrData.outFoloderPath = getTgtFolderPath(ocrData);
        
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
		api.url = DX_URL + String.format("xxxx/%s/xxx", ocrData.unitId);
		api.method = "GET";
		api.setProxy(DX_PROXY_HOST, DX_PROXY_PORT, DX_PROXY_USER, DX_PROXY_PASSWORD);
		api.putRequestHeader(API_KEY, API_KEY_VALUE);
		api.saveFile = csvFilePath;	//フルパス
		//---------------------------------------
		//HTTP request process
		//---------------------------------------
		int res;
		try {
			res = api.download(ocrData.csvFileName);
		} catch (Exception e) {
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
		
     	try {
			convertCSV(ocrData);
		} catch (Throwable e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
     	
     	postOcrProcess(ocrData);
     	
        System.out.println("■exportResultCSV: end");
     	return;
	}

	//OCRデータの出力先フォルダパスを取得
	private String getTgtFolderPath(OcrDataFormBean ocrData) {
		String teigiFolder = OUTPUT_PATH + ocrData.docsetName + "\\" + ocrData.unitName;
		
		//サブフォルダ(定義名＋日時)作成
		//...
				
		return null;
	}

    private ArrayList<ArrayList<String>> parseCSV(String fileName) {
    	ArrayList<ArrayList<String>> list = null;
        try {

            // 入力CSVファイルの読み込み
            File file= new File(fileName);
            FileInputStream input = new FileInputStream(file);
            InputStreamReader stream= new InputStreamReader(input,"SJIS");
            BufferedReader br = new BufferedReader(stream);

            list = new ArrayList<ArrayList<String>>();
            String line;

            while ((line = br.readLine()) != null) {
	            // 自作メソッド呼び出し
            	ArrayList<String> data = csvSplit(line);
	            list.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
		return list;
	}
    //https://qiita.com/takashi-fki/items/c7095ccb41fe169db5f4
	private ArrayList<String> csvSplit(String line) {

        char c;
        StringBuilder s = new StringBuilder();
        String str;
        ArrayList<String> data = new ArrayList<String>();
        boolean singleQuoteFlg = false;

        for (int i=0; i < line.length(); i++){
            c = line.charAt(i);
            if (c == ',' && !singleQuoteFlg) {
            	str = s.toString().replace("\"","");	//ダブルクォーテーションは外す。
            	System.out.println(s.toString() + ": " + str);
                data.add(str);
                s.delete(0,s.length());
            } else if (c == ',' && singleQuoteFlg) {
                s.append(c);
            } else if (c == '\"') {
                singleQuoteFlg = !singleQuoteFlg;
                s.append(c);
            } else {
                s.append(c);
            }
        }
        if (!singleQuoteFlg) {
        	str = s.toString().replace("\"","");	//ダブルクォーテーションは外す。
        	System.out.println(s.toString() + ": " + str);
            data.add(str);
            s.delete(0,s.length());
        }
        
        return data;
    }
    
    private void convertCSV(OcrDataFormBean ocrData) throws Throwable {
		String teigiName = ocrData.Name;
		String documentName = ocrData.documentName;
		String createdAt = ocrData.createdAt;
		int headerNum = ocrData.headerNum;
		int meisaiNum = ocrData.meisaiNum;
		String[][] convTbl;
		
		System.out.println("■convertCSV: start");
		
		String outputcsvFile = ocrData.outFoloderPath + ocrData.csvFileName;
        ArrayList<ArrayList<String>> list = parseCSV(outputcsvFile);
        int maxRow = list.size();
		int maxCol = 0;
        for (int r=0; r<list.size(); r++) {
        	for (int c=0; c<list.get(r).size(); c++) {
        		System.out.println(list.get(r).get(c));
        	}
        	System.out.println("");		
            if (maxCol < list.get(r).size())
            	maxCol = list.get(r).size();
        }
        //String[] csv_data = csv.get(0).split(",");
        //int maxCol = csv.get(0).split(",").length;	//1行目データ部の列数
        int repeatNum = (maxCol-ocrData.headerNum)/ocrData.meisaiNum;
        int rowWidth = repeatNum * (maxRow-1) + 1;
        int colWidth = ocrData.headerNum + ocrData.meisaiNum;
        
        convTbl = new String[rowWidth][colWidth];
        //CSV2Excelテーブル変換処理
        int r2offset = 0;
        String str;
        for (int r=0; r<maxRow; r++) {
        	if (r == 0) {
        		//1行目のヘッダ(カラム)
        		for (int c=0; c<colWidth; c++) {
        			convTbl[r][c] = list.get(r).get(c);
        		}
        	} else {
        		//加工前データの変換
        		int r2;
        		for (int p=0; p<repeatNum; p++) {
        			r2 = (r-1)*repeatNum + 1 + p - r2offset;
        			//ヘッダデータ
        			for (int c=0; c<headerNum; c++) {
	        			convTbl[r2][c] = list.get(r).get(c);
        			}
        			//明細データ
        			for (int c=0; c<meisaiNum; c++) {
        				str = list.get(r).get(headerNum + p*meisaiNum);
        				if (str.equals("") == true) {
        					//convTbl.splice(r2,1);	//データ削除
        					r2offset++;	//1行戻すためのオフセット加算
        					rowWidth--;	//データ削除分のRowWidthを更新
        					break;
        				}
        				str = list.get(r).get(c + headerNum + p*meisaiNum);
        				convTbl[r2][c + headerNum] = str;
        			}
        		}
        	
        	}
        }
        
    	//RirekiDBへの履歴データ書き込み
    	//Excel形式で保存 / 履歴テーブルDBに書き込み
    	//Excelオープン
    	//String xlsPath = ".\\templete\\" + documentName + ".xlsx";
        String xlsPath = documentName + ".xlsx";
        Workbook excel = WorkbookFactory.create(new File(xlsPath));
        Sheet sheet = excel.getSheetAt(0);	//1シート目
        Row row;
        Cell cell;
        boolean resultFlag;
        String resultMsg;
        String value;
        for (int rowIdx=1; rowIdx<rowWidth; rowIdx++) {	//データ2行目（明細）から開始
        	resultFlag = false;
        	resultMsg = "";
        	String fields = "COL0,COL1,COL2;";
        	String values = "'" + createdAt + "','" + teigiName + "','" + rowIdx + "'";
		    row = sheet.createRow(rowIdx);	//行の生成
        	for (int colIdx=0; colIdx<colWidth; colIdx++) {
    	        //row = sheet.getRow(rowIdx);
        		//cellValue = row.getCell(colIdx);
	        	value = convTbl[rowIdx][colIdx].trim();
	        	convTbl[rowIdx][colIdx] = value;
			    cell = row.createCell(colIdx);	//セルの生成
			    cell.setCellValue(value);
	        	
	        	//通常データは、4列目（+3）から挿入
	        	fields = fields + "COL" + (colIdx+3) + ",";
	        	values = values + "'" + value + "'";
	    	}
        }
        //XLSXのファイル保存
		String outputcsvPath = ocrData.outFoloderPath + ocrData.csvFileName;
        String outFilePath = outputcsvPath.replace(".csv",".xlsx");
        System.out.println("  XLSXファイル保存: " + outFilePath);
	    FileOutputStream out = null;
	    out = new FileOutputStream(outFilePath);
	    excel.write(out);
	    
	    //CSVファイル削除
	}
	
	private void postOcrProcess(OcrDataFormBean ocrData) {
        System.out.println("■postOcrProcess: start");
        //出力先フォルダ取得（なければ作成）
        String outputFolderPath = getTgtFolderPath(ocrData);
        
        //読込画像ファイルを取得
        String uploadFilePath = ocrData.uploadFilePath;	//pdf
        Path p1 = Paths.get(uploadFilePath);
        String fileName = p1.getFileName().toString();
        String copyToFile = OUTPUT_PATH + fileName;
        //pdf回転変換
        
        
        
        System.out.println("■postOcrProcess: end");
		
	}


	
}
