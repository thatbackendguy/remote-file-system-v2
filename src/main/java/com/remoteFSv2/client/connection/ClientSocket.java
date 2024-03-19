package com.remoteFSv2.client.connection;

import com.remoteFSv2.client.Client;
import static com.remoteFSv2.utils.Config.*;
import static com.remoteFSv2.utils.Constants.*;

import java.io.IOException;
import java.net.Socket;

public class ClientSocket
{
    private ClientSocket() {}

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
            var socket = new Socket(HOST, CLIENT_PORT);

            return socket;
        } catch(IOException e)
        {
            System.out.println(CLIENT + CONNECTION_ERROR);

            Client.logger.error(CONNECTION_ERROR);

            System.out.println("[Client] Retrying again in 5 seconds...");

            try
            {

                Thread.sleep(5000);

            } catch(InterruptedException ex)
            {
                System.out.println(CLIENT + "Error! See logs for more info...");

                Client.logger.error(Client.fatal, ex.getMessage());

            }
            try
            {
                Client.start();
            } catch(NullPointerException | IOException exception)
            {
                System.out.println(CLIENT + "Error starting client!");

                Client.logger.error(Client.fatal, "Error starting client!");
            }
        }

        return null;
    }
}
