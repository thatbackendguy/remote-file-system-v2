package com.remoteFSv2.client;

import com.remoteFSv2.client.connection.ClientSocket;
import com.remoteFSv2.client.handler.FileSystem;
import com.remoteFSv2.client.handler.User;
import com.remoteFSv2.utils.Common;
import com.remoteFSv2.utils.Constants;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client
{
    public static void start()
    {
        Socket socket = null;

        User userHandler = null;

        ClientSocket clientSocket = ClientSocket.getInstance();

        System.out.println("Welcome to the Remote File System!");
        while(true)
        {
            Scanner sc = new Scanner(System.in);

            System.out.println("--------------------");
            System.out.println("\t\tMenu");
            System.out.println("--------------------");
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
                            System.out.println("\t\tLogin");
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

                            userHandler.close();
                        }
                        else
                        {
                            throw new IOException();
                        }
                        break;

                    // REGISTER
                    case 2:
                        socket = clientSocket.connectServer();
                        if(socket != null)
                        {
                            userHandler = new User(socket);

                            System.out.println("-----------------------");
                            System.out.println("\t\tRegister");
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
                            throw new IOException();
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

            } catch(InputMismatchException | NumberFormatException numberFormatException)
            {
                System.out.println(Constants.INVALID_INPUT + " Valid range = [0-2]");
            }
        }

    }

    public static void startFileSystem(String username)
    {
        Socket socket = null;

        FileSystem fileSystem = null;

        ClientSocket clientSocket = ClientSocket.getInstance();

        System.out.println("Welcome to the Remote File System!");

        while(true)
        {
            Scanner sc = new Scanner(System.in);

            System.out.println("--------------------");
            System.out.println("\t\tMenu");
            System.out.println("--------------------");
            System.out.println("ls  ==>  List files");
            System.out.println("get  ==>  Download file");
            System.out.println("put  ==>  Upload file");
            System.out.println("rm  ==>  Delete file");
            System.out.println("mkdir  ==>  Make Directory");
            System.out.println("rmdir  ==>  Remove Directory");
            System.out.println("cd  ==>  Change Directory");
            System.out.println("back  ==>  Go back one directory");
            System.out.println("logout  ==>  Logout");
            System.out.print("Enter your choice: ");

            try
            {
                var choice = sc.nextLine();

                choice = choice.toLowerCase();

                var filePath = "";

                var fileChoice = "";

                switch(choice)
                {
                    // LIST FILES OF SERVER
                    case Constants.LIST:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(User.userData.get(username), socket);

                        fileSystem.listFiles();

                        fileSystem.close();

                        break;

                    // DOWNLOAD FILE FROM SERVER
                    case Constants.DOWNLOAD:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);

                        fileSystem.listFiles();

                        System.out.print("Enter your choice (0) to exit: ");

                        fileChoice = sc.nextLine();

                        if(fileChoice.equals("0") || fileChoice.isEmpty())
                        {
                            break;
                        }
                        else
                        {
                            fileSystem.reqDownloadFile(fileChoice);
                        }
                        fileSystem.close();
                        break;

                    // UPLOAD FILE TO SERVER
                    case Constants.UPLOAD:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);
                        System.out.print("Enter your complete file path or (0) to exit: ");

                        filePath = sc.nextLine();

                        if(filePath.equals("0") || filePath.isEmpty())
                        {
                            break;
                        }
                        else
                        {
                            fileSystem.uploadFile(filePath);
                        }
                        fileSystem.close();

                        break;

                    // DELETE FILE FROM SERVER
                    case Constants.REMOVE_FILE:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);
                        fileSystem.listFiles();

                        System.out.print("Enter your choice (0) to exit: ");

                        fileChoice = sc.nextLine();

                        if(fileChoice.equals("0") || fileChoice.isEmpty())
                        {
                            break;
                        }
                        else
                        {
                            fileSystem.deleteFile(fileChoice);
                        }
                        fileSystem.close();
                        break;

                    case Constants.MKDIR:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);

                        fileSystem.listFiles();

                        // enter code here


                        fileSystem.close();
                        break;

                    case Constants.RMDIR:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);

                        fileSystem.listFiles();

                        // enter code here


                        fileSystem.close();
                        break;

                    case Constants.CD:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);

                        fileSystem.listFiles();

                        // enter code here


                        fileSystem.close();
                        break;

                    case Constants.BACK:
                        socket = clientSocket.connectServer();

                        fileSystem = new FileSystem(username, socket);

                        fileSystem.listFiles();

                        // enter code here


                        fileSystem.close();
                        break;

                    // LOGOUT
                    case Constants.LOGOUT:

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

            } catch(NumberFormatException nfe)
            {

                System.out.println(Constants.CLIENT + "Error: " + nfe.getMessage());

            } catch(JSONException jsonException)
            {
                System.out.println(Constants.CLIENT + Constants.IMPROPER_JSON);
            }
        }

    }
}
