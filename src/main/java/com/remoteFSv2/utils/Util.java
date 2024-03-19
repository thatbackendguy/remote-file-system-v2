package com.remoteFSv2.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class Util
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
}
