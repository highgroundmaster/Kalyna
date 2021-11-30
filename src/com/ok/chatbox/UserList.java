package com.ok.chatbox;

import com.ok.kalyna.Kalyna;
import com.ok.kalyna.KalynaHash;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class UserList {

    private static final Scanner scn = new Scanner(System.in);
    public static void showUserList(){
        String err_msg = "";
        while (true) {
            ConsoleUtil.CLS();
            byte[] pbkHash = KalynaHash.Hash(ChatClient.chatCipher.getPublicKey().getEncoded(), 16 );
            String hashB64 = Base64.getEncoder().encodeToString(pbkHash);
            System.out.println("\nPublic Key Hash " +  hashB64);
            System.out.println(" -- chat with -- ");
            System.out.print(err_msg);
            System.out.println(" Enter Username :-  (\"exit\" to exit)");
            String username = scn.nextLine();
            if(username.equals("exit"))
                System.exit(0);
            try {
                String PBK = ChatClient.rest.GetPublicKey(username);
                if(PBK != null){
                    System.out.println("got PBK");
                    byte[] hash = KalynaHash.Hash( Base64.getDecoder().decode(PBK),16 );
                    System.out.println("Hash(PBK) = " + Base64.getEncoder().encodeToString(hash));
                    System.out.println("continue? (y/n)");
                    if(scn.nextLine().toLowerCase().charAt(0) == 'y')
                        ChatConnector.ChatWith(username,PBK);
                }else{
                    err_msg = " invalid Username \n";
                }
            } catch (IOException e) {
                e.printStackTrace();
                err_msg = "IO Exception while getting Public Key of " + username + "\n";
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                err_msg = "NO Such Algo Exception while " + e.getLocalizedMessage() +  "  \n";
            }
        }
    }
}
