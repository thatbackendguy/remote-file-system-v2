package com.remotefsv2.client;

import com.remotefsv2.client.connection.ClientSocket;
import com.remotefsv2.client.handler.FileSystem;
import com.remotefsv2.client.handler.User;

import com.remotefsv2.utils.Util;

import static com.remotefsv2.utils.Constants.*;

import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client
{

    private Client() {}

    public static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static final Marker FATAL = MarkerFactory.getMarker("FATAL");

    public static String CURRENT_PATH = "/";

    public static void start() throws IOException
    {
        Socket socket = null;

        User userHandler = null;

        // get instance of singleton class
        ClientSocket clientSocket = ClientSocket.getInstance();

        System.out.println("Welcome to the Remote File System!");

        while(true)
        {
            Scanner sc = new Scanner(System.in);

            System.out.println("--------------------------------");
            System.out.println("\tREMOTE FILE SYSTEM");
            System.out.println("--------------------------------");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");

            try
            {
                var choice = sc.nextInt();

                var username = "";

                var password = "";

                switch(choice)
                {
                    // LOGIN
                    case 1:
                        System.out.println("--------------------");
                        System.out.println("\tLogin");
                        System.out.println("--------------------");

                        System.out.print("Enter username: ");
                        username = sc.next();

                        System.out.print("Enter password: ");
                        password = sc.next();

                        socket = clientSocket.connectServer();

                        if(socket != null)
                        {
                            userHandler = new User(socket);

                            if(userHandler.authenticateUser(LOGIN, username, password))
                            {
                                userHandler.close();

                                startFileSystem(username);
                            }
                            else
                            {
                                userHandler.close();
                            }
                        }
                        else
                        {
                            return;
                        }

                        break;

                    // REGISTER
                    case 2:
                        socket = clientSocket.connectServer();

                        if(socket != null)
                        {
                            userHandler = new User(socket);

                            System.out.println("-----------------------");
                            System.out.println("\tRegister");
                            System.out.println("-----------------------");

                            username = Util.limitedLengthInputPrompt("username");

                            if(username.length() >= 6)
                            {
                                password = Util.limitedLengthInputPrompt("password");

                                if(password.length() >= 6)
                                {
                                    userHandler.authenticateUser(REGISTER, username, password);

                                    userHandler.close();
                                }
                                else
                                {
                                    System.out.println(CLIENT + INVALID_INPUT);
                                }

                            }
                            else
                            {
                                System.out.println(CLIENT + INVALID_INPUT);
                            }
                        }
                        else
                        {
                            return;
                        }

                        break;


                    // EXIT
                    case 0:

                        System.out.println("Exiting client...");

                        return;

                    default:
                        LOGGER.info(INVALID_INPUT);

                        System.out.println(INVALID_INPUT + " Valid range = [0-2]");
                }

            } catch(JSONException jsonException) // improper response
            {
                System.out.println(CLIENT + IMPROPER_JSON);

                LOGGER.error(IMPROPER_JSON);

            } catch(IOException e) // error while connecting to socket
            {
                System.out.println(CLIENT + CONNECTION_ERROR);

                LOGGER.error(FATAL, CONNECTION_ERROR);

                break;
            } catch(InputMismatchException e) // input scanner
            {
                System.out.println(INVALID_INPUT + " Valid range = [0-2]");

                LOGGER.error(INVALID_INPUT);

            } catch(NullPointerException e) // when server is down and client is on IO
            {
                System.out.println(CLIENT + SERVER_DOWN);

                LOGGER.error(FATAL, SERVER_DOWN);
            } finally
            {
                if(socket != null)
                {
                    socket.close();
                }

                if(userHandler != null)
                {
                    userHandler.close();
                }
            }
        }

    }

    private static void printHelp()
    {
        var commands = new String[]{"ls", "pwd", "cd", "rm", "get", "put", "mkdir", "rmdir", "logout"};

        var descriptions = new String[]{"List files", "Get current working directory", "Change Directory", "Delete file", "Download file", "Upload file", "Make Directory", "Remove Directory", "Logout"};

        var examples = new String[]{"ls", "pwd", "cd <valid dir name>/..", "rm <file name>", "get <file name>", "put <local file path>", "mkdir <dir name>", "rmdir <file name>", "logout"};

        ColumnFormatter<String> commandFormatter = ColumnFormatter.text(Alignment.LEFT, 10);

        ColumnFormatter<String> descFormatter = ColumnFormatter.text(Alignment.LEFT, 30);

        ColumnFormatter<String> exFormatter = ColumnFormatter.text(Alignment.LEFT, 25);

        Table.Builder builder = new Table.Builder("Command", commands, commandFormatter);

        builder.addColumn("Description", descriptions, descFormatter);

        builder.addColumn("Usage", examples, exFormatter);

        Table table = builder.build();

        System.out.println(table);
    }

    public static void startFileSystem(String username) throws NullPointerException, IOException, JSONException
    {
        Socket socket = null;

        FileSystem fileSystem = null;

        ClientSocket clientSocket = ClientSocket.getInstance();

        System.out.println("Welcome to the Remote File System!");

        System.out.println("Note: Enter 'help' for more info...");

        while(true)
        {
            Scanner sc = new Scanner(System.in);

            System.out.print("Enter command >> ");

            var choice = sc.nextLine();

            var parts = choice.split(" ", 2);

            var command = parts[0];

            command = command.toLowerCase();

            var argument = parts.length > 1 ? parts[1] : "";

            switch(command)
            {
                // PRINTS LIST OF AVAILABLE COMMAND
                case HELP:

                    if(!argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at HELP command");

                        break;
                    }

                    Client.printHelp();

                    break;

                // PRINT CURRENT WORKING DIR
                case PWD:

                    if(!argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at PWD command");

                        break;
                    }

                    System.out.println("Current working directory: " + CURRENT_PATH);

                    break;

                // LIST FILES FROM CURRENT DIR
                case LIST:

                    if(!argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at LIST command");

                        break;
                    }


                    socket = clientSocket.connectServer();

                    fileSystem = new FileSystem(User.userData.get(username), socket);

                    fileSystem.listFiles();

                    fileSystem.close();

                    break;

                // DOWNLOAD FILE FROM SERVER
                case DOWNLOAD:

                    if(argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at DOWNL commandOAD");

                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.receiveFile(argument);

                    }

                    fileSystem.close();

                    break;

                // UPLOAD FILE TO SERVER
                case UPLOAD:

                    if(argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at UPLOA commandD");

                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.sendFile(argument);

                    }

                    fileSystem.close();

                    break;

                // DELETE FILE FROM SERVER
                case REMOVE_FILE:

                    if(argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at REMOV commandE FILE");

                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.deleteFile(argument);

                    }

                    fileSystem.close();

                    break;

                // MAKE DIR ON SERVER
                case MKDIR:

                    if(argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at MKDIR command");

                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.makeOrRemoveDir(MKDIR, argument, CURRENT_PATH);

                    }

                    fileSystem.close();

                    break;

                // REMOVE DIR ON SERVER
                case RMDIR:

                    if(argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at RMDIR command");

                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.makeOrRemoveDir(RMDIR, argument, CURRENT_PATH);

                    }

                    fileSystem.close();

                    break;

                // CHANGE DIR
                case CD:

                    if(argument.isEmpty()) // go to root directory
                    {
                        CURRENT_PATH = "/";

                        Client.LOGGER.info("Directory changed to root folder!");
                    }
                    else if(argument.equals("..")) // go back to parent directory
                    {
                        if(CURRENT_PATH.equals("/"))
                        {
                            break;
                        }
                        CURRENT_PATH = String.valueOf(Path.of(CURRENT_PATH).getParent());

                        Client.LOGGER.info("Directory changed to {}!", CURRENT_PATH);
                    }
                    else if(argument.equals(".")) // current path
                    {
                        break;
                    }
                    else
                    {
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        CURRENT_PATH = fileSystem.changeDirectory(argument, CURRENT_PATH);

                        fileSystem.close();
                    }

                    break;

                // LOGOUT
                case LOGOUT:

                    if(!argument.isEmpty())
                    {
                        System.out.println(CLIENT + INVALID_INPUT);

                        LOGGER.error(INVALID_INPUT + "at LOGOU commandT");

                        break;
                    }

                    CURRENT_PATH = "/";

                    return;

                default:

                    System.out.println(CLIENT + INVALID_INPUT);

                    LOGGER.error(INVALID_INPUT);
            }
        }
    }
}
