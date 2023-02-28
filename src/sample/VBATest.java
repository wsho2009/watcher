package sample;

import java.io.IOException;

public class VBATest {

	public VBATest() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public static void main(String[] args) {
    	//https://blog.goo.ne.jp/xmldtp/e/beb03fb01fb1d1a2c37db8d69b43dcdd
		System.out.println("start");
		Runtime r = Runtime.getRuntime();
		try {
			//コマンドラインから****.vbsを呼び出せる。
			String[] cmdList = new String[7];
			cmdList[0]	=	"cmd";
			cmdList[1]	=	"/c";
			cmdList[2]	=	"test.vbs";				//VBSファイル指定
			cmdList[3]	=	"/file:test.xlsm";		//VBSファイル指定
			cmdList[4]	=	"/method:run";			//VBSのメソッド指定
			cmdList[5]	=	"/outfname:test.txt";	//VBAへの引数
			cmdList[6]	=	"/msg:123";				//VBAへの引数
			//System.out.println(cmdList);
			r.exec(cmdList);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		System.out.println("end");
	}
}
