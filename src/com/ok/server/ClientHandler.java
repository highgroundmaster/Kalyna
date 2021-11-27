package com.ok.server;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class ClientHandler implements Runnable{

    private Socket Sock;
    public ClientHandler(Socket sock ){
        Sock = sock;
    }

    private BufferedReader br;
    private BufferedWriter bw;
    private InputStream is;
    private OutputStream os;

    @Override
    public void run() {
        try {
            System.out.println(" got here  ");

            is = Sock.getInputStream();
            os = Sock.getOutputStream();
            br = new BufferedReader(new InputStreamReader(is));
            bw = new BufferedWriter(new OutputStreamWriter(os));

            String command = br.readLine();
            System.out.println("got command " + command);
            switch (command){
                case "LOGIN":
                    login();
                    break;
                case "SEND":
                    sendPacket();
                    break;
                case "PBK":
                    getPBK();
                    break;
                case "USR":
                    getUsername();
                    break;
                default:
                    invalidCommand();
                    break;
            }

        }catch (IOException e){
            e.printStackTrace();
            if(!Sock.isClosed()) {
                try {
                    Sock.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void getUsername() throws IOException{
        String Base64PublicKey = br.readLine();
        String username = ChatServer.clientList.getUsername(Base64PublicKey);
        if(username == null){
            bw.write("INVALID\n");
        }else {
            bw.write("GOT\n");
            bw.write(username + "\n");
        }
        bw.flush();
        Sock.close();
    }

    private void getPBK() throws IOException{
        String username = br.readLine();
        String pub = ChatServer.clientList.getPBK(username);
        if(pub == null){
            bw.write("DNE\n");
        }else {
            bw.write("GOT\n");
            bw.write(pub + "\n");
        }
        bw.flush();
        Sock.close();
    }

    private void invalidCommand() throws IOException {
        bw.write("INVALID\n");
        bw.flush();
        Sock.close();
    }

    private void sendPacket() throws IOException {
        String Base64PublicKey = br.readLine();
        System.out.println("sending packet ");
        byte[] publicKey;
        try {
            publicKey = Base64.getDecoder().decode(Base64PublicKey);
            int len = Integer.parseInt( br.readLine() );
            System.out.println(" got len " + len);
            byte[] packetData = ReadNBytes(len);
            System.out.println(" got packet");
            boolean successes = ChatServer.clientList.SendPacket(packetData,Base64PublicKey);
            if(successes) {
                bw.write("ok\n");
            }
            else {
                bw.write("FAIL\n");
            }
            bw.flush();
            System.out.println("done sending");
        }catch (IllegalArgumentException e){
            invalidCommand();
        }
    }

    private void login() throws IOException{
        String username = br.readLine();
        String Base64PublicKey = br.readLine();

        System.out.println(" new User login " + username + " pbk = " + Base64PublicKey.substring(0,10));
        ChatServer.clientList.LoginClient(Sock,Base64PublicKey,username);

    }

    private byte[] ReadNBytes( int n) throws IOException {
        byte[] out  = new byte[n];
        int pos     = 0;

        while (pos < n){
            byte[] tmp  = new byte[Math.min((n - pos),is.available())];
            int len     = is.read(tmp);

            System.arraycopy(tmp,0,out,pos,len);
            pos += len;
        }
        return out;
    }
}
