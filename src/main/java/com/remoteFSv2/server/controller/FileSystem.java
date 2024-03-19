package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.Server;
import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Util;
import static com.remoteFSv2.utils.Config.*;
import static com.remoteFSv2.utils.Constants.*;
import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static com.remoteFSv2.server.controller.User.*;

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
            synchronized(userEntity.get(username))
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

                        response.put(STATUS_CODE, SUCCESS);

                        Server.logger.info("{}: Files listed successfully!", username);
                    }
                    else
                    {
                        response.put(STATUS_CODE, FAILED);

                        response.put(MESSAGE, SERVER + currPath + " " + EMPTY_DIRECTORY);

                        Server.logger.error("{}: {} {}", username, currPath, EMPTY_DIRECTORY);
                    }
                } catch(IOException e)
                {
                    Server.logger.error("{}: Error listing files!", username);
                }
            }
        }
        else
        {
            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);
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
                synchronized(userEntity.get(username))
                {
                    var file = Paths.get(rootDirectory, username, currPath, fileName);

                    if(Util.validateFilePath(file))
                    {
                        synchronized(userEntity.get(username))
                        {
                            Files.deleteIfExists(file);

                            response.put(MESSAGE, SERVER + fileName + " " + FILE_DELETE_SUCCESS);

                            Server.logger.info("{}: {}", username, FILE_DELETE_SUCCESS);
                        }

                    }
                    else
                    {
                        Server.logger.error("{}: {}", username, FILE_NOT_FOUND);

                        response.put(MESSAGE, SERVER + fileName + " " + FILE_NOT_FOUND);
                    }
                }
            }
            else
            {
                response.put(MESSAGE, SERVER + JWT_INVALID);

                Server.logger.error(JWT_INVALID);
            }
        } catch(IOException e)
        {
            response.put(MESSAGE, SERVER + fileName + " " + FILE_DELETE_ERROR);

            Server.logger.error(fileName + " " + FILE_DELETE_ERROR + e.getMessage());
        }

        clientConnection.send(response.toString());
    }

    public void makeDirectory(String token, String dirName, String currPath)
    {
        var response = new JSONObject();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            synchronized(userEntity.get(username))
            {
                var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

                try
                {
                    Files.createDirectory(dirPath);

                    response.put(MESSAGE, SERVER + dirName + " " + MKDIR_SUCCESS);

                    Server.logger.info("{}: {}", username, MKDIR_SUCCESS);

                } catch(FileAlreadyExistsException e)
                {
                    response.put(MESSAGE, SERVER + dirName + " " + DIR_ALREADY_EXISTS);

                    Server.logger.error("{}: {}", username, DIR_ALREADY_EXISTS);

                } catch(IOException e)
                {
                    // parent dir not exists
                    response.put(MESSAGE, SERVER + currPath + " " + INVALID_PATH);

                    Server.logger.error("{}: {}", username, INVALID_PATH);
                }
            }
        }
        else
        {
            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);
        }

        clientConnection.send(response.toString());
    }

    public void removeDirectory(String token, String dirName, String currPath)
    {
        var response = new JSONObject();

        var username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            synchronized(userEntity.get(username))
            {
                var dirPath = Paths.get(rootDirectory, username, currPath, dirName);

                if(Files.isDirectory(dirPath) && Files.exists(dirPath))
                {
                    try
                    {

                        Util.removeDirRecursively(String.valueOf(dirPath));

                        response.put(MESSAGE, SERVER + dirName + " " + DIR_DELETE_SUCCESS);

                        Server.logger.info("{}: {}", username, DIR_DELETE_SUCCESS);


                    } catch(IOException e)
                    {
                        // delete failed
                        response.put(MESSAGE, SERVER + dirName + " " + DIR_DELETE_ERROR);

                        Server.logger.error("{}: {}", username, DIR_DELETE_ERROR);
                    }
                }
                else
                {
                    // delete failed
                    response.put(MESSAGE, SERVER + currPath + " " + INVALID_PATH);

                    Server.logger.error("{}: {}", username, INVALID_PATH);
                }
            }
        }
        else
        {
            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);
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

            synchronized(userEntity.get(username))
            {
                if(Files.isDirectory(dirPath) && Files.exists(dirPath))
                {
                    var resDirPath = Paths.get(currPath, destPath);

                    response.put(STATUS_CODE, SUCCESS);

                    response.put(CURRENT_DIR_PATH, String.valueOf(resDirPath));
                }
                else
                {
                    response.put(STATUS_CODE, FAILED);

                    response.put(MESSAGE, SERVER + currPath + "/" + destPath + " " + INVALID_PATH);

                    Server.logger.error("{}: {}", username, INVALID_PATH);
                }
            }
        }
        else
        {
            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);

        }

        clientConnection.send(response.toString());
    }

    public void receiveFile(JSONObject request)
    {
        var response = new JSONObject();

        var currPath = request.getString(CURRENT_DIR_PATH);

        var fileName = request.getString(FILE_NAME);

        var size = request.getLong(FILE_SIZE);

        String username = JWTUtil.verifyToken(request.getString(TOKEN));

        if(username != null)
        {
            synchronized(userEntity.get(username))
            {
                var filePath = ROOT_DIR_SERVER + username + currPath + "/" + fileName;

                try(FileOutputStream fos = new FileOutputStream(filePath))
                {
                    var buffer = new byte[CHUNK_SIZE]; // 8KB

                    int bytes = 0;

                    while(request.getString(STATUS_CODE).equals(PENDING) && size > 0)
                    {
                        // Decode the Base64 encoded string to a byte array
                        buffer = Base64.getDecoder().decode(request.getString(PAYLOAD));

                        bytes = request.getInt(OFFSET);

                        // Here we write the file using write method
                        fos.write(buffer, 0, bytes);

                        size -= bytes; // read upto file size
                        String req = clientConnection.receive();

                        request = new JSONObject(req);
                    }

                } catch(IOException e)
                {
                    response.put(MESSAGE, SERVER + fileName + " " + FILE_UPLOAD_ERROR);

                    Server.logger.error("{}: {}", fileName, FILE_UPLOAD_ERROR);
                }
            }
        }
        else
        {
            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);
        }

        response.put(MESSAGE, SERVER + fileName + " " + FILE_UPLOAD_SUCCESS);

        Server.logger.info("{}: {}", fileName, FILE_UPLOAD_SUCCESS);

        clientConnection.send(response.toString());
    }

    public void sendFile(String token, String currPath, String fileName)
    {
        var response = new JSONObject();

        String username = JWTUtil.verifyToken(token);

        if(username != null)
        {
            synchronized(userEntity.get(username))
            {
                var filePath = Paths.get(rootDirectory, username, currPath, fileName);

                if(Util.validateFilePath(filePath))
                {
                    try(FileInputStream fileInputStream = new FileInputStream(filePath.toFile()))
                    {

                        var fileSize = filePath.toFile().length();

                        var bytes = 0;

                        var buffer = new byte[CHUNK_SIZE];

                        while((bytes = fileInputStream.read(buffer)) != -1)
                        {
                            // Send the file to Client Socket
                            // Convert the byte array to a Base64 encoded string
                            String payload = Base64.getEncoder().encodeToString(buffer);

                            response.put(STATUS_CODE, PENDING);

                            response.put(FILE_NAME, fileName);

                            response.put(FILE_SIZE, fileSize);

                            response.put(PAYLOAD, payload);

                            response.put(OFFSET, bytes);

                            // send packet to server
                            clientConnection.send(response.toString());
                        }

                        Server.logger.info("{}: {}", fileName, FILE_SENT_SUCCESS);

                        response.put(STATUS_CODE, SUCCESS);

                        response.put(MESSAGE, SERVER + fileName + " " + FILE_DOWNLOAD_SUCCESS);

                    } catch(IOException e) // exception caught for FileInputStream
                    {
                        Server.logger.error("Error reading file!");

                        response.put(MESSAGE, SERVER + "Error reading file!");
                    }
                }
                else
                {
                    response.put(STATUS_CODE, FAILED);

                    response.put(MESSAGE, SERVER + FILE_NOT_FOUND);

                    Server.logger.error(FILE_NOT_FOUND);
                }
            }

        }
        else
        {
            response.put(STATUS_CODE, FAILED);

            response.put(MESSAGE, SERVER + JWT_INVALID);

            Server.logger.error(JWT_INVALID);

        }

        clientConnection.send(response.toString());
    }

}
