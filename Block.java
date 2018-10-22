import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

public class Block {

    private int index;
    private java.sql.Timestamp timestamp;
    private Transaction transaction;
    private String nonce;
    private int intNonce;
    private String previousHash, hash;
    private int difficulty = 5;
    private int numTries = 0;

    //Constructor for blocks from file
    public Block(int index, Timestamp timestamp, Transaction transaction, String nonce, String hash){
        this.index = index;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.nonce = nonce;
        this.hash = hash;
    }

    //Constructor for new blocks
    public Block(int index, Timestamp timestamp, Transaction transaction, String previousHash){
        this.index = index;
        this.timestamp = timestamp;
        this.transaction = transaction;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    public Timestamp getTimestamp(){
        return timestamp;
    }

    public int getIndex(){
        return index;
    }

    public String getPreviousHash(){
        return previousHash;
    }

    public String getHash(){
        return hash;
    }

    public String getNonce(){
        return nonce;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setPreviousHash(String previousHash){
        this.previousHash = previousHash;
    }

    public String calculateHash() {
        nonce = Integer.toString(intNonce);
        String data = this.toString();
        String calcHash = null;
        try {
            calcHash = Sha1.hash(data);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return calcHash;
    }


    public void mineBlock(int difficulty){
        String key = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring( 0, difficulty).equals(key)) {
            intNonce++;
            numTries++;
            hash = calculateHash();
        }
        System.out.println("Block mined! : " + hash);
        System.out.println("Number of tries to find the nonce: " + numTries);
    }

    public String toString(){
        return timestamp.toString() + ":" + transaction.toString() + "." + nonce + previousHash;
    }

}