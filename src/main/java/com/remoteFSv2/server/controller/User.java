package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Constants;

import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.util.HashMap;


public class User
{
    private JSONObject response = new JSONObject();

    private final ClientConnection clientConnection;

    static HashMap<String, String> userCredentials = new HashMap<>();

    static HashMap<String, String> usersMap = new HashMap<>();


    public User(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }


    public void registerUser(String username, String password)
    {
        response.clear();
        if(userCredentials.containsKey(username))
        {
            response.put("status", 1);
            response.put("message", Constants.SERVER + Constants.REGISTRATION_ERROR);

            clientConnection.send(response.toString());
        }
        else
        {
            userCredentials.put(username, password);


            var token = JWTUtil.generateToken(username);

            usersMap.put(username, token);

            response.put("status", 0);
            response.put("token", token);
            response.put("message", Constants.SERVER + Constants.REGISTRATION_SUCCESS);

            clientConnection.send(response.toString());
        }
    }


    public void loginUser(String username, String password, String token)
    {
        if(userCredentials.isEmpty())
        {
            response.put("status", 1);
            response.put("message", Constants.SERVER + Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
        if(userCredentials.containsKey(username)) // user exists
        {
            if(password.equals(userCredentials.get(username))) // password match
            {
                if(token.isEmpty()) // token doesn't exists
                {
                    response.put("status", 1);
                    response.put("message", Constants.SERVER + Constants.LOGIN_ERROR);

                    clientConnection.send(response.toString());
                }
                else if(username.equals(JWTUtil.verifyToken(token)))
                {
                    response.put("status", 0);
                    response.put("token", token);
                    response.put("message", Constants.SERVER + Constants.LOGIN_SUCCESS);

                    usersMap.put(username, token);

                    clientConnection.send(response.toString());
                }
                else
                {
                    response.put("status", 1);
                    response.put("message", Constants.SERVER + Constants.LOGIN_ERROR);

                    clientConnection.send(response.toString());
                }

            }
        }
        else
        {
            response.put("status", 1);
            response.put("message", Constants.SERVER + Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
    }
}
