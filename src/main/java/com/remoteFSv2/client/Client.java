package com.remoteFSv2.client;

import com.remoteFSv2.client.connection.ClientSocket;
import com.remoteFSv2.client.handler.FileSystem;
import com.remoteFSv2.client.handler.User;
import com.remoteFSv2.utils.Common;
import com.remoteFSv2.utils.Constants;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Table;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client
{
    public static String currPath = "/";

    public static void start()
    {
        Socket socket = null;

        User userHandler = null;

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
                        socket = clientSocket.connectServer();

                        if(socket != null)
                        {
                            userHandler = new User(socket);

                            System.out.println("--------------------");
                            System.out.println("\tLogin");
                            System.out.println("--------------------");

                            System.out.print("Enter username: ");
                            username = sc.next();

                            System.out.print("Enter password: ");
                            password = sc.next();

                            if(userHandler.authenticateUser(Constants.LOGIN, username, password))
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

                            username = Common.limitedLengthInputPrompt("username");

                            if(username.length() >= 6)
                            {
                                password = Common.limitedLengthInputPrompt("password");

                                if(password.length() >= 6)
                                {
                                    userHandler.authenticateUser(Constants.REGISTER, username, password);

                                    userHandler.close();
                                }
                                else
                                {
                                    System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                                }

                            }
                            else
                            {
                                System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
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

                        System.out.println(Constants.INVALID_INPUT + " Valid range = [0-2]");
                }

            } catch(JSONException jsonException)
            {
                System.out.println(Constants.CLIENT + Constants.IMPROPER_JSON);

            } catch(IOException e)
            {
                System.out.println(Constants.CLIENT + Constants.CONNECTION_ERROR);

                break;
            } catch(InputMismatchException e)
            {
                System.out.println(Constants.INVALID_INPUT + " Valid range = [0-2]");
            } catch(NullPointerException e)
            {
                System.out.println(Constants.CLIENT + Constants.SERVER_DOWN);
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

    public static void startFileSystem(String username)
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

            try
            {
                var choice = sc.nextLine();

                var parts = choice.split(" ", 2);

                var command = parts[0];

                command = command.toLowerCase();

                var argument = parts.length > 1 ? parts[1] : "";

                switch(command)
                {
                    // PRINTS LIST OF AVAILABLE COMMAND
                    case "help":

                        if(!argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                            break;
                        }

                        Client.printHelp();

                        break;

                    // PRINT CURRENT WORKING DIR
                    case "pwd":

                        if(!argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                            break;
                        }

                        System.out.println("Current working directory: " + currPath);

                        break;

                    // LIST FILES FROM CURRENT DIR
                    case Constants.LIST:

                        if(!argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                            break;
                        }


                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.listFiles();

                        fileSystem.close();

                        break;

                    // DOWNLOAD FILE FROM SERVER
                    case Constants.DOWNLOAD:

                        if(argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);

                            break;
                        }
                        else
                        {
                            socket = clientSocket.connectServer();

                            fileSystem = new FileSystem(User.userData.get(username), socket);

                            fileSystem.requestDownload(argument);

                        }

                        fileSystem.close();

                        break;

                    // UPLOAD FILE TO SERVER
                    case Constants.UPLOAD:

                        if(argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);

                            break;
                        }
                        else
                        {
                            socket = clientSocket.connectServer();

                            fileSystem = new FileSystem(User.userData.get(username), socket);

                            fileSystem.requestUpload(argument);

                        }

                        fileSystem.close();

                        break;

                    // DELETE FILE FROM SERVER
                    case Constants.REMOVE_FILE:

                        if(argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);

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

                    case Constants.MKDIR:

                        if(argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);

                            break;
                        }
                        else
                        {
                            socket = clientSocket.connectServer();

                            fileSystem = new FileSystem(User.userData.get(username), socket);

                            fileSystem.makeOrRemoveDir(Constants.MKDIR, argument, currPath);

                        }

                        fileSystem.close();

                        break;

                    case Constants.RMDIR:

                        if(argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);

                            break;
                        }
                        else
                        {
                            socket = clientSocket.connectServer();

                            fileSystem = new FileSystem(User.userData.get(username), socket);

                            fileSystem.makeOrRemoveDir(Constants.RMDIR, argument, currPath);

                        }

                        fileSystem.close();

                        break;

                    case Constants.CD:

                        if(argument.isEmpty()) // go to root directory
                        {
                            currPath = "/";
                        }
                        else if(argument.equals("..")) // go back to parent directory
                        {
                            if(currPath.equals("/"))
                            {
                                break;
                            }
                            currPath = String.valueOf(Path.of(currPath).getParent());
                        }
                        else if(argument.equals(".")) // current path
                        {
                            break;
                        }
                        else
                        {
                            socket = clientSocket.connectServer();

                            fileSystem = new FileSystem(User.userData.get(username), socket);

                            fileSystem.changeDirectory(argument, currPath);

                            fileSystem.close();
                        }

                        break;

                    // LOGOUT
                    case Constants.LOGOUT:

                        if(!argument.isEmpty())
                        {
                            System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                            break;
                        }

                        currPath = "/";

                        return;

                    default:

                        System.out.println(Constants.CLIENT + Constants.INVALID_INPUT);
                }

            } catch(NullPointerException npe)
            {
                System.out.println(Constants.CLIENT + Constants.SERVER_DOWN);

                break;

            } catch(IOException e)
            {
                System.out.println(Constants.CLIENT + Constants.CONNECTION_ERROR);

                break;

            } catch(JSONException jsonException)
            {
                System.out.println(Constants.CLIENT + Constants.IMPROPER_JSON);

                break;
            }
        }

    }
}
