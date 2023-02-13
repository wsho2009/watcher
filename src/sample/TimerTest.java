  package sample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ocr.OcrProcess;

public class TimerTest {

	//public TimerTest() {
	//}
	static boolean ocrExlusiveFlag;

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
}
