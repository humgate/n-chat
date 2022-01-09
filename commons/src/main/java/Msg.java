public class Msg {
    private String client;
    private String message;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Msg(String client, String message) {
        this.client = client;
        this.message = message;
    }

    @Override
    public String toString() {
        return client + " "+ message;
    }
}
