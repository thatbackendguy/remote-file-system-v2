package com.remoteFSv2.client;

import com.remoteFSv2.client.ui.UserAuthentication;
import com.remoteFSv2.utils.Constants;

import java.io.IOException;

public class Client
{

    public static void main(String[] args)
    {

        var userAuthenticationUI = new UserAuthentication();

        try
        {
            userAuthenticationUI.start();

        } catch(IOException e)
        {
            System.out.println(Constants.CLIENT + Constants.SERVER_DOWN);
        }

    }
}
