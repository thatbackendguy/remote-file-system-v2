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

            res.put("status","1");

            res.put("message","Request improper format!");

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
        try
        {
            var command = request.getString("command");

            var argument = request.getString("argument");

            switch(command)
            {
                case Constants.REGISTER:

                    userController.registerUser(argument.split(",", 2)[0], argument.split(",", 2)[1]);


                case Constants.LOGIN:

                    userController.loginUser(argument.split(",", 2)[0], argument.split(",", 2)[1]);


                case Constants.LIST:
                    fileSystemController.listFiles();


                case Constants.DOWNLOAD:
                    // Example: DOWNLOAD indexOfFile

                    fileSystemController.getFileName(Integer.parseInt(argument));


                case Constants.START_SENDING:
                    // for starting the sending of file when server receives confirmation from "DOWNLOAD"

                    fileSystemController.sendFile(argument);


                case Constants.UPLOAD:
                    // Example: UPLOAD fileName

                    fileSystemController.receiveFile(argument);


                case Constants.DELETE:
                    // Example: DELETE indexOfFile

                    fileSystemController.deleteFile(Integer.parseInt(argument));


                default:
                    System.out.println(Constants.SERVER + Constants.INVALID_INPUT);
            }
        } catch(NullPointerException e)
        {
            System.out.println("[Server] Error: Invalid request!");
        }
    }
}
