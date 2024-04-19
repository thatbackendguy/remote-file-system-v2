package com.remotefsv2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

import static com.remotefsv2.utils.Config.*;
import static com.remotefsv2.utils.Constants.*;

import com.remotefsv2.server.controller.FileSystem;
import com.remotefsv2.server.controller.User;
import com.remotefsv2.server.handler.ClientConnection;
import com.remotefsv2.server.handler.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Server
{
    private Server() {}

    private static final int CORE_POOL_SIZE = 8; // Number of core threads

    private static final int MAXIMUM_POOL_SIZE = 12; // Maximum number of threads

    private static final long KEEP_ALIVE_TIME = 60; // Time (in seconds) for idle threads to wait before termination

    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private static final BlockingQueue<Runnable> WORKERS = new ArrayBlockingQueue<>(100); // Queue capacity

    public static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static final Marker fatal = MarkerFactory.getMarker("FATAL");

    public static void start() throws IOException, NullPointerException
    {

        ExecutorService threadPoolExecutor = null;

        ClientConnection clientConnection = null;

        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT))
        {

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
            if(clientConnection != null)
            {
                clientConnection.close();
            }

            if(threadPoolExecutor != null)
            {
                threadPoolExecutor.shutdown();
            }

            logger.error(fatal, SERVER + SERVER_STOP);
        }

    }


}
