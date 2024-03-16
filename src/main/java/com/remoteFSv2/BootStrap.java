package com.remoteFSv2;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.server.Server;
import com.remoteFSv2.utils.Common;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class BootStrap
{

    public static void main(String[] args)
    {
        if(args.length > 0)
        {
            if(Objects.equals(args[0], "server"))
            {
                Server.start();
            }
            else if(Objects.equals(args[0], "fresh-server"))
            {
                try
                {
                    Common.removeDirRecursively(Config.ROOT_DIR_SERVER);

                    System.out.println(Constants.SERVER + "Cleanup Successful!");

                    Files.createDirectories(Path.of(Config.ROOT_DIR_SERVER));

                    Server.start();

                } catch(IOException e)
                {
                    System.out.println(Constants.SERVER + Constants.SERVER_START_ERROR);
                }
            }
            else if(Objects.equals(args[0], "client"))
            {
                Client.start();
            }
            else if(Objects.equals(args[0], "fresh-client"))
            {
                try
                {
                    Common.removeDirRecursively(Config.ROOT_DIR_CLIENT);

                    System.out.println(Constants.CLIENT + "Cleanup Successful!");

                    Files.createDirectories(Path.of(Config.ROOT_DIR_CLIENT));

                    Client.start();

                } catch(IOException e)
                {
                    System.out.println(Constants.CLIENT + Constants.CLIENT_START_ERROR);
                }
            }
            else
            {
                System.out.println("Please start server/client using following commands:");

                System.out.println("java -jar <name of executable> server");

                System.out.println("java -jar <name of executable> fresh-server");

                System.out.println("java -jar <name of executable> client");

                System.out.println("java -jar <name of executable> fresh-client");
            }
        }
        else
        {
            System.out.println("Please start server/client using following commands:");

            System.out.println("java -jar <name of executable> server");

            System.out.println("java -jar <name of executable> fresh-server");

            System.out.println("java -jar <name of executable> client");

            System.out.println("java -jar <name of executable> fresh-client");
        }
    }
}
