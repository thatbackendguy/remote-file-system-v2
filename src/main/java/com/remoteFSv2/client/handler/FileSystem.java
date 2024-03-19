package com.remoteFSv2.client.handler;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.utils.Util;
import static com.remoteFSv2.utils.Config.*;
import static com.remoteFSv2.utils.Constants.*;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Base64;
import java.util.Objects;

public class FileSystem implements Closeable
{
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
        var request = new JSONObject();

        request.put(TOKEN, token);

        request.put(CURRENT_DIR_PATH, Client.currPath);

        request.put(COMMAND, LIST);

        writer.println(request.toString());

        Client.logger.info("ls request sent to server!");

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getString(STATUS_CODE).equals(SUCCESS))
        {
            System.out.println(resJSON.get("files"));

            Client.logger.info("Files listed successfully!");

            return true;
        }
        else
        {
            System.out.println(resJSON.getString(MESSAGE));

            Client.logger.error(resJSON.getString(MESSAGE));

            return false;
        }
    }

    public void deleteFile(String fileName)
    {
        var request = new JSONObject();
        try
        {
            request.put(TOKEN, token);

            request.put(COMMAND, DELETE);

            request.put(FILE_NAME, fileName);

            request.put(CURRENT_DIR_PATH, Client.currPath);

            writer.println(request.toString());

            Client.logger.info("rm request sent to server!");

            var response = reader.readLine();

            var resJSON = new JSONObject(response);

            System.out.println(resJSON.getString(MESSAGE));

            Client.logger.info(resJSON.getString(MESSAGE));

        } catch(IOException | NullPointerException e)
        {
            System.out.println(CLIENT + CONNECTION_ERROR);

            Client.logger.error(CONNECTION_ERROR);
        }
    }

    public void makeOrRemoveDir(String command, String dirName, String currPath) throws IOException
    {
        var request = new JSONObject();

        request.put(TOKEN, token);

        if(Objects.equals(command, MKDIR))
        {
            request.put(COMMAND, MKDIR);
        }
        else
        {
            request.put(COMMAND, RMDIR);
        }

        request.put(DIR_NAME, dirName);

        request.put(CURRENT_DIR_PATH, currPath);

        writer.println(request.toString());

        Client.logger.info("mkdir/rmdir request sent to server!");

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        System.out.println(resJSON.getString(MESSAGE));

        Client.logger.info(resJSON.getString(MESSAGE));

    }

    public void changeDirectory(String destPath, String currPath) throws IOException
    {
        var request = new JSONObject();

        request.put(TOKEN, token);

        request.put(DEST_PATH, destPath);

        request.put(CURRENT_DIR_PATH, currPath);

        request.put(COMMAND, CD);

        writer.println(request.toString());

        var response = reader.readLine();

        var resJSON = new JSONObject(response);

        if(resJSON.getString(STATUS_CODE).equals(SUCCESS))
        {
            Client.logger.info("Directory changed to {}!", resJSON.getString(CURRENT_DIR_PATH));

            Client.currPath = resJSON.getString(CURRENT_DIR_PATH);
        }
        else
        {
            System.out.println(resJSON.get(MESSAGE));

            Client.logger.info(resJSON.getString(MESSAGE));
        }
    }

    public void sendFile(String localPath) throws IOException
    {
        var request = new JSONObject();

        Path filePath = Paths.get(localPath);

        if(Util.validateFilePath(filePath))
        {
            try(FileInputStream fileInputStream = new FileInputStream(filePath.toFile()))
            {
                var fileName = String.valueOf(filePath.getFileName());

                var fileSize = filePath.toFile().length();

                var bytes = 0;

                var buffer = new byte[CHUNK_SIZE];

                while((bytes = fileInputStream.read(buffer)) != -1)
                {
                    // Send the file to Client Socket
                    // Convert the byte array to a Base64 encoded string
                    String payload = Base64.getEncoder().encodeToString(buffer);

                    request.put(COMMAND, UPLOAD);

                    request.put(TOKEN, token);

                    request.put(CURRENT_DIR_PATH, Client.currPath);

                    request.put(STATUS_CODE, PENDING);

                    request.put(FILE_NAME, fileName);

                    request.put("fileSize", fileSize);

                    request.put("payload", payload);

                    request.put("offset", bytes);

                    // send packet to server
                    writer.println(request.toString());
                }

                Client.logger.info("{}: {}", fileName,FILE_UPLOAD_SUCCESS);

                request.put(STATUS_CODE, SUCCESS);

                writer.println(request.toString());

                var res = reader.readLine();

                JSONObject resJSON = new JSONObject(res);

                System.out.println(resJSON.getString(MESSAGE));

                Client.logger.info(resJSON.getString(MESSAGE));

            } catch(FileNotFoundException e)
            {
                System.out.println(CLIENT + FILE_NOT_FOUND);

                Client.logger.error(FILE_NOT_FOUND);
            }
        }
        else
        {
            System.out.println(CLIENT + INVALID_PATH);

            Client.logger.error(INVALID_PATH);
        }
    }

    public void receiveFile(String fileName)
    {
        var request = new JSONObject();

        var filePath = ROOT_DIR_CLIENT + "/" + fileName;

        request.put(TOKEN, token);

        request.put(COMMAND, DOWNLOAD);

        request.put(CURRENT_DIR_PATH, Client.currPath);

        request.put(FILE_NAME, fileName.trim());

        writer.println(request.toString());

        try(FileOutputStream fos = new FileOutputStream(filePath))
        {
            var response = reader.readLine();

            var resJSON = new JSONObject(response);

            if(resJSON != null) // server is down
            {
                if(!resJSON.getString(STATUS_CODE).equals(FAILED))
                {
                    var buffer = new byte[CHUNK_SIZE]; // 8KB

                    var size = resJSON.getInt(FILE_SIZE);

                    int bytes = 0;

                    while(resJSON.getString(STATUS_CODE).equals(PENDING) && size > 0)
                    {
                        // Decode the Base64 encoded string to a byte array
                        buffer = Base64.getDecoder().decode(resJSON.getString(PAYLOAD));

                        bytes = resJSON.getInt(OFFSET);

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

                System.out.println(resJSON.getString(MESSAGE));

                Client.logger.info("{}: {}", fileName,FILE_DOWNLOAD_SUCCESS);
            }
            else
            {
                System.out.println(CLIENT + SERVER_DOWN);

                Client.logger.error(SERVER_DOWN);
            }

        } catch(IOException e)
        {
            System.out.println(CLIENT + FILE_DOWNLOAD_ERROR);

            Client.logger.error(FILE_DOWNLOAD_ERROR);
        }
    }

    public void close() throws IOException
    {
        reader.close(); // Close input stream

        writer.close(); // Close output stream

        socket.close(); // Close socket connection
    }

}
