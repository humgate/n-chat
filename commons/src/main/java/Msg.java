public class Msg {
    private String client;
    private int num;
    private String message;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Msg(String client, int num, String message) {
        this.client = client;
        this.num = num;
        this.message = message;
    }



    @Override
    public String toString() {
        return client + ' ' + num + " " + message;
    }
}
