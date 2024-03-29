package com.remotefsv2.server.controller;

import com.remotefsv2.server.handler.ClientConnection;

import static com.remotefsv2.utils.Config.*;
import static com.remotefsv2.utils.Constants.*;
import static com.remotefsv2.server.Server.*;

import com.remotefsv2.utils.JWTUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class User
{
    private final ClientConnection clientConnection;

    public static ConcurrentMap<String, String> userCredentials = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, Object> userEntity = new ConcurrentHashMap<>();

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

            logger.info(REGISTRATION_ERROR);
        }
        else
        {
            userCredentials.put(username, password);

            userEntity.put(username, new Object());

            response.put(STATUS_CODE, SUCCESS);

            response.put(MESSAGE, SERVER + REGISTRATION_SUCCESS);

            var path = Path.of(ROOT_DIR_SERVER, username);

            try
            {
                Files.createDirectories(path);

            } catch(IOException e)
            {
                logger.error(SERVER + MKDIR_FAIL);
            }

            logger.info(REGISTRATION_SUCCESS);

            clientConnection.send(response.toString());
        }
    }


    public void loginUser(String username, String password)
    {
        var response = new JSONObject();

        if(userCredentials.isEmpty()) // if map is empty -> no user available
        {
            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, SERVER + USER_NOT_FOUND);

            clientConnection.send(response.toString());

            logger.info(USER_NOT_FOUND);
        }
        else // map is not empty -> users are available
        {
            if(userCredentials.containsKey(username)) // user exists
            {
                if(password.equals(userCredentials.get(username))) // password match
                {
                    var token = JWTUtil.generateToken(username);

                    response.put(TOKEN, token);

                    response.put(STATUS_CODE, SUCCESS);

                    response.put(TOKEN, token);

                    response.put(MESSAGE, SERVER + LOGIN_SUCCESS);

                    clientConnection.send(response.toString());

                    logger.info(MESSAGE_FORMATTER, username, LOGIN_SUCCESS);
                }
                else
                {
                    response.put(STATUS_CODE, FAILED);

                    response.put(MESSAGE, SERVER + INVALID_CREDENTIALS);

                    clientConnection.send(response.toString());

                    logger.info("{}: {}", username, INVALID_CREDENTIALS);
                }
            }
        }
    }
}
