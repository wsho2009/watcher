package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyUtils {

	//public MyUtils() {
	//	// TODO 自動生成されたコンストラクター・スタブ
	//}
	public static void SystemLogPrint(String msg) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.printf("[%s]%s\n", dateFormat.format(new Date()), msg);
	}

	public static void SystemErrPrint(String msg) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.err.printf("[%s]%s\n", dateFormat.format(new Date()), msg);
	}

}
