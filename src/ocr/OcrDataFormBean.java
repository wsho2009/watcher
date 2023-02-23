package ocr;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDataFormBean implements Serializable {
//import com.fasterxml.jackson.annotation.JsonProperty;
	//DB接続
	String URL;
	String USER;
	String PASS;
	
	//OcrData
	@JsonProperty("unitId")
	String unitId;
	@JsonProperty("unitName")
	String unitName;
	@JsonProperty("uploadFilePath")
	String uploadFilePath;
	@JsonProperty("status")
	String status;
	@JsonProperty("csvFileName")
	String csvFileName;
	@JsonProperty("createdAt")
	String createdAt;
	@JsonProperty("linkUrl")
	String linkUrl;
	@JsonProperty("type")
	int type;
	
	//OcrForm
	@JsonProperty("No") String No;
    @JsonProperty("Name") String Name;
    @JsonProperty("documentId") String documentId;
    @JsonProperty("documentName") String documentName;
    @JsonProperty("docsetId") String docsetId;
    @JsonProperty("docsetName") String docsetName;
    
    @JsonProperty("headerNum") int headerNum;
    @JsonProperty("meisaiNum") int meisaiNum;
    @JsonProperty("colHinemei") int colHinemei;
    @JsonProperty("colSuryo") int colSuryo;
    @JsonProperty("colTanka") int colTanka;
    @JsonProperty("colKingaku") int colKingaku;
    
    @JsonProperty("mailFlag") int mailFlag;	//DBなし
	@JsonProperty("outFoloderPath") String outFoloderPath;	//DBなし
        
	public OcrDataFormBean() {
		//接続情報取得
		ResourceBundle rb = ResourceBundle.getBundle("prop");
		URL = rb.getString("URL");
		USER = rb.getString("USER");
		PASS = rb.getString("PASS");
    }

	public void setNo(String No) {this.No = No;	}
	public void setName(String Name) {this.Name = Name;	}
	public void setDocumentId(String documentId) {this.documentId = documentId;	}
	public void setDocumentName(String documentName)  {this.documentName = documentName;}
	public void setDocsetId(String docsetId) {this.docsetId = docsetId;}
	public void setDocsetName(String docsetName)  {this.docsetName = docsetName; }

	public void updateFromUploadFilePath() throws SQLException {
		String sql = "UPDATE OCRDATATABLE SET UNIT_ID=?,UNIT_NAME=?,STATUS?,CREATEDAT=TO_DATE(?,'YYYY/MM/DD HH:MM:SS') " + 
				 	 "WHERE UPLOAD_PATH=?";
	
		//接続処理
		Connection conn = null;
	   try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(this.URL, this.USER, this.PASS);
			System.out.println(sql);
			conn.setAutoCommit(false);
	       
	       PreparedStatement ps = conn.prepareStatement(sql);
	       int i=1;
	       ps.setString(i++, this.unitId);
	       ps.setString(i++, this.unitName);
	       ps.setString(i++, this.status);
	       ps.setString(i++, this.createdAt);
	       ps.setString(i++, this.uploadFilePath);
	       
	       ps.executeUpdate();
	       conn.commit();
	   } catch (Exception e) {
	       e.printStackTrace();
	   } finally {
			// DB接続を解除
			if (conn != null) {
				conn.close();
			}
	   }
		
	}
}
