package com.remoteFSv2.server.controller;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.server.Server;
import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Util;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;
import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileSystem
{
    private final String rootDirectory; // Root directory of the file system

    private final ClientConnection clientConnection;

    public FileSystem(ClientConnection clientConnection, String rootDirectory)
    {
        this.clientConnection = clientConnection;

        this.rootDirectory = rootDirectory;
    }

    public void listFiles(String token, String currPath)
    {
        var response = new JSONObject();

        String username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            try
            {
                var directory = Paths.get(rootDirectory, username, currPath);

                var files = new ArrayList<String>();

                Files.list(directory).forEach(path -> {
                    if(Files.isDirectory(path))
                    {
                        files.add(path.getFileName().toString() + "/");
                    }
                    else
                    {
                        files.add(path.getFileName().toString());
                    }
                });

                if(!files.isEmpty())
                {
                    response.put("files", files);

                    response.put(Constants.STATUS_CODE, Constants.SUCCESS);

                    Server.logger.info("{}: Files listed successfully!",username);
                }
                else
                {
                    response.put(Constants.STATUS_CODE, Constants.FAILED);

                    response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.EMPTY_DIRECTORY);

                    Server.logger.error("{}: {} {}",username,currPath,Constants.EMPTY_DIRECTORY);
                }
            } catch(IOException e)
            {
                Server.logger.error("{}: Error listing files!",username);
            }
        }
        else
        {
            response.put(Constants.STATUS_CODE, Constants.FAILED);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);
        }

        clientConnection.send(response.toString());

    }

    public void deleteFile(String token, String fileName, String currPath)
    {
        var response = new JSONObject();

        try
        {
            var username = JWTUtil.verifyToken(token);

            if(username != null)
            {
                var file = Paths.get(rootDirectory, username, currPath, fileName);

                if(Util.validateFilePath(file))
                {
                    Files.deleteIfExists(file);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_DELETE_SUCCESS);

                    Server.logger.info("{}: {}",username,Constants.FILE_DELETE_SUCCESS);

                }
                else
                {
                    Server.logger.error("{}: {}",username,Constants.FILE_NOT_FOUND);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_NOT_FOUND);
                }
            }
            else
            {
                response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

                Server.logger.error(Constants.JWT_INVALID);
            }
        } catch(IOException e)
        {
            response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_DELETE_ERROR);

            Server.logger.error(fileName + " " + Constants.FILE_DELETE_ERROR + e.getMessage());
        }

        clientConnection.send(response.toString());
    }

    public void makeDirectory(String token, String dirName, String currPath)
    {
        var response = new JSONObject();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

            try
            {
                Files.createDirectory(dirPath);

                response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.MKDIR_SUCCESS);

                Server.logger.info("{}: {}",username,Constants.MKDIR_SUCCESS);

            } catch(FileAlreadyExistsException e)
            {
                response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_ALREADY_EXISTS);

                Server.logger.error("{}: {}",username,Constants.DIR_ALREADY_EXISTS);

            } catch(IOException e)
            {
                // parent dir not exists
                response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.INVALID_PATH);

                Server.logger.error("{}: {}",username,Constants.INVALID_PATH);
            }
        }
        else
        {
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);
        }

        clientConnection.send(response.toString());
    }

    public void removeDirectory(String token, String dirName, String currPath)
    {
        var response = new JSONObject();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

            if(Files.isDirectory(dirPath) && Files.exists(dirPath))
            {
                try
                {
                    Util.removeDirRecursively(String.valueOf(dirPath));

                    response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_DELETE_SUCCESS);

                    Server.logger.info("{}: {}",username,Constants.DIR_DELETE_SUCCESS);

                } catch(IOException e)
                {
                    // delete failed
                    response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_DELETE_ERROR);

                    Server.logger.error("{}: {}",username,Constants.DIR_DELETE_ERROR);
                }
            }
            else
            {
                // delete failed
                response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.INVALID_PATH);

                Server.logger.error("{}: {}",username,Constants.INVALID_PATH);
            }
        }
        else
        {
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);
        }

        clientConnection.send(response.toString());
    }

    public void changeDirectory(String token, String destPath, String currPath)
    {
        var response = new JSONObject();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, destPath);

            if(Files.isDirectory(dirPath) && Files.exists(dirPath))
            {
                var resDirPath = Paths.get(currPath, destPath);

                response.put(Constants.STATUS_CODE, Constants.SUCCESS);

                response.put(Constants.CURRENT_DIR_PATH, String.valueOf(resDirPath));
            }
            else
            {
                response.put(Constants.STATUS_CODE, Constants.FAILED);

                response.put(Constants.MESSAGE, Constants.SERVER + currPath + "/" + destPath + " " + Constants.INVALID_PATH);

                Server.logger.error("{}: {}",username,Constants.INVALID_PATH);
            }
        }
        else
        {
            response.put(Constants.STATUS_CODE, Constants.FAILED);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);

        }

        clientConnection.send(response.toString());
    }

    public void receiveFile(JSONObject request)
    {
        var response = new JSONObject();

        var currPath = request.getString(Constants.CURRENT_DIR_PATH);

        var fileName = request.getString(Constants.FILE_NAME);

        var size = request.getLong("fileSize");

        String username = JWTUtil.verifyToken(request.getString(Constants.TOKEN));

        if(username != null)
        {
            var filePath = Config.ROOT_DIR_SERVER + username + currPath + "/" + fileName;

            try(FileOutputStream fos = new FileOutputStream(filePath))
            {
                var buffer = new byte[Config.CHUNK_SIZE]; // 8KB

                int bytes = 0;

                while(request.getString(Constants.STATUS_CODE).equals(Constants.PENDING) && size > 0)
                {
                    // Decode the Base64 encoded string to a byte array
                    buffer = Base64.getDecoder().decode(request.getString("payload"));

                    bytes = request.getInt("offset");

                    // Here we write the file using write method
                    fos.write(buffer, 0, bytes);

                    size -= bytes; // read upto file size
                    String req = clientConnection.receive();

                    request = new JSONObject(req);
                }

            } catch(IOException e)
            {
                response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_UPLOAD_ERROR);

                Server.logger.error("{}: {}",fileName,Constants.FILE_UPLOAD_ERROR);
            }
        }
        else
        {
            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);
        }

        response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_UPLOAD_SUCCESS);

        Server.logger.info("{}: {}",fileName,Constants.FILE_UPLOAD_SUCCESS);

        clientConnection.send(response.toString());
    }

    public void sendFile(String token, String currPath, String fileName)
    {
        var response = new JSONObject();

        String username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var filePath = Paths.get(rootDirectory, username, currPath, fileName);

            if(Util.validateFilePath(filePath))
            {
                try(FileInputStream fileInputStream = new FileInputStream(filePath.toFile()))
                {

                    var fileSize = filePath.toFile().length();

                    var bytes = 0;

                    var buffer = new byte[Config.CHUNK_SIZE];

                    while((bytes = fileInputStream.read(buffer)) != -1)
                    {
                        // Send the file to Client Socket
                        // Convert the byte array to a Base64 encoded string
                        String payload = Base64.getEncoder().encodeToString(buffer);

                        response.put(Constants.STATUS_CODE, Constants.PENDING);

                        response.put(Constants.FILE_NAME, fileName);

                        response.put("fileSize", fileSize);

                        response.put("payload", payload);

                        response.put("offset", bytes);

                        // send packet to server
                        clientConnection.send(response.toString());
                    }

                    Server.logger.info("{}: {}",fileName,Constants.FILE_SENT_SUCCESS);

                    response.put(Constants.STATUS_CODE, Constants.SUCCESS);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_DOWNLOAD_SUCCESS);

                } catch(IOException e) // exception caught for FileInputStream
                {
                    Server.logger.error("Error reading file!");

                    response.put(Constants.MESSAGE, Constants.SERVER + "Error reading file!");
                }
            }
            else
            {
                response.put(Constants.STATUS_CODE, Constants.FAILED);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.FILE_NOT_FOUND);

                Server.logger.error(Constants.FILE_NOT_FOUND);
            }

        }
        else
        {
            response.put(Constants.STATUS_CODE, Constants.FAILED);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.JWT_INVALID);

            Server.logger.error(Constants.JWT_INVALID);

        }

        clientConnection.send(response.toString());
    }

}
