package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Util;
import com.remoteFSv2.utils.Config;
import com.remoteFSv2.utils.Constants;
import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileSystem
{
    private JSONObject response = new JSONObject();

    private final String rootDirectory; // Root directory of the file system

    private final ClientConnection clientConnection;

    public FileSystem(ClientConnection clientConnection, String rootDirectory)
    {
        this.clientConnection = clientConnection;

        this.rootDirectory = rootDirectory;
    }

    public void listFiles(String token, String currPath)
    {
        response.clear();

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

                    response.put(Constants.STATUS_CODE, 0);
                }
                else
                {
                    response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.EMPTY_DIRECTORY);

                    response.put(Constants.STATUS_CODE, 1);
                }

                clientConnection.send(response.toString());

            } catch(IOException e)
            {
                System.out.println(Constants.SERVER + "Error listing files!" + e.getMessage());
            }
        }


    }

    public void invokeSendFile(String token, String currPath, String fileName)
    {
        response.clear();

        String username = JWTUtil.verifyToken(token);

        if(username != null) // if token present
        {
            var filePath = Paths.get(rootDirectory, username, currPath, fileName);

            if(Util.validateFilePath(filePath)) // if valid file path
            {
                try
                {
                    var file = new File(String.valueOf(filePath));

                    var fileInputStream = new FileInputStream(file);

                    var dataOutputStream = new DataOutputStream(clientConnection.clientSocket.getOutputStream());

                    var success = Util.sendFile(fileInputStream, dataOutputStream, file);

                    if(success)
                    {
                        System.out.println(Constants.SERVER + fileName + " " + Constants.FILE_SENT_SUCCESS);
                    }
                    else
                    {
                        System.out.println(Constants.SERVER + fileName + " " + Constants.FILE_SENT_ERROR);
                    }

                } catch(IOException io)
                {
                    System.out.println(Constants.SERVER + Constants.IO_ERROR + "\nError: " + io.getMessage());
                }
            }

        }

    }

    public void invokeReceiveFile(String token, String currPath, String fileName)
    {
        response.clear();

        String username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            try
            {
                var dataInputStream = new DataInputStream(clientConnection.clientSocket.getInputStream());

                var filePath = Config.ROOT_DIR_SERVER + username + currPath + "/" + fileName;

                var success = Util.receiveFile(dataInputStream, filePath);

                if(success)
                {
                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_UPLOAD_SUCCESS);
                }
                else
                {
                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_UPLOAD_ERROR);
                }

            } catch(IOException e)
            {
                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_UPLOAD_ERROR);
            }

            clientConnection.send(response.toString());
        }
    }


    public void deleteFile(String token, String fileName, String currPath)
    {
        response.clear();

        try
        {
            var username = JWTUtil.verifyToken(token);

            if(username != null)
            {
                var file = Paths.get(rootDirectory, username, currPath, fileName);

                if(Util.validateFilePath(file))
                {
                    Files.deleteIfExists(file);

                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_DELETE_SUCCESS);

                }
                else
                {
                    System.out.println(Constants.SERVER + fileName + " " + Constants.FILE_NOT_FOUND);

                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_NOT_FOUND);
                }
            }
            else
            {
                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);

            }

        } catch(IOException e)
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + fileName + " " + Constants.FILE_DELETE_ERROR);

            System.out.println(Constants.SERVER + fileName + " " + Constants.FILE_DELETE_ERROR + e.getMessage());
        }

        clientConnection.send(response.toString());
    }

    public void makeDirectory(String token, String dirName, String currPath)
    {
        response.clear();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

            try
            {
                Files.createDirectory(dirPath);

                response.put(Constants.STATUS_CODE, 0);

                response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.MKDIR_SUCCESS);

            } catch(FileAlreadyExistsException e)
            {
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_ALREADY_EXISTS);

                clientConnection.send(response.toString());

            } catch(IOException e)
            {
                // parent dir not exists
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.INVALID_PATH);
            }
        }
        else
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);

        }

        clientConnection.send(response.toString());
    }

    public void removeDirectory(String token, String dirName, String currPath)
    {
        response.clear();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

            if(Files.isDirectory(dirPath) && Files.exists(dirPath))
            {
                try
                {
                    Util.removeDirRecursively(String.valueOf(dirPath));

                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_DELETE_SUCCESS);

                } catch(IOException e)
                {
                    // delete failed
                    response.clear();

                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + dirName + " " + Constants.DIR_DELETE_ERROR);
                }
            }
            else
            {
                // delete failed
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + currPath + " " + Constants.INVALID_PATH);
            }


        }
        else
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);

        }

        clientConnection.send(response.toString());
    }

    public void changeDirectory(String token, String destPath, String currPath)
    {
        response.clear();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            var dirPath = Paths.get(rootDirectory, username, currPath, destPath);

            if(Files.isDirectory(dirPath) && Files.exists(dirPath))
            {
                var resDirPath = Paths.get(currPath, destPath);

                response.put(Constants.STATUS_CODE, 0);

                response.put(Constants.CURRENT_DIR_PATH, String.valueOf(resDirPath));
            }
            else
            {
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + currPath + "/" + destPath + " " + Constants.INVALID_PATH);
            }


        }
        else
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);

        }

        clientConnection.send(response.toString());
    }

}
