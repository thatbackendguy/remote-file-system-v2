package com.remoteFSv2.client.connection;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.client.Config;
import com.remoteFSv2.utils.Constants;

import java.io.IOException;
import java.net.Socket;

public class ClientSocket
{
    private ClientSocket(){}
    
    private static ClientSocket clientSocket = null;
    
    public static ClientSocket getInstance()
    {
        if(clientSocket == null)
        {
            clientSocket = new ClientSocket();
        }

        return clientSocket;
    }
    
    public Socket connectServer()
    {
        try
        {
            var socket = new Socket(Config.HOST,  Config.PORT);

            return socket;
        }
        catch(IOException e)
        {
            System.out.println("[Client] Error: " + e.getMessage());

            System.out.println("[Client] Retrying again in 5 seconds...");

            try
            {

                Thread.sleep(5000);

            } catch(InterruptedException ex)
            {
                System.out.println(Constants.CLIENT + "Error!\nStatus: FATAL\nMessage: "+ex.getMessage());
            }
            Client.main(null);
        }
        return null;
    }
}