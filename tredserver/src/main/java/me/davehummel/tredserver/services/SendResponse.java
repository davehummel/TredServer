package me.davehummel.tredserver.services;

/**
 * Created by dmhum on 2/14/2017.
 */
public class SendResponse {

    private boolean isSuccess;

    private long execTime;

    private String error;


    public boolean isSuccess() {
        return isSuccess;
    }

    public long getExecTime() {
        return execTime;
    }

    public String getError() {
        return error;
    }

    void setSuccess(boolean success) {
        isSuccess = success;
    }

    void setExecTime(long execTime) {
        this.execTime = execTime;
    }

    void setError(String error) {
        this.error = error;
    }
}
