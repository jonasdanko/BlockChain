public class Sender {

    private int balance;
    private String name;

    public Sender(String name){
        this.name = name;
        balance = 0;
    }

    public String getName() {
        return name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
