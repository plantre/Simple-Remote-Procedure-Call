package org.plantre;

public class Hello implements HelloService {
    private String message;
    private String description;


    public Hello() {
    }

    @Override
    public String hello(Hello hello) {
        return "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
