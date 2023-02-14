package ocr;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDataFormBean implements Serializable {
//import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    @JsonProperty("mailFlag") int mailFlag;	//DBなし
	@JsonProperty("outFoloderPath") String outFoloderPath;	//DBなし
        
	//public OcrDataFormBean() {
    //}

	public void setNo(String No) {this.No = No;	}
	public void setName(String Name) {this.Name = Name;	}
	public void setDocumentId(String documentId) {this.documentId = documentId;	}
	public void setDocumentName(String documentName)  {this.documentName = documentName;}
	public void setDocsetId(String docsetId) {this.docsetId = docsetId;}
	public void setDocsetName(String docsetName)  {this.docsetName = docsetName; }

	public void updateFromUploadFilePath() {
		// TODO 自動生成されたメソッド・スタブ
		
	}
}
