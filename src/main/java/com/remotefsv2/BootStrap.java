package com.remotefsv2;

import com.remotefsv2.client.Client;
import com.remotefsv2.server.Server;
import com.remotefsv2.utils.Util;
import static com.remotefsv2.utils.Config.*;
import com.remotefsv2.utils.Constants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class BootStrap
{
    public static final Logger LOGGER = LoggerFactory.getLogger(BootStrap.class);

    public static final Marker FATAL = MarkerFactory.getMarker("FATAL");

    public static void main(String[] args)
    {
        var config = BootStrap.loadConfig();

        HOST = config.getString("HOST");

        CLIENT_PORT = config.getInt("PORT");


            if(args.length > 0 && Objects.equals(args[0], "SERVER"))
            {
                try
                {
                    Server.start();

                } catch(NullPointerException | IOException e)
                {
                    LOGGER.error(FATAL,Constants.SERVER + Constants.SERVER_STOP_ERROR);
                }

            }
            else if(args.length > 0 && Objects.equals(args[0], "CLIENT"))
            {
                try
                {
                    Client.start();

                } catch(NullPointerException | IOException e)
                {
                    LOGGER.error(FATAL,Constants.CLIENT + Constants.CONNECTION_ERROR);
                }
            }
            else if(args.length > 0 && Objects.equals(args[0],"CLEAN"))
            {
                try
                {
                    Util.removeDirRecursively(ROOT_DIR_SERVER);
                    Util.removeDirRecursively(ROOT_DIR_CLIENT);

                    Files.createDirectories(Path.of(ROOT_DIR_SERVER));
                    Files.createDirectories(Path.of(ROOT_DIR_CLIENT));

                    LOGGER.info("Cleanup Successful!");
                }
                catch(IOException e)
                {
                    LOGGER.error(FATAL,"Error in cleaning!");
                }

            }
            else
            {
                System.out.println("Please start server/client using following commands:");

                System.out.println("java Bootstrap server");

                System.out.println("java Bootstrap client");
            }


    }

    private static JSONObject loadConfig()
    {
        try(var inputStream = new FileInputStream("config.json"))
        {
            var buffer = inputStream.readAllBytes();

            var jsonText = new String(buffer);

            Client.LOGGER.info("Reading config file: " + jsonText);

            return new JSONObject(jsonText);

        }
        catch(IOException e)
        {
            System.out.println("Error reading configuration file: ");

            Client.LOGGER.error("Error reading configuration file: " + e.getMessage());

            return new JSONObject();
        }
    }
}
