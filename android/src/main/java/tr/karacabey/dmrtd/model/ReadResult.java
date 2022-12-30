package tr.karacabey.dmrtd.model;

import java.util.HashMap;
import java.util.Map;

public class ReadResult {
    final boolean isSuccess;
    final String message;
    final EDocument document;

    public ReadResult(boolean isSuccess, String message, EDocument document) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.document = document;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public EDocument getDocument() {
        return document;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> rawMap = new HashMap<>();

        rawMap.put("isSuccess", isSuccess);
        rawMap.put("message", message);

        if(document!=null)
       rawMap.put("document", document.toMap());

        return rawMap;
    }


}
