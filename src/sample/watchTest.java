package sample;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class watchTest {

	public static void main(String[] args) throws Exception {
		// https://qiita.com/fumikomatsu/items/67f012b364dda4b03bf1
		String targetPath = "D:\\pleiades\\2022-06\\workspace\\watcher\\watcher\\";
		//String path = new File(".").getAbsoluteFile().getParent();

		Path dir = Paths.get(targetPath);
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        for (;;) {
            WatchKey watchKey = watcher.take();
            for (WatchEvent<?> event: watchKey.pollEvents()) {
                if (event.kind() == OVERFLOW) continue;
                //新規作成
                if (event.kind() == ENTRY_CREATE) {
                    WatchEvent<Path> ev = cast(event);
                    Path name = ev.context();
                    Path src = dir.resolve(name);
                    System.out.format("%s: %s\n", event.kind().name(), src);

                    try {
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
	                    Path dst = Paths.get(targetPath + "done\\" + name);
                    	Files.move(src, dst);
                    } catch(IOException e) {
                    	System.out.println(e);
                    }                
                }
            }
            watchKey.reset();
        }
	}

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
}
