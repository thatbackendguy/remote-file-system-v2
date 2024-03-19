package com.remoteFSv2.client.handler;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.utils.Util;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Base64;
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

        request.put(Constants.TOKEN, token);

        request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

        request.put(Constants.COMMAND, Constants.LIST);

        writer.println(request.toString());

        Client.logger.info("ls request sent to server!");

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getString(Constants.STATUS_CODE).equals(Constants.SUCCESS))
        {
            System.out.println(resJSON.get("files"));

            Client.logger.info("Files listed successfully!");

            return true;
        }
        else
        {
            System.out.println(resJSON.getString(Constants.MESSAGE));

            Client.logger.error(Constants.SERVER + resJSON.get(Constants.MESSAGE));

            return false;
        }
    }

    public void deleteFile(String fileName)
    {
        try
        {
            request.put(Constants.TOKEN, token);

            request.put(Constants.COMMAND, Constants.DELETE);

            request.put(Constants.FILE_NAME, fileName);

            request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

            writer.println(request.toString());

            Client.logger.info("rm request sent to server!");

            var response = reader.readLine();

            var resJSON = new JSONObject(response);

            System.out.println(resJSON.getString(Constants.MESSAGE));

            Client.logger.info(Constants.SERVER + resJSON.get(Constants.MESSAGE));

        } catch(IOException | NullPointerException e)
        {
            System.out.println(Constants.CLIENT + Constants.CONNECTION_ERROR);

            Client.logger.error(Constants.CONNECTION_ERROR);
        }
    }

    public void makeOrRemoveDir(String command, String dirName, String currPath) throws IOException
    {
        request.put(Constants.TOKEN, token);

        if(Objects.equals(command, Constants.MKDIR))
        {
            request.put(Constants.COMMAND, Constants.MKDIR);
        }
        else
        {
            request.put(Constants.COMMAND, Constants.RMDIR);
        }

        request.put(Constants.DIR_NAME, dirName);

        request.put(Constants.CURRENT_DIR_PATH, currPath);

        writer.println(request.toString());

        Client.logger.info("mkdir/rmdir request sent to server!");

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        System.out.println(resJSON.get(Constants.MESSAGE));

        Client.logger.info(Constants.SERVER + resJSON.get(Constants.MESSAGE));

    }

    public void changeDirectory(String destPath, String currPath) throws IOException
    {
        request.put(Constants.TOKEN, token);

        request.put(Constants.DEST_PATH, destPath);

        request.put(Constants.CURRENT_DIR_PATH, currPath);

        request.put(Constants.COMMAND, Constants.CD);

        writer.println(request.toString());

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getString(Constants.STATUS_CODE).equals(Constants.SUCCESS))
        {
            Client.logger.info("Directory changed to {}!", resJSON.getString(Constants.CURRENT_DIR_PATH));

            Client.currPath = resJSON.getString(Constants.CURRENT_DIR_PATH);
        }
        else
        {
            System.out.println(resJSON.get(Constants.MESSAGE));

            Client.logger.error(Constants.SERVER + resJSON.get(Constants.MESSAGE));
        }
    }

    public void sendFile(String localPath) throws IOException
    {
        Path filePath = Paths.get(localPath);

        if(Util.validateFilePath(filePath))
        {
            try(FileInputStream fileInputStream = new FileInputStream(filePath.toFile()))
            {
                var fileName = String.valueOf(filePath.getFileName());

                var fileSize = filePath.toFile().length();

                var bytes = 0;

                var buffer = new byte[Config.CHUNK_SIZE];

                Client.logger.info("{} upload started!", fileName);

                while((bytes = fileInputStream.read(buffer)) != -1)
                {
                    // Send the file to Client Socket
                    // Convert the byte array to a Base64 encoded string
                    String payload = Base64.getEncoder().encodeToString(buffer);

                    request.put(Constants.COMMAND, Constants.UPLOAD);

                    request.put(Constants.TOKEN, token);

                    request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

                    request.put(Constants.STATUS_CODE, Constants.PENDING);

                    request.put(Constants.FILE_NAME, fileName);

                    request.put("fileSize", fileSize);

                    request.put("payload", payload);

                    request.put("offset", bytes);

                    // send packet to server
                    writer.println(request.toString());
                }

                Client.logger.info("{} upload finished!", fileName);

                request.put(Constants.STATUS_CODE, Constants.SUCCESS);

                writer.println(request.toString());

                var res = reader.readLine();

                JSONObject resJSON = new JSONObject(res);

                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.info(Constants.SERVER + resJSON.getString(Constants.MESSAGE));

            } catch(FileNotFoundException e)
            {
                System.out.println(Constants.CLIENT + Constants.FILE_NOT_FOUND);

                Client.logger.error(Constants.FILE_NOT_FOUND);
            }
        }
        else
        {
            System.out.println(Constants.CLIENT + Constants.INVALID_PATH);

            Client.logger.error(Constants.INVALID_PATH);
        }
    }

    public void receiveFile(String fileName)
    {
        var filePath = Config.ROOT_DIR_CLIENT + "/" + fileName;

        request.put(Constants.TOKEN, token);

        request.put(Constants.COMMAND, Constants.DOWNLOAD);

        request.put(Constants.CURRENT_DIR_PATH, Client.currPath);

        request.put(Constants.FILE_NAME, fileName.trim());

        writer.println(request.toString());

        try(FileOutputStream fos = new FileOutputStream(filePath))
        {
            var response = reader.readLine();

            var resJSON = new JSONObject(response);

            if(resJSON != null) // server is down
            {
                if(!resJSON.getString(Constants.STATUS_CODE).equals(Constants.FAILED))
                {
                    var buffer = new byte[Config.CHUNK_SIZE]; // 8KB

                    var size = resJSON.getInt("fileSize");

                    int bytes = 0;

                    while(resJSON.getString(Constants.STATUS_CODE).equals(Constants.PENDING) && size > 0)
                    {
                        Client.logger.info("{} download started!", fileName);

                        // Decode the Base64 encoded string to a byte array
                        buffer = Base64.getDecoder().decode(resJSON.getString("payload"));

                        bytes = resJSON.getInt("offset");

                        // Here we write the file using write method
                        fos.write(buffer, 0, bytes);

                        size -= bytes; // read upto file size

                        response = reader.readLine();

                        resJSON = new JSONObject(response);

                        if(resJSON == null) // if server is down while transfer is ON
                        {
                            throw new IOException();
                        }
                    }
                }

                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.info("{} download completed!", fileName);
            }
            else
            {
                System.out.println(Constants.CLIENT + Constants.SERVER_DOWN);

                Client.logger.error(Constants.SERVER_DOWN);
            }

        } catch(IOException e)
        {
            System.out.println(Constants.CLIENT + Constants.FILE_DOWNLOAD_ERROR);

            Client.logger.error(Constants.FILE_DOWNLOAD_ERROR);
        }
    }

    public void close() throws IOException
    {
        reader.close(); // Close input stream

        writer.close(); // Close output stream

        socket.close(); // Close socket connection
    }

}
