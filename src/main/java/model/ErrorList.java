package model;

public class ErrorList {
    private String path, errMsg;

    public String getPath() {
        return path;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setData(String errMsg, String path){
        this.path = path;
        this.errMsg = errMsg;
    }

}
