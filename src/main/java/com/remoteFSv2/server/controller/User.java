package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;

import java.util.HashMap;

public class User
{
    static HashMap<String, String> userCredentials = new HashMap<>();

    public boolean registerUser(String username, String password)
    {
        if(userCredentials.containsKey(username))
        {
            return false;
        }
        else
        {
            userCredentials.put(username, password);

            return true;
        }
    }

    public boolean loginUser(String username, String password)
    {
        if(userCredentials.isEmpty())
        {
            return false;
        }
        if(userCredentials.containsKey(username))
        {
            return password.equals(userCredentials.get(username));
        }
        else
        {
            return false;
        }
    }
}
