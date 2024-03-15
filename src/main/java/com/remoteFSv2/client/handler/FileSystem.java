package com.remoteFSv2.client.handler;

import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

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

    public void reqDownloadFile(String fileChoice) throws IOException
    {
        request.clear();

        request.put(Constants.TOKEN, token);
        request.put(Constants.COMMAND, Constants.DOWNLOAD);

        writer.println(request.toString());
        var response = reader.readLine();

        var resJSON = new JSONObject(response);

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

                reader.readLine();

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


    }

    public boolean receiveFileFromServer(String fileName) throws IOException
    {
        request.clear();

        request.put(Constants.TOKEN, token);
        request.put(Constants.COMMAND, Constants.START_SENDING);
        request.put(Constants.FILE_NAME, fileName.trim());

        writer.println(request.toString());


        var bytes = 0;

        var dataInputStream = new DataInputStream(socket.getInputStream());

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

    }


    public boolean uploadFile(String localPath)
    {
        var fileDirectories = localPath.trim().split("/");

        var fileName = fileDirectories[fileDirectories.length - 1];

        if(Files.exists(Paths.get(localPath)) && fileName.contains("."))
        {
            try
            {
                writer.println("UPLOAD " + fileName);

                var file = new File(localPath);

                var dataOutputStream = new DataOutputStream(socket.getOutputStream());

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

                reader.readLine();

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


    public void deleteFile(String fileName)
    {
        try
        {
            request.clear();

            request.put(Constants.TOKEN, token);
            request.put(Constants.COMMAND, Constants.DELETE);
            request.put(Constants.FILE_NAME, fileName);

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

        if(resJSON.getInt(Constants.STATUS_CODE) == 0)
        {
            System.out.println(resJSON.get(Constants.MESSAGE));
        }
        else
        {
            System.out.println(resJSON.getString(Constants.MESSAGE));
        }
    }


    public void close() throws IOException
    {
        reader.close(); // Close input stream

        writer.close(); // Close output stream

        socket.close(); // Close socket connection
    }

}
