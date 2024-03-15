package com.remoteFSv2;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.server.Server;

import java.util.Objects;

public class BootStrap
{

    public static void main(String[] args)
    {
        if(Objects.equals(args[0], "server"))
        {
            Server.start();
        }
        else
        {
            Client.start();
        }

    }

}
