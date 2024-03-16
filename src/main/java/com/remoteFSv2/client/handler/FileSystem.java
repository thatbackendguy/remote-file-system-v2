package com.remoteFSv2.client.handler;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.utils.Common;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Objects;

public class FileSystem implements Closeable
{

    private JSONObject request = new JSONObject();

    private final String token;

    private final Socket socket;

    public final BufferedReader reader;

    public final PrintWriter writer;

    public FileSystem(String token, Socket socket) throws IOException
    {
        this.token = token;

        this.socket = socket;

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean listFiles() throws IOException
    {

        request.clear();

        request.put(Constants.TOKEN, token);

        request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

        request.put(Constants.COMMAND, Constants.LIST);

        writer.println(request.toString());

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getInt(Constants.STATUS_CODE) == 0)
        {
            System.out.println(resJSON.get("files"));

            return true;
        }
        else
        {
            System.out.println(resJSON.getString(Constants.MESSAGE));

            return false;
        }
    }

    public void requestDownload(String fileName) throws IOException
    {
        request.clear();

        request.put(Constants.TOKEN, token);

        request.put(Constants.COMMAND, Constants.DOWNLOAD);

        request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

        request.put(Constants.FILE_NAME, fileName.trim());

        writer.println(request.toString());

        var dataInputStream = new DataInputStream(socket.getInputStream());

        var success = Common.receiveFile(dataInputStream, Config.ROOT_DIR_CLIENT + fileName);

        if(success)
        {
            System.out.println(Constants.CLIENT + Constants.FILE_DOWNLOAD_SUCCESS);
        }
        else
        {
            System.out.println(Constants.CLIENT + Constants.FILE_DOWNLOAD_ERROR);
        }

    }


    public void requestUpload(String localPath)
    {
        Path filePath = Paths.get(localPath);

        if(Common.validateFilePath(filePath))
        {
            try
            {
                var fileName = String.valueOf(filePath.getFileName());

                request.clear();

                request.put(Constants.TOKEN, token);

                request.put(Constants.COMMAND, Constants.UPLOAD);

                request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

                request.put(Constants.FILE_NAME, fileName);

                writer.println(request.toString());

                var file = new File(localPath);

                var dataOutputStream = new DataOutputStream(socket.getOutputStream());

                FileInputStream fileInputStream = new FileInputStream(file);

                var success = Common.sendFile(fileInputStream, dataOutputStream, file);

                var response = reader.readLine();

                var resJSON = new JSONObject(response);

                if(success && resJSON.getInt(Constants.STATUS_CODE) == 0)
                {
                    System.out.println(resJSON.getString(Constants.MESSAGE));
                }
                else
                {
                    System.out.println(resJSON.getString(Constants.MESSAGE));
                }

            } catch(FileNotFoundException e)
            {
                System.out.println(Constants.CLIENT + Constants.FILE_NOT_FOUND);

            } catch(IOException io)
            {
                System.out.println(Constants.CLIENT + Constants.IO_ERROR + "\nError: " + io.getMessage());
            }

        }
        else
        {
            System.out.println(Constants.CLIENT + Constants.INVALID_PATH);
        }

    }


    public void deleteFile(String fileName)
    {
        try
        {
            request.clear();

            request.put(Constants.TOKEN, token);

            request.put(Constants.COMMAND, Constants.DELETE);

            request.put(Constants.FILE_NAME, fileName);

            request.put(Constants.CURRENT_DIR_PATH, Client.currPath);


            writer.println(request.toString());

            var response = reader.readLine();

            var resJSON = new JSONObject(response);

            if(resJSON.getInt(Constants.STATUS_CODE) == 0) // success
            {
                System.out.println(resJSON.getString(Constants.MESSAGE));
            }
            else if(resJSON.getInt(Constants.STATUS_CODE) == 1) // failed
            {
                System.out.println(resJSON.getString(Constants.MESSAGE));
            }
            else
            {
                throw new IOException();
            }
        } catch(IOException | NullPointerException e)
        {
            System.out.println(Constants.CLIENT + Constants.CONNECTION_ERROR);
        }
    }


    public void makeOrRemoveDir(String command, String dirName, String currPath) throws IOException
    {
        request.clear();

        request.put(Constants.TOKEN, token);

        if(Objects.equals(command, Constants.MKDIR))
            request.put(Constants.COMMAND, Constants.MKDIR);
        else
            request.put(Constants.COMMAND, Constants.RMDIR);

        request.put(Constants.DIR_NAME, dirName);

        request.put(Constants.CURRENT_DIR_PATH, currPath);

        writer.println(request.toString());

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        System.out.println(resJSON.get(Constants.MESSAGE));

    }

    public void changeDirectory(String destPath, String currPath) throws IOException
    {
        request.clear();

        request.put(Constants.TOKEN, token);

        request.put(Constants.DEST_PATH, destPath);

        request.put(Constants.CURRENT_DIR_PATH, currPath);

        request.put(Constants.COMMAND, Constants.CD);

        writer.println(request.toString());

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getInt(Constants.STATUS_CODE) == 0)
        {
            Client.currPath = resJSON.getString(Constants.CURRENT_DIR_PATH);
        }
        else
        {
            System.out.println(resJSON.get(Constants.MESSAGE));
        }
    }


    public void close() throws IOException
    {
        reader.close(); // Close input stream

        writer.close(); // Close output stream

        socket.close(); // Close socket connection
    }

}
