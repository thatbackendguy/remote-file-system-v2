package com.remoteFSv2.ui;

import com.remoteFSv2.client.connection.ClientSocket;
import com.remoteFSv2.client.handler.FileSystem;
import com.remoteFSv2.client.handler.User;
import com.remoteFSv2.utils.Constants;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class FileManager
{
    private FileSystem fileSystem;

    private Socket socket;

    private final String username;

    public FileManager(String username)
    {
        this.username = username;
    }

    public void start() throws IOException
    {
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

            } catch(IOException e)
            {

                System.out.println(Constants.CLIENT + Constants.IO_ERROR);

                break;

            } catch(NumberFormatException nfe)
            {

                System.out.println(Constants.CLIENT+"Error: " + nfe.getMessage());

            } catch(JSONException jsonException)
            {
                System.out.println(Constants.CLIENT + Constants.IMPROPER_JSON);
            }
        }

    }
}
