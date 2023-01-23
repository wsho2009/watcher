package fax;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DeleteFile {
	int DaysOfDeletion;
	String backupPath;
	long deleteCount;
	long totalCount;
	long deleteSize;
	long totalSize;
	int mbSize;
	
	public DeleteFile() {
		DaysOfDeletion = 60;	//日
		backupPath = "\\backup";
		deleteCount = 0;
		totalCount = 0;
		deleteSize = 0;
		totalSize = 0;
		mbSize = 0;
	}
	
	public void run(String targetPath) {
		System.out.println("「" + targetPath + "」フォルダ内のファイル更新日が"
							+ DaysOfDeletion + "日以上のものを削除します。");
		scanDeleteDile(targetPath, targetPath + backupPath, true);
		System.out.println("「" + targetPath + "」フォルダ内のファイル更新日"
				+ DaysOfDeletion + "日以上のものを削除しました(ファイル数: " + deleteCount +")。");
		totalCount = totalCount - deleteCount;
		totalSize = totalSize - deleteSize;
		mbSize = Math.round(totalSize/(1024*1024));
		System.out.println("トータルファイル数:" + totalCount + " 削除ファイル数: " + deleteCount
				+ " サイズ(MB): " + mbSize);
	}

	private void scanDeleteDile(String targetPath, String backupPath, boolean deleteFlag) {
		//指定ディレクトリ肺のファイルのみ(またはディレクトリのみ)を取得
        File file1 = new File(targetPath);
        File fileArray1[] = file1.listFiles();
        
        for (File f: fileArray1){
            // フォルダ
            if(f.isDirectory()) {
                System.out.println(f.toString());//フォルダを表示
                scanDeleteDile(f.toString(), backupPath, true);
            }
            
            // ファイル
            if(f.isFile()) {
                System.out.println(f.toString());//ファイルを表示
                String fileName = f.toString();
                String extension = fileName.substring(fileName.length()-3);	//拡張子：後ろから3文字
                if (extension.equals("pdf") == true) {
                    totalSize = totalSize + f.length();
                    totalCount++;
                    if (deleteFlag == true) {
						//https://qiita.com/fumikomatsu/items/b98cc4d0dee782323096
                    	// 現在日時を取得
                        Calendar st = Calendar.getInstance();//Calendarクラスで現在日時を取得
                        st.add(Calendar.DATE, -60);          //現在値を取得(60日前)
                        Date start = st.getTime();           //Dateに直す
                        // ファイルの更新日時
                        Long lastModified = f.lastModified();
                        Date koushin = new Date(lastModified);

                        if(start.compareTo(koushin) < -1 ){//compareToで比較
                        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            String update_time = simpleDateFormat.format(koushin);
                            System.out.println(update_time+"： "+fileName);
                        }
                        
                    }
                }
            }
        }
	}

}
