package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Constants;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

public class User
{
    private JSONObject response = new JSONObject();
    private final ClientConnection clientConnection;
    static HashMap<String, String> userCredentials = new HashMap<>();

    public User(ClientConnection clientConnection) {this.clientConnection = clientConnection;}

    public void registerUser(String username, String password)
    {
        response.clear();
        if(userCredentials.containsKey(username))
        {
            response.put("status",1);
            response.put("message", Constants.REGISTRATION_ERROR);

            clientConnection.send(response.toString());
        }
        else
        {
            userCredentials.put(username, password);

            response.put("status",0);
            response.put("message", Constants.REGISTRATION_SUCCESS);

            clientConnection.send(response.toString());
        }
    }

    public void loginUser(String username, String password)
    {
        if(userCredentials.isEmpty())
        {
            response.put("status",1);
            response.put("message", Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
        if(userCredentials.containsKey(username))
        {
            if(password.equals(userCredentials.get(username)))
            {
                response.put("status",0);
                response.put("UUID",UUID.randomUUID());
                response.put("message", Constants.LOGIN_SUCCESS);

                clientConnection.send(response.toString());
            }
        }
        else
        {
            response.put("status",1);
            response.put("message", Constants.LOGIN_ERROR);

            clientConnection.send(response.toString());
        }
    }
}
