package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.Server;
import com.remoteFSv2.server.handler.ClientConnection;
import static com.remoteFSv2.utils.Config.*;
import static com.remoteFSv2.utils.Constants.*;

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

    public static ConcurrentHashMap<String, Object> userEntity = new ConcurrentHashMap<>();

    public User(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }

    public void registerUser(String username, String password)
    {
        var response = new JSONObject();

        if(userCredentials.containsKey(username))
        {
            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, SERVER + REGISTRATION_ERROR);

            clientConnection.send(response.toString());

            Server.logger.error(REGISTRATION_ERROR);
        }
        else
        {
            userCredentials.put(username, password);

            userEntity.put(username,new Object());

            response.put(STATUS_CODE, SUCCESS);

            response.put(MESSAGE, SERVER + REGISTRATION_SUCCESS);

            var path = Path.of(ROOT_DIR_SERVER, username);

            try
            {
                Files.createDirectories(path);

            } catch(IOException e)
            {
                Server.logger.error(SERVER + MKDIR_FAIL);
            }

            Server.logger.info(REGISTRATION_SUCCESS);

            clientConnection.send(response.toString());
        }
    }


    public void loginUser(String username, String password)
    {
        var response = new JSONObject();

        if(userCredentials.isEmpty())
        {
            response.put(STATUS_CODE, 1);

            response.put(MESSAGE, SERVER + USER_NOT_FOUND);

            clientConnection.send(response.toString());

            Server.logger.error(USER_NOT_FOUND);
        }
        if(userCredentials.containsKey(username)) // user exists
        {
                if(password.equals(userCredentials.get(username))) // password match
                {
                    var token = JWTUtil.generateToken(username);

                    response.put(TOKEN, token);

                    response.put(STATUS_CODE, 0);

                    response.put(TOKEN, token);

                    response.put(MESSAGE, SERVER + LOGIN_SUCCESS);

                    clientConnection.send(response.toString());

                    Server.logger.info(username + " " + LOGIN_SUCCESS);
                }
                else
                {
                    response.put(STATUS_CODE, 1);

                    response.put(MESSAGE, SERVER + INVALID_CREDENTIALS);

                    clientConnection.send(response.toString());

                    Server.logger.error("Login not successful!");
                }


        }
        else // User doesn't exists
        {
            response.put(STATUS_CODE, 1);

            response.put(MESSAGE, SERVER + INVALID_CREDENTIALS);

            clientConnection.send(response.toString());

            Server.logger.error(INVALID_CREDENTIALS);
        }
    }
}
