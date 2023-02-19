package sample;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
 
//https://style.potepan.com/articles/27539.html#JavaExcelApache_POI
public class poiTest {
    public static void main(String[] args) throws EncryptedDocumentException, IOException{
    	try {
	        // Excelファイルへアクセス(eclipse上でパスをしていないとプロジェクトパスになる)
    		//String filePath = "User.xlsx";
    		String filePath = "AAAA.xlsx";
	        Workbook excel = WorkbookFactory.create(new File(filePath));
	    
	        // シート名を取得
	        //Sheet sheet = excel.getSheet("user_data");
	        Sheet sheet = excel.getSheetAt(0);
	    
	        // 0行目を取得
	        Row row = sheet.getRow(1);
	    
	        // 0番目のセルの値を取得
	        Cell cell_name = row.getCell(0);
	        // 1番目のセルの値を取得
	        Cell cell_sex = row.getCell(1);
	        // 2番目のセルの値を取得
	        Cell cell_age = row.getCell(2);
	    
	        // セルの値を文字列として取得
	        String value_name = cell_name.getStringCellValue();
	        String value_sex = cell_sex.getStringCellValue();
	        String value_age = cell_age.getStringCellValue();
	    
	        // 文字列を結果として表示
	        System.out.println("value_name ： " + value_name);
	        System.out.println("value_sex ： " + value_sex);
	        System.out.println("value_age ： " + value_age);
    	} catch (Throwable t) {
    	    //LOG.error("Failure during static initialization", t);
    	    throw t;
    	 }
    }
}
