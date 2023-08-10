package dev.tarcisio.minebox.advice;

import java.util.Date;
import java.util.List;

public class ArgumentValidateMessage {
    private int statusCode;
    private Date timestamp;
    private List<String> message;
    private String description;

    public ArgumentValidateMessage(int statusCode, Date timestamp, List<String> message, String description) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.description = description;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<String> getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
 
}
