package fax;

import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ResourceBundle;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import utils.MyUtils;

public class ScanFile {
	static String kyoten;
	static String targetPath;
	static String listFile;

	public static void main(String[] args) throws Exception {
		
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		kyoten = rb.getString("KYOTEN");
		targetPath = rb.getString("TARGET_PATH");
		listFile = rb.getString("LIST_FILE");
		//String path = new File(".").getAbsoluteFile().getParent();
		
		//指定ディレクトリ配下のファイルのみ(またはディレクトリ)を取得
		// https://qiita.com/fumikomatsu/items/67f012b364dda4b03bf1
		Path dir = Paths.get(targetPath);
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        
		//fax detetion process
		MyUtils.SystemLogPrint("■ScanFile: scan: " + kyoten);
    	DeleteFile delete = new DeleteFile();
		delete.run(targetPath);
        for (;;) {
            WatchKey watchKey = watcher.take();
            for (WatchEvent<?> event: watchKey.pollEvents()) {
                if (event.kind() == OVERFLOW) continue;
                //新規作成
                if (event.kind() == ENTRY_CREATE) {
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();		//ファイルパス
                    Path src = dir.resolve(name);	//フルパス
                    String fileName = name.toString();
                    System.out.format("%s: %s %s\n", event.kind().name(), src, name);
                    //String extension = fileName.substring(fileName.lastIndexOf("."));	//
                    String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
                    fileName = fileName.substring(0, (fileName.length()-4));	//.拡張子外す
                    if (extension.equals("pdf") == true) {
                    	try {
                    		MyUtils.SystemLogPrint("  ファイル検出...: " + fileName);
							scanProcess(fileName);
						} catch (Throwable e) {
							e.printStackTrace();
						}
                    }
                }
            }
            watchKey.reset();
        }
	}
	static void scanProcess(String fileName) throws Throwable {
		//1
		
		//2
		
		//Excelオープン
    	try {
	        // Excelファイルへアクセス(eclipse上でパスをしていないとプロジェクトパスになる)
	        Workbook excel = WorkbookFactory.create(new File(listFile));
	        // シート名を取得
	        Sheet sheet = excel.getSheet("LIST");
	        String name = "Other";
	        String flag = "0"; 
	        for (int r=1; r<256; r++) {	//r=0: ヘッダ
		        // 0行目を取得
		        Row row = sheet.getRow(r);
		        // 0番目のセルの値を取得
		        //Cell no = row.getCell(0);			//NO
		        Cell cell_name = row.getCell(1);	//NAME
		        name = cell_name.getStringCellValue();
		        Cell cell_fax = row.getCell(2);		//FAX_NO
		        String faxNo = cell_fax.getStringCellValue();
		        Cell cell_flag = row.getCell(3);	//FLAG
		        CellType type = cell_flag.getCellType();
		        if (type == CellType.NUMERIC) {
			        int val;
		        	val = (int)cell_flag.getNumericCellValue();
		        	flag =  String.valueOf(val);
		        } else {
			        flag = cell_flag.getStringCellValue();
		        }
		        if (fileName.equals(faxNo) == true) {
		        	//if (flag == 1) {
		        	if (flag.equals("1") == true) {
		        		MyUtils.SystemLogPrint("  FLAG: " + flag);
		        	}
 		        	break;
		        }
		        if ("Other".equals(name) == true) {
		        	break;
		        }
	        } //for
    	} catch (Throwable t) {
    	    //LOG.error("Failure during static initialization", t);
    	    throw t;
    	}
    	//------------------------------------------------------
        //フォルダ整理（フォルダ存在を確認し、なければフォルダ作成）
        //------------------------------------------------------
        Path dstDir = Paths.get(targetPath + fileName);
    	if (!Files.exists(dstDir)) {
    		Files.createDirectory(dstDir);
    	}
        //------------------------------------------------------
        //フォルダへ退避（doneフォルダへ移動）
        //------------------------------------------------------
    	Path src = Paths.get(targetPath + fileName + ".pdf");
        Path dst = Paths.get(targetPath + fileName + "\\" + fileName + ".pdf");
    	Files.move(src, dst, REPLACE_EXISTING);

    	//------------------------------------------------------
        //添付メール送信
        //------------------------------------------------------

    	//------------------------------------------------------
        //FaxDataTableへ登録
        //------------------------------------------------------

    	//------------------------------------------------------
        //OCR登録処理
        //------------------------------------------------------
	}

	static void uploadProcess(String fileName) throws Throwable {
        //------------------------------------------------------
        //取り込み実行
        //------------------------------------------------------
    	//https://blog.goo.ne.jp/xmldtp/e/beb03fb01fb1d1a2c37db8d69b43dcdd
		Runtime r = Runtime.getRuntime();
		try
		{
			//コマンドラインから****.vbsを呼び出せる。
			String[] cmdList = new String[6];
			cmdList[0]	=	"cmd";
			cmdList[1]	=	"/c";
			cmdList[2]	=	"test.vbs";			//VBSファイル指定
			cmdList[3]	=	"/file:test.xls";	//Excelファイル指定
			cmdList[4]	=	"/outfname:test.txt";
			cmdList[5]	=	"/msg:123";
			r.exec(cmdList);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
       	
    	//------------------------------------------------------
        //フォルダ整理（フォルダ存在を確認し、なければフォルダ作成）
        //------------------------------------------------------
        Path dstDir = Paths.get(targetPath + "done\\");
    	if (!Files.exists(dstDir)) {
    		Files.createDirectory(dstDir);
    	}
        //------------------------------------------------------
        //フォルダへ退避（doneフォルダへ移動）
        //------------------------------------------------------
    	Path src = Paths.get(targetPath + fileName + ".pdf");
        Path dst = Paths.get(targetPath + "done\\" + fileName + ".pdf");
    	Files.move(src, dst, REPLACE_EXISTING);
	}

	@SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
}
