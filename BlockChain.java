import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BlockChain {

    private static ArrayList<Block> blockChain;
    private static int sizeOfBlockChain;
    private ArrayList<Sender> senders;
    private  ArrayList<String> nameOfSenders;
    private static int blockMiningDifficulty = 5;

    public BlockChain(){
        blockChain = new ArrayList<>();
        sizeOfBlockChain = 0;
        senders = new ArrayList<>();
        nameOfSenders = new ArrayList<>();
    }

    public static BlockChain fromFile(String fileName)  {
        BlockChain blockChainFromFile = new BlockChain();
        ArrayList<String> list = new ArrayList<>();

        //Reading data from file
        try {
            BufferedReader in = new BufferedReader(new FileReader("bitcoinBank.txt"));
            String str;
            while ((str = in.readLine()) != null){
                list.add(str);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Seperating data into blocks in lists
        List<List<String>> data = new ArrayList<List<String>>();
        int indexInFileArray = 0;
        for(int j = 0 ; j<list.size()/7 ; ++j){
            ArrayList<String> dataPieces = new ArrayList<String>();
            for(int i=0; i<7 ; ++i){
                dataPieces.add(i, list.get(indexInFileArray));
                ++indexInFileArray;
            }
            data.add(dataPieces);
        }

        //Converting data into arguements creating blocks and adding the, to chain
        for(List<String> blockArguements : data){
            int blockIndex = Integer.parseInt(blockArguements.get(0));
            Timestamp time = new Timestamp(Long.parseLong(blockArguements.get(1)));
            Transaction trans = new Transaction(blockArguements.get(2), blockArguements.get(3), Integer.parseInt(blockArguements.get(4)));
            String nonce = blockArguements.get(5);
            String hash = blockArguements.get(6);

            Block block = new Block(blockIndex, time, trans, nonce, hash);
            if(sizeOfBlockChain == 0){
                block.setPreviousHash("00000");
            }
            else{
                String prevHash = "";
                try {
                    prevHash = Sha1.hash(blockChain.get(sizeOfBlockChain-1).toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                block.setPreviousHash(prevHash);
            }

            blockChain.add(block);
            ++sizeOfBlockChain;
        }
        return blockChainFromFile;
    }

    public static int getBalance(String username){
        int userBalance = 0;
        for(Block block : blockChain){
            if(block.getTransaction().getReceiver().equals(username)){
                userBalance = userBalance + block.getTransaction().getAmount();
            }
            if(block.getTransaction().getSender().equals(username)){
                userBalance = userBalance - block.getTransaction().getAmount();
            }
        }
        return userBalance;
    }
/*
    public void toFile(String fileName){

    }
*/
    public boolean validateBlockchain(){
        boolean isValid = true;
        int index = 0;
        for(int i = 0 ; i<blockChain.size()-1 ; ++i){
            if(! nameOfSenders.contains(blockChain.get(i+1).getTransaction().getSender())){
                nameOfSenders.add(blockChain.get(i+1).getTransaction().getSender());
                senders.add(new Sender(blockChain.get(i+1).getTransaction().getSender()));

            }
        }

        for(int i = 0 ; i<blockChain.size()-1 ; ++i){
            if(! blockChain.get(i+1).getPreviousHash().equals(blockChain.get(i).getHash())){
                isValid = false;
            }
            if(blockChain.get(i).getIndex() != i){
                isValid = false;
            }

        }

        for(String sender : nameOfSenders){
            int balanceCheck = getBalance(sender);
            for(Sender s : senders){
                s.setBalance(balanceCheck);
            }
            if(balanceCheck<0){
                isValid = false;
            }
        }

        return isValid;
    }

    public void add(Block block){
        blockChain.add(block);
    }

    public static boolean checkNewTransaction(String sender, int amount){

        int userBalance = getBalance(sender);
        return userBalance>=amount;
    }


    public static void main(String[] args){
        BlockChain b = fromFile("bitconBank.txt");
        System.out.println("Validating block chain from file: ");
        boolean validChain = b.validateBlockchain();
        if(validChain){
            System.out.println("Blockchain from file is valid.");
            boolean flag = true;
            while (flag) {
                Scanner userIn = new Scanner(System.in);
                System.out.println("Enter a new transaction.");
                System.out.println("Sender: ");
                System.out.println("Reciever: ");
                System.out.println("Amount: ");
                String sender = userIn.nextLine();
                String reciever = userIn.nextLine();
                int amount = Integer.parseInt(userIn.nextLine());

                boolean transaction = checkNewTransaction(sender, amount);
                if(transaction){
                    System.out.println("Transaction is valid.");
                    Transaction t = new Transaction(sender, reciever, amount);
                    Timestamp time = new Timestamp(System.currentTimeMillis());
                    int index;
                    if(sizeOfBlockChain==0){
                        index = 0;
                    }
                    else{
                        index = sizeOfBlockChain;
                    }
                    String prevHash;
                    if(sizeOfBlockChain==0){
                        prevHash = "00000";
                    }
                    else{
                        prevHash = blockChain.get(sizeOfBlockChain-1).getPreviousHash();
                    }
                    Block blockToBeAdded = new Block(index, time, t, prevHash);
                    System.out.println("Mining block: ");
                    blockToBeAdded.mineBlock(blockMiningDifficulty);
                    b.add(blockToBeAdded);
                    System.out.println("Block Added!");
                }
                else{
                    System.out.println("Invalid transaction.");
                }


                System.out.println("Would you like to enter another transaction? (y=yes n=no)");
                String input = userIn.nextLine();
                if(input.equals("n") || input.equals("no")){
                    flag = false;
                }
            }

            for(Block bl : blockChain){
                System.out.println(bl.toString());
                System.out.println(bl.getHash());
                System.out.println(bl.getPreviousHash());
                System.out.println();
            }
            System.out.println(validChain);
        }else{
            System.out.println("Invalid blockchain from file.");
        }
    }
}
