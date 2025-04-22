import java.io.Serializable;

public class Message implements Serializable {
    private String username, message;

    public Message(String username, String message) {
        this.username = username;

        this.message = message;
    }

    public String getUsername() {
        return username;
    }


    public String getMessage() {
        return message;
    }
}

//TODO gestire logout con frame aperti