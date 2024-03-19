package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.Server;
import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;

import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;


public class User
{
    private final ClientConnection clientConnection;

    public static ConcurrentHashMap<String, String> userCredentials = new ConcurrentHashMap<>();

    public User(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }

    public void registerUser(String username, String password)
    {
        var response = new JSONObject();

        if(userCredentials.containsKey(username))
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.REGISTRATION_ERROR);

            clientConnection.send(response.toString());
        }
        else
        {
            userCredentials.put(username, password);

            response.put(Constants.STATUS_CODE, 0);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.REGISTRATION_SUCCESS);

            var path = Path.of(Config.ROOT_DIR_SERVER, username);

            try
            {
                Files.createDirectories(path);

            } catch(IOException e)
            {
                Server.logger.error(Constants.SERVER + Constants.MKDIR_FAIL);
            }

            clientConnection.send(response.toString());
        }
    }


    public void loginUser(String username, String password)
    {
        var response = new JSONObject();

        if(userCredentials.isEmpty())
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.USER_NOT_FOUND);

            clientConnection.send(response.toString());
        }
        if(userCredentials.containsKey(username)) // user exists
        {
                if(password.equals(userCredentials.get(username))) // password match
                {
                    var token = JWTUtil.generateToken(username);

                    response.put(Constants.TOKEN, token);

                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.TOKEN, token);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.LOGIN_SUCCESS);

                    clientConnection.send(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_CREDENTIALS);

                    clientConnection.send(response.toString());
                }


        }
        else // User doesn't exists
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_CREDENTIALS);

            clientConnection.send(response.toString());
        }
    }
}
