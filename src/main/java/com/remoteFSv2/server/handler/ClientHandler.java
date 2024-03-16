package com.remoteFSv2.server.handler;

import com.remoteFSv2.server.controller.FileSystem;
import com.remoteFSv2.server.controller.User;
import com.remoteFSv2.utils.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class ClientHandler extends Thread
{

    private final ClientConnection clientConnection;

    private final FileSystem fileSystemController;

    private final User userController;

    public ClientHandler(ClientConnection clientConnection, FileSystem fileSystemController, User userController)
    {
        this.clientConnection = clientConnection;

        this.fileSystemController = fileSystemController;

        this.userController = userController;
    }

    @Override
    public void run()
    {
        try(this.clientConnection)
        {
            System.out.println(Constants.SERVER + Constants.CLIENT_CONNECTED + clientConnection.clientSocket);

            // Handle client requests
            var request = "";

            while((request = clientConnection.receive()) != null)
            {
                System.out.println("[Server] Received request from client: " + request);

                var requestJSON = new JSONObject(request);

                // Process request and send response back to client
                processRequest(requestJSON);
            }
        } catch(JSONException jsonException)
        {
            // send response that received json is improper format
            var res = new JSONObject();

            res.put("status", "1");

            res.put("message", Constants.IMPROPER_JSON);

            clientConnection.send(res.toString());

        } catch(IOException e)
        {
            System.out.println("[Server] Error handling client request: " + e.getMessage());

        } finally
        {
            try
            {
                clientConnection.close(); // Close client socket

                System.out.println("[Server] Client connection closed: " + clientConnection);

            } catch(IOException e)
            {
                System.out.println("[Server] Error while closing client connection: " + e.getMessage());
            }
        }
    }

    private void processRequest(JSONObject request)
    {
        var command = request.getString(Constants.COMMAND);

        switch(command)
        {
            case Constants.REGISTER:

                userController.registerUser(request.getString("username"), request.getString("password"));

                return;

            case Constants.LOGIN:

                userController.loginUser(request.getString("username"), request.getString("password"), request.getString(Constants.TOKEN));

                return;

            case Constants.LIST:

                fileSystemController.listFiles(request.getString(Constants.TOKEN), request.getString(Constants.CURRENT_DIR_PATH));

                return;

            case Constants.DOWNLOAD:

                fileSystemController.invokeSendFile(request.getString(Constants.TOKEN), request.getString(Constants.CURRENT_DIR_PATH), request.getString(Constants.FILE_NAME));

                return;

            case Constants.UPLOAD:

                fileSystemController.invokeReceiveFile(request.getString(Constants.TOKEN), request.getString(Constants.CURRENT_DIR_PATH), request.getString(Constants.FILE_NAME));

                return;

            case Constants.DELETE:

                fileSystemController.deleteFile(request.getString(Constants.TOKEN), request.getString(Constants.FILE_NAME), request.getString(Constants.CURRENT_DIR_PATH));

                return;

            case Constants.MKDIR:

                fileSystemController.makeDirectory(request.getString(Constants.TOKEN), request.getString(Constants.DIR_NAME), request.getString(Constants.CURRENT_DIR_PATH));

                return;

            case Constants.RMDIR:

                fileSystemController.removeDirectory(request.getString(Constants.TOKEN), request.getString(Constants.DIR_NAME), request.getString(Constants.CURRENT_DIR_PATH));

                return;

            case Constants.CD:

                fileSystemController.changeDirectory(request.getString(Constants.TOKEN), request.getString(Constants.DEST_PATH), request.getString(Constants.CURRENT_DIR_PATH));

                return;

            default:
                System.out.println(Constants.SERVER + Constants.INVALID_INPUT);
        }
    }
}
