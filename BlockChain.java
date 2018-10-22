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
            BufferedReader in = new BufferedReader(new FileReader(fileName));
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
            String stringStamp = blockArguements.get(1);
            Timestamp time;
            if(stringStamp.contains("-")){
                int year = Integer.parseInt(stringStamp.substring(0, 4));
                int month = Integer.parseInt(stringStamp.substring(5, 7));
                int day = Integer.parseInt(stringStamp.substring(8, 10));
                int hour = Integer.parseInt(stringStamp.substring(11, 13));
                int min = Integer.parseInt(stringStamp.substring(14, 16));
                int sec = Integer.parseInt(stringStamp.substring(17, 19));
                int nano = Integer.parseInt(stringStamp.substring(20));
                time = new Timestamp(year,month,day,hour,min,sec,nano);
            }
            else{
                time = new Timestamp(Long.parseLong(blockArguements.get(1)));
            }

            Transaction trans = new Transaction(blockArguements.get(2), blockArguements.get(3), Integer.parseInt(blockArguements.get(4)));
            String nonce = blockArguements.get(5);
            String hash = blockArguements.get(6);

            Block block = new Block(blockIndex, time, trans, nonce, hash);
            if(sizeOfBlockChain == 0){
                block.setPreviousHash("00000");
            }
            else{
                String prevHash = "";
                prevHash = blockChain.get(sizeOfBlockChain-1).getHash();

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
                System.out.println("invalid prev hash");
            }
            if(blockChain.get(i).getIndex() != i){
                isValid = false;
                System.out.println("Invalid index");
            }

        }

        for(String sender : nameOfSenders){
            int balanceCheck = getBalance(sender);
            for(Sender s : senders){
                s.setBalance(balanceCheck);
            }
            if(balanceCheck<0){
                isValid = false;
                System.out.println("invalid balance send");
            }
        }

        return isValid;
    }

    public void add(Block block){
        blockChain.add(block);
        ++sizeOfBlockChain;
    }

    public static boolean checkNewTransaction(String sender, int amount){

        int userBalance = getBalance(sender);
        return userBalance>=amount;
    }

    public static void toFile(String fileName){
        try {
            PrintWriter writer = new PrintWriter(fileName);
            int count = 0;
            while(count < sizeOfBlockChain){
                for (Block bl : blockChain) {
                    writer.println(bl.getIndex());
                    Timestamp time = bl.getTimestamp();
                    writer.println(bl.getTimestamp().toString());
                    Transaction t = bl.getTransaction();
                    writer.println(t.getSender());
                    writer.println(t.getReceiver());
                    writer.println(t.getAmount());
                    writer.println(bl.getNonce());
                    writer.println(bl.getHash());
                }
                ++count;
                writer.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);

        String fileName = "";
        System.out.println("Enter the file name for the blockchain (do NOT include .txt): ");
        fileName = scan.nextLine();
        BlockChain b = fromFile(fileName + ".txt");
        System.out.println("Validating block chain from file: ");
        boolean validChain = b.validateBlockchain();
        if(validChain){
            System.out.println("Blockchain from file is valid.");
            boolean flag = true;
            while (flag) {
                Scanner userIn = new Scanner(System.in);
                System.out.println("Would you like to enter a(nother) transaction? (y=yes n=no)");
                String input = userIn.nextLine();
                if(input.equals("y") || input.equals("yes")){
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
                            prevHash = blockChain.get(sizeOfBlockChain-1).getHash();
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
                }
                else{
                    flag = false;
                }

            }

            System.out.println("Writing blockchain to file.");
            toFile(fileName + "_jdank056.txt");

            System.out.println(validChain);
        }else{
            System.out.println("Invalid blockchain from file.");
        }
    }
}