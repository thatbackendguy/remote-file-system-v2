package com.remoteFSv2.server.controller;

import com.remoteFSv2.server.handler.ClientConnection;
import com.remoteFSv2.utils.Common;
import com.remoteFSv2.utils.Constants;
import com.remoteFSv2.utils.JWTUtil;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;


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
                var directory = Paths.get(rootDirectory ,username,currPath);

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
                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.EMPTY_DIRECTORY);

                    response.put(Constants.STATUS_CODE, 1);
                }

                clientConnection.send(response.toString());

            } catch(IOException e)
            {
                System.out.println(Constants.SERVER + "Error listing files!" + e.getMessage());
            }
        }


    }

    //    public void getFileName(Integer fileChoice)
    //    {
    //        if(fileMap.containsKey(fileChoice))
    //        {
    //            return "START_RECEIVING " + fileMap.get(fileChoice);
    //        }
    //        else
    //        {
    //            return "FILE_NOT_FOUND";
    //        }
    //    }
    //
    //    public void sendFile(String fileName)
    //    {
    //
    //        var file = new File(rootDirectory + fileName);
    //
    //        try(var fileInputStream = new FileInputStream(file))
    //        {
    //            var dataOutputStream = new DataOutputStream(clientConnection.clientSocket.getOutputStream());
    //
    //            // Here we send the File length to Client
    //            dataOutputStream.writeLong(file.length());
    //
    //            var bytes = 0;
    //
    //            // Here we  break file into 8KB chunks
    //            var buffer = new byte[8192];
    //
    //            while((bytes = fileInputStream.read(buffer)) != -1)
    //            {
    //                // Send the file to Client Socket
    //                dataOutputStream.write(buffer, 0, bytes);
    //
    //                dataOutputStream.flush();
    //            }
    //
    //            // close the file here
    //            fileInputStream.close();
    //
    //            return true;
    //
    //        } catch(FileNotFoundException e)
    //        {
    //            System.out.println("[Server] File not found!");
    //
    //        }
    //        catch(IOException io)
    //        {
    //            System.out.println("[Server] Data input/output stream error...\nError: " + io.getMessage());
    //
    //        }
    //
    //        return false;
    //    }
    //
    //    public void receiveFile(String fileName)
    //    {
    //        try
    //        {
    //            var bytes = 0;
    //
    //            var dataInputStream = new DataInputStream(clientConnection.clientSocket.getInputStream());
    //
    //            var fileOutputStream = new FileOutputStream(rootDirectory + fileName);
    //
    //            var size = dataInputStream.readLong(); // read file size
    //
    //            var buffer = new byte[8192]; // 8KB
    //
    //            while(size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1)
    //            {
    //                // Here we write the file using write method
    //                fileOutputStream.write(buffer, 0, bytes);
    //
    //                size -= bytes; // read upto file size
    //            }
    //
    //            fileOutputStream.close();
    //
    //            return true;
    //
    //        } catch(NullPointerException npe)
    //        {
    //            System.out.println("[Server] Server is down!");
    //
    //            return false;
    //        }catch(IOException e)
    //        {
    //            System.out.println("[Server] Error in receiving file from server...\nError: " + e.getMessage());
    //
    //            return false;
    //        }
    //    }
    //

    public void deleteFile(String token, String fileName)
    {
        response.clear();

        try
        {
            var username = JWTUtil.verifyToken(token);

            if(username != null)
            {
                var file = Paths.get(rootDirectory, username, fileName);

                if(Common.validateFilePath(file))
                {
                    Files.deleteIfExists(file);

                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.FILE_DELETE_SUCCESS);

                }
                else
                {
                    System.out.println(Constants.SERVER + Constants.FILE_NOT_FOUND);

                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.FILE_NOT_FOUND);
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

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.FILE_DELETE_ERROR);

            System.out.println(Constants.SERVER + Constants.FILE_DELETE_ERROR + e.getMessage());
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

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.MKDIR_SUCCESS);

            } catch(FileAlreadyExistsException e)
            {
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.DIR_ALREADY_EXISTS);

                clientConnection.send(response.toString());

            } catch(IOException e)
            {
                // parent dir not exists
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_PATH);
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
                    Common.removeDirRecursively(String.valueOf(dirPath));

                    response.put(Constants.STATUS_CODE, 0);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.DIR_DELETE_SUCCESS);

                } catch(IOException e)
                {
                    // delete failed
                    response.clear();

                    response.put(Constants.STATUS_CODE, 1);

                    response.put(Constants.MESSAGE, Constants.SERVER + Constants.DIR_DELETE_ERROR);
                }
            }
            else
            {
                // delete failed
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.DIR_DELETE_ERROR);
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
                var resDirPath = Paths.get(currPath,destPath);

                response.put(Constants.STATUS_CODE, 0);

                response.put(Constants.CURRENT_DIR_PATH,String.valueOf(resDirPath));
            }
            else
            {
                response.clear();

                response.put(Constants.STATUS_CODE, 1);

                response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_PATH);
            }


        }
        else
        {
            response.put(Constants.STATUS_CODE, 1);

            response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);

        }

        clientConnection.send(response.toString());
    }

//    public void goBackOneDir(String token, String currPath)
//    {
//        response.clear();
//
//        var username = JWTUtil.verifyToken(token);
//
//        if(username != null)
//        {
//            var dirPath = Paths.get(rootDirectory, username, currPath);
//
//            if(Files.isDirectory(dirPath) && Files.exists(dirPath))
//            {
//                var parentPath = dirPath.getParent();
//
//                var resDirPath = Paths.get(currPath,destPath);
//
//                response.put(Constants.STATUS_CODE, 0);
//
//                response.put(Constants.CURRENT_DIR_PATH,String.valueOf(resDirPath));
//            }
//            else
//            {
//                response.clear();
//
//                response.put(Constants.STATUS_CODE, 1);
//
//                response.put(Constants.MESSAGE, Constants.SERVER + Constants.INVALID_PATH);
//            }
//
//
//        }
//        else
//        {
//            response.put(Constants.STATUS_CODE, 1);
//
//            response.put(Constants.MESSAGE, Constants.SERVER + Constants.UNAUTHORIZED_ACCESS);
//
//        }
//
//        clientConnection.send(response.toString());
//    }

}
