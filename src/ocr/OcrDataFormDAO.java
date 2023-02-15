/**
 * 
 */
package ocr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * @author PC
 *
 */
public class OcrDataFormDAO {
	
	public OcrDataFormDAO() {
	}

	// インスタンスオブジェクトの生成->返却（コードの簡略化）
	public static OcrDataFormDAO getInstance() {
		return new OcrDataFormDAO();
	}

	public ArrayList<OcrDataFormBean> queryNotComplete() throws SQLException {
		
		String sql = "select o.*,t.* from OCRDATATABLE o, OCRFORMTABLE t " +
					 "where o.STATUS <> 'COMPLETE' and o.UNIT_NAME=t.NAME order by o.CREATEDAT";
        //接続情報取得
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		String URL = rb.getString("URL");
		String USER = rb.getString("USER");
		String PASS = rb.getString("PASS");
		
		//接続処理
		Connection conn = null;
		ArrayList<OcrDataFormBean> list = new ArrayList<OcrDataFormBean>();
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(URL,USER,PASS);
			System.out.println(sql);

			PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            OcrDataFormBean dataform = new OcrDataFormBean();
    		while(rs.next()) {
    			// ユーザIDと名前をBeanクラスへセット
    			dataform.unitId = rs.getString("UNIT_ID");
    			dataform.unitName = rs.getString("UNIT_NAME");
    			dataform.uploadFilePath = rs.getString("UPLOAD_PATH");
    			dataform.status = rs.getString("STATUS");
    			dataform.csvFileName = rs.getString("CSV_FILENAME");
    			dataform.createdAt = rs.getString("CREATEDAT");
    			dataform.linkUrl = rs.getString("LINK_URL");
    			dataform.type = rs.getInt("TYPE");
    			dataform.setNo(rs.getString("NO"));
    			dataform.setName(rs.getString("NAME"));
    			dataform.setDocumentId(rs.getString("DOCUMENT_ID"));
    			dataform.setDocumentName(rs.getString("DOCUMENT_NAME"));
    			dataform.setDocsetId(rs.getString("DOCSET_ID"));
    			dataform.setDocsetName(rs.getString("DOCSET_NAME"));
            	// リストにBeanクラスごと格納
    			list.add(dataform);
    			//Beanクラスを初期化
    			dataform = new OcrDataFormBean();
    		}
			
		} catch(SQLException sql_e) {
			// エラーハンドリング
			System.out.println("sql実行失敗");
			sql_e.printStackTrace();
			
		} catch(ClassNotFoundException e) {
			// エラーハンドリング
			System.out.println("JDBCドライバ関連エラー");
			e.printStackTrace();
			
		} finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
		}
		// リストを返す
		return list;
	}
}
