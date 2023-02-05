package ocr;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {

	//public TimerTest() {
	//}

	public static void main(String[] args) {
        Timer timer = new Timer(); // 今回追加する処理
        TimerTask task = new TimerTask() {
            int count = 0;
            public void run() {
                // 定期的に実行したい処理
                count++;
                System.out.println(count + "回目のタスクが実行されました。");
            }
        };
        timer.scheduleAtFixedRate(task,0,3000); // 今回追加する処理（3000ms間隔）
    }
}
