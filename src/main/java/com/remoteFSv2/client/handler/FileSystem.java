package com.remoteFSv2.client.handler;

import com.remoteFSv2.client.Config;

import java.io.*;
import java.nio.file.*;

public class FileSystem
{


    private final User userHandler;

    public FileSystem(User userHandler)
    {
        this.userHandler = userHandler;
    }

    public void listFiles()
    {
        try
        {
            var response = userHandler.sendRequest("LIST");
            if(response.equals("null"))
            {
                throw new IOException();
            }
            else if(response.equals("{}"))
            {
                System.out.println("[Client] Remote server directory is empty! Please upload files...");
            }
            else
            {
                System.out.println(response);
            }

        } catch(NullPointerException | IOException e)
        {
            System.out.println("[Client] Server is down!");
        }
    }

    public void reqDownloadFile(String fileChoice)
    {
        try
        {
            var response = userHandler.sendRequest("DOWNLOAD " + fileChoice);
            if(response.equals("null"))
            {
                throw new IOException();
            }
            var command = response.split(" ", 2)[0]; // "START_RECEIVING" command

            if(command.equals("START_RECEIVING"))
            {
                var argument = response.split(" ", 2)[1]; // FILE-NAME

                if(receiveFileFromServer(argument))
                {
                    System.out.println("[Client] File downloaded successfully!");

                    userHandler.reader.readLine();

                }
                else
                {
                    System.out.println("[Client] Error! File not received properly!");
                }
            }
            else
            {
                System.out.println("[Client] File not found on server!");
            }

        } catch(NullPointerException npe)
        {
            System.out.println("[Client] Server is down!");
        } catch(IOException e)
        {
            System.out.println("[Client] Error downloading files from server!");
        }
    }

    public boolean receiveFileFromServer(String fileName)
    {
        userHandler.writer.println("START_SENDING " + fileName.trim());

        try
        {
            var bytes = 0;

            var dataInputStream = new DataInputStream(userHandler.socket.getInputStream());

            var fileOutputStream = new FileOutputStream(Config.ROOT_DIR_CLIENT + fileName);

            // read file size
            var size = dataInputStream.readLong();

            var buffer = new byte[8192]; // 8KB

            while(size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1)
            {
                // Here we write the file using write method
                fileOutputStream.write(buffer, 0, bytes);

                size -= bytes; // read upto file size
            }

            fileOutputStream.close();

            return true;

        } catch(NullPointerException npe)
        {
            System.out.println("[Client] Server is down!");

            return false;

        } catch(IOException e)
        {
            System.out.println("[Client] Error in receiving file from server...\nError: " + e.getMessage());

            return false;
        }
    }


    public boolean uploadFile(String localPath)
    {
        var fileDirectories = localPath.trim().split("/");

        var fileName = fileDirectories[fileDirectories.length - 1];

        if(Files.exists(Paths.get(localPath)) && fileName.contains("."))
        {
            try
            {
                userHandler.writer.println("UPLOAD " + fileName);

                var file = new File(localPath);

                var dataOutputStream = new DataOutputStream(userHandler.socket.getOutputStream());

                FileInputStream fileInputStream = new FileInputStream(file);

                // Here we send the File to Server
                dataOutputStream.writeLong(file.length());

                var bytes = 0;

                // Here we break file into 8KB chunks
                var buffer = new byte[8192];

                while((bytes = fileInputStream.read(buffer)) != -1)
                {
                    // Send the file to Server Socket
                    dataOutputStream.write(buffer, 0, bytes);

                    dataOutputStream.flush();
                }

                // close the file here
                fileInputStream.close();

                userHandler.reader.readLine();

                return true;

            } catch(NullPointerException npe)
            {
                System.out.println("[Client] Server is down!");

                return false;

            } catch(FileNotFoundException e)
            {
                System.out.println("[Client] File not found!");

                return false;

            } catch(IOException io)
            {
                System.out.println("[Client] Data input/output stream error...\nError: " + io.getMessage());

                return false;
            }

        }
        else
        {
            System.out.println("[Client] Incorrect file path!");

            return false;
        }

    }


    public void deleteFile(String fileChoice)
    {
        try
        {
            var response = userHandler.sendRequest("DELETE " + fileChoice);

            if(response.equals("null"))
            {
                throw new IOException();
            }

            System.out.println(response);

        } catch(IOException | NullPointerException e)
        {
            System.out.println("[Client] Server is down!");
        }
    }
}
