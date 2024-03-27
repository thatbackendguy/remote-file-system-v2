package com.remoteFSv2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadPoolExecutor;

import static com.remoteFSv2.utils.Config.*;
import static com.remoteFSv2.utils.Constants.*;

import com.remoteFSv2.server.controller.FileSystem;
import com.remoteFSv2.server.controller.User;
import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.server.handler.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Server
{
    public static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static final Marker fatal = MarkerFactory.getMarker("FATAL");

    public static void start() throws IOException,NullPointerException
    {
        ServerSocket serverSocket = null;

        ThreadPoolExecutor threadPoolExecutor = null;

        ClientConnection clientConnection = null;

        try
        {

            serverSocket = new ServerSocket(SERVER_PORT);

            logger.info(SERVER + SERVER_START_SUCCESS + SERVER_PORT);

            threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, WORKERS);

            while(true)
            {
                var clientSocket = serverSocket.accept();

                clientConnection = new ClientConnection(clientSocket);

                var fileSystemController = new FileSystem(clientConnection, ROOT_DIR_SERVER);

                var userController = new User(clientConnection);

                var clientHandler = new ClientHandler(clientConnection, fileSystemController, userController);

                threadPoolExecutor.execute(clientHandler);
            }

        } catch(IOException io)
        {
            logger.error(fatal, SERVER + SERVER_START_ERROR);

        } catch(Exception e)
        {
            logger.error(fatal, SERVER + SERVER_DOWN);

        } finally
        {
                clientConnection.close();

                serverSocket.close(); // Close the server socket

                threadPoolExecutor.shutdown();

                logger.error(fatal, SERVER + SERVER_STOP);
        }

    }


}
