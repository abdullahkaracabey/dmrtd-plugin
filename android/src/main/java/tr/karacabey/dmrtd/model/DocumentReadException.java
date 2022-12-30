package tr.karacabey.dmrtd.model;

public class DocumentReadException extends Exception{

    private String code;

    public  DocumentReadException( String code){
        this.code= code;
    }


    public String getCode(){
        return this.code;
    }


}
