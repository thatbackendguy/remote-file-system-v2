package com.remoteFSv2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadPoolExecutor;

import com.remoteFSv2.utils.Config;
import com.remoteFSv2.server.controller.FileSystem;
import com.remoteFSv2.server.controller.User;
import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.server.handler.ClientHandler;
import com.remoteFSv2.utils.Constants;

public class Server
{
    public static void main(String[] args)
    {
        ServerSocket serverSocket = null;

        ThreadPoolExecutor threadPoolExecutor = null;
        
        ClientConnection clientConnection = null;

        try
        {
            serverSocket = new ServerSocket(Config.SERVER_PORT);

            System.out.println(Constants.SERVER + Constants.SERVER_START_SUCCESS + Config.SERVER_PORT);

            threadPoolExecutor = new ThreadPoolExecutor(Config.CORE_POOL_SIZE, Config.MAXIMUM_POOL_SIZE, Config.KEEP_ALIVE_TIME, Config.UNIT, Config.WORKERS);

            while(true)
            {
                var clientSocket = serverSocket.accept();

                clientConnection = new ClientConnection(clientSocket);

                var fileSystemController = new FileSystem(clientConnection, Config.ROOT_DIR_SERVER);

                var userController = new User(clientConnection);

                var clientHandler = new ClientHandler(clientConnection, fileSystemController, userController);

                threadPoolExecutor.execute(clientHandler);
            }

        } catch(IOException io)
        {
            System.out.println(Constants.SERVER + Constants.SERVER_DOWN);
        }
        finally
        {
            try
            {
                clientConnection.close();

                serverSocket.close(); // Close the server socket

                threadPoolExecutor.shutdown();

                System.out.println(Constants.SERVER+Constants.SERVER_STOP);

            }catch(NullPointerException npe)
            {
                System.out.println(Constants.SERVER + Constants.SERVER_START_ERROR);
            }
             catch(IOException e)
            {
                System.out.println(Constants.SERVER + Constants.SERVER_STOP_ERROR + e.getMessage());
            }

        }

    }
}
