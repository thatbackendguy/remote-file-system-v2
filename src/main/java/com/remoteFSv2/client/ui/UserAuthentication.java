package com.remoteFSv2.client.ui;

import com.remoteFSv2.client.connection.ClientSocket;
import com.remoteFSv2.client.handler.FileSystem;

import com.remoteFSv2.client.handler.User;
import com.remoteFSv2.utils.Constants;
import org.json.JSONException;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class UserAuthentication
{
    private Scanner sc = new Scanner(System.in);

    private Socket socket = null;

    private User userHandler = null;

    public void start() throws IOException
    {
        ClientSocket clientSocket = ClientSocket.getInstance();

        System.out.println("Welcome to the Remote File System!");
        while(true)
        {
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

                        userHandler = new User(socket);

                        System.out.println("--------------------");
                        System.out.println("\t\tLogin");
                        System.out.println("--------------------");

                        System.out.print("Enter username: ");
                        username = userHandler.reader.readLine();

                        System.out.print("Enter password: ");
                        password = userHandler.reader.readLine();

                        if(userHandler.authenticateUser(Constants.LOGIN, username, password))
                        {

                            var fileSystemClient = new FileSystem(userHandler);

                            var fileManagerUI = new FileManager(fileSystemClient, socket);

                            fileManagerUI.start();

                        }

                        userHandler.close();
                        break;

                    // REGISTER
                    case 2:
                        socket = clientSocket.connectServer();

                        userHandler = new User(socket);

                        System.out.println("--------------------");
                        System.out.println("\t\tRegister");
                        System.out.println("--------------------");

                        username = userInput("username");

                        if(username.length() >= 6)
                        {
                            password = userInput("password");

                            if(password.length() >= 6)
                            {
                                if(userHandler.authenticateUser(Constants.LOGIN, username, password))
                                {
                                    System.out.println(Constants.CLIENT + Constants.REGISTRATION_SUCCESS);
                                }
                                else
                                {
                                    System.out.println(Constants.CLIENT + Constants.REGISTRATION_ERROR);
                                }

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
                System.out.println(Constants.CLIENT + "Response is in improper format!");

            } catch(IOException e)
            {
                System.out.println("IOException: " + e.getMessage());

                System.out.println("Server disconnected. Exiting client...");

                break;

            } catch(NumberFormatException numberFormatException)
            {
                System.out.println(Constants.INVALID_INPUT + " Valid range = [0-2]");
            } finally
            {
                userHandler.close();
            }
        }

    }

    public String userInput(String identity)
    {
        var input = "";
        for(int chance = 0; chance < 3; chance++)
        {
            System.out.print("Enter " + identity + ": ");

            input = sc.next();

            if(input.length() >= 6)
            {
                return input;
            }
            else
            {
                System.out.println(identity + " length should be >= 6!");
            }
        }
        return "";
    }
}
