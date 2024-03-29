package com.remotefsv2.client.connection;

import com.remotefsv2.client.Client;
import static com.remotefsv2.utils.Config.*;
import static com.remotefsv2.utils.Constants.*;

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
            return new Socket(HOST, CLIENT_PORT);
        }
        catch(IOException e)
        {
            System.out.println(CLIENT + CONNECTION_ERROR);

            Client.LOGGER.error(CONNECTION_ERROR);

            System.out.println("[Client] Retrying again in 5 seconds...");

            try
            {

                Thread.sleep(5000);

            } catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt(); // Preserve the interrupted status

                Client.LOGGER.error(Client.FATAL, ex.getMessage());

            }
            try
            {
                Client.start();
            }
            catch(NullPointerException | IOException exception)
            {
                Client.LOGGER.error(Client.FATAL, CONNECTION_ERROR);
            }
        }

        return null;
    }
}
