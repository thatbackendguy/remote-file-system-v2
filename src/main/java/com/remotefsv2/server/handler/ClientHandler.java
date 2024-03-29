package com.remotefsv2.server.handler;

import com.remotefsv2.server.Server;
import com.remotefsv2.server.controller.FileSystem;
import com.remotefsv2.server.controller.User;

import static com.remotefsv2.utils.Constants.*;

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
            Server.logger.trace(MESSAGE_FORMATTER, CLIENT_CONNECTED, clientConnection.clientSocket);

            // Handle client requests
            var request = "";

            while((request = clientConnection.receive()) != null)
            {
                Server.logger.trace(MESSAGE_FORMATTER, "Received request", request);

                var requestJSON = new JSONObject(request);

                // Process request and send response back to client
                processRequest(requestJSON);
            }
        } catch(JSONException jsonException)
        {
            // send response that received json is improper format
            var response = new JSONObject();

            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, IMPROPER_JSON);

            clientConnection.send(response.toString());

        } catch(IOException e)
        {
            Server.logger.error(MESSAGE_FORMATTER, "Error handling client request", e.getMessage());

        } finally
        {
            try
            {
                clientConnection.close(); // Close client socket

                Server.logger.trace(MESSAGE_FORMATTER, "Client connection closed", clientConnection);

            } catch(IOException e)
            {
                Server.logger.error(Server.fatal, MESSAGE_FORMATTER, "[Server] Error while closing client connection", e.getMessage());
            }
        }
    }

    private void processRequest(JSONObject request)
    {
        var command = request.getString(COMMAND);

        switch(command)
        {
            case REGISTER:

                userController.registerUser(request.getString("username"), request.getString("password"));

                return;

            case LOGIN:

                userController.loginUser(request.getString("username"), request.getString("password"));

                return;

            case LIST:

                fileSystemController.listFiles(request.getString(TOKEN), request.getString(CURRENT_DIR_PATH));

                return;

            case DOWNLOAD:

                fileSystemController.sendFile(request.getString(TOKEN), request.getString(CURRENT_DIR_PATH), request.getString(FILE_NAME));

                return;

            case UPLOAD:

                fileSystemController.receiveFile(request);

                return;

            case DELETE:

                fileSystemController.deleteFile(request.getString(TOKEN), request.getString(FILE_NAME), request.getString(CURRENT_DIR_PATH));

                return;

            case MKDIR:

                fileSystemController.makeDirectory(request.getString(TOKEN), request.getString(DIR_NAME), request.getString(CURRENT_DIR_PATH));

                return;

            case RMDIR:

                fileSystemController.removeDirectory(request.getString(TOKEN), request.getString(DIR_NAME), request.getString(CURRENT_DIR_PATH));

                return;

            case CD:

                fileSystemController.changeDirectory(request.getString(TOKEN), request.getString(DEST_PATH), request.getString(CURRENT_DIR_PATH));

                return;

            default:
                Server.logger.error(INVALID_INPUT);
        }
    }
}
