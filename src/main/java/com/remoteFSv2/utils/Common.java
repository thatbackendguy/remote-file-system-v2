package com.remoteFSv2.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class Common
{
    public static void removeDirRecursively(String filePath) throws IOException
    {
        Path directoryPath = Paths.get(filePath);

        if(Files.exists(directoryPath))
        {
            Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    Files.delete(file);

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    Files.delete(dir);

                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static boolean validateFilePath(Path filePath)
    {
        try
        {
            // Check if the path exists
            if(Files.exists(filePath))
            {
                // Check if the path is a regular file (not a directory)
                return Files.isRegularFile(filePath);

            }
        } catch(Exception e)
        {
            return false;
        }

        return false;
    }

    public static String limitedLengthInputPrompt(String identity)
    {
        Scanner sc = new Scanner(System.in);

        var input = "";

        for(int chance = 2; chance >= 0; chance--)
        {
            System.out.print("Enter " + identity + ": ");

            input = sc.next();

            if(input.length() >= 6)
            {

                break;
            }
            else
            {
                System.out.println(identity + " length should be >= 6! Chances left: " + chance);
            }
        }

        return input;
    }

    public static synchronized boolean receiveFile(DataInputStream dataInputStream, String filePath)
    {
        try
        {
            var bytes = 0;

            var fileOutputStream = new FileOutputStream(filePath);

            // read file size
            var size = dataInputStream.readLong();

            var buffer = new byte[Config.CHUNK_SIZE]; // 4KB

            while(size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1)
            {
                // Here we write the file using write method
                fileOutputStream.write(buffer, 0, bytes);

                size -= bytes; // read upto file size
            }

            fileOutputStream.close();

            return true;
        } catch(IOException io)
        {
            return false;
        }
    }

    public static synchronized boolean sendFile(FileInputStream fileInputStream, DataOutputStream dataOutputStream, File file)
    {
        try
        {
            // Here we send the File length to Client
            dataOutputStream.writeLong(file.length());

            var bytes = 0;

            // Here we break file into 4KB chunks
            var buffer = new byte[Config.CHUNK_SIZE];

            while((bytes = fileInputStream.read(buffer)) != -1)
            {
                // Send the file to Client Socket
                dataOutputStream.write(buffer, 0, bytes);

                dataOutputStream.flush();
            }

            return true;
        } catch(IOException io)
        {
            return false;
        }
    }
}
