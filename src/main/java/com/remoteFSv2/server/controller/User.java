package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Constants;

import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class User
{
    private JSONObject response = new JSONObject();

    private final ClientConnection clientConnection;

    public static ConcurrentHashMap<String, String> userCredentials = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, String> usersMap = new ConcurrentHashMap<>();


    public User(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }


    public void registerUser(String username, String password)
    {
        response.clear();
        if(userCredentials.containsKey(username))
        {
            response.put(Constants.STATUS_CODE, 1);
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.REGISTRATION_ERROR);

            clientConnection.send(response.toString());
        }
        else
        {
            userCredentials.put(username, password);


            var token = JWTUtil.generateToken(username);

            usersMap.put(username, token);

            response.put(Constants.STATUS_CODE, 0);
            response.put(Constants.TOKEN, token);
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.REGISTRATION_SUCCESS);

            clientConnection.send(response.toString());
        }
    }


    public void loginUser(String username, String password, String token)
    {
        if(userCredentials.isEmpty())
        {
            response.put(Constants.STATUS_CODE, 1);
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
        if(userCredentials.containsKey(username)) // user exists
        {
            if(username.equals(JWTUtil.verifyToken(token))) // token match
            {
                if(password.equals(userCredentials.get(username))) // password match
                {
                    response.put(Constants.STATUS_CODE, 0);
                    response.put(Constants.TOKEN, token);
                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.LOGIN_SUCCESS);

                    usersMap.put(username, token);

                    clientConnection.send(response.toString());
                }
                else
                {
                    response.put(Constants.STATUS_CODE, 1);
                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_CREDENTIALS);

                    clientConnection.send(response.toString());
                }

            }
            else if(token.isEmpty()) // token doesn't exists
            {
                response.put(Constants.STATUS_CODE, 1);
                response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_EMPTY);

                clientConnection.send(response.toString());
            }
            else
            {
                response.put(Constants.STATUS_CODE, 1);
                response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

                clientConnection.send(response.toString());
            }
        }
        else
        {
            response.put(Constants.STATUS_CODE, 1);
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
    }
}
