package com.remoteFSv2;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.server.Server;
import com.remoteFSv2.utils.Util;
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
                try
                {
                    Server.start();

                } catch(NullPointerException | IOException e)
                {
                    System.out.println(Constants.SERVER + Constants.SERVER_STOP_ERROR);
                }

            }
            else if(Objects.equals(args[0], "client"))
            {
                try
                {
                    Client.start();

                } catch(NullPointerException | IOException e)
                {
                    System.out.println(Constants.CLIENT + Constants.CONNECTION_ERROR);
                }
            }
            else if(Objects.equals(args[0],"clean"))
            {
                try
                {
                    Util.removeDirRecursively(Config.ROOT_DIR_SERVER);
                    Util.removeDirRecursively(Config.ROOT_DIR_CLIENT);

                    Files.createDirectories(Path.of(Config.ROOT_DIR_SERVER));
                    Files.createDirectories(Path.of(Config.ROOT_DIR_CLIENT));

                    System.out.println("Cleanup Successful!");
                }
                catch(IOException e)
                {
                    System.out.println(Constants.SERVER + "Error in cleaning!");
                }

            }
            else
            {
                System.out.println("Please start server/client using following commands:");

                System.out.println("java -jar <name of executable> server");

                System.out.println("java -jar <name of executable> client");

                System.out.println("java -jar <name of executable> clean");
            }
        }
        else
        {
            System.out.println("Please start server/client using following commands:");

            System.out.println("java -jar <name of executable> server");

            System.out.println("java -jar <name of executable> client");

            System.out.println("java -jar <name of executable> clean");
        }
    }
}
