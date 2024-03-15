package com.remoteFSv2.utils;

import java.util.Scanner;

public class Common
{
    public static String limitedLengthInputPrompt(String identity)
    {
        Scanner sc = new Scanner(System.in);
        var input = "";
        for(int chance = 0; chance < 3; chance++)
        {
            System.out.print("Enter " + identity + ": ");

            input = sc.next();

            if(input.length() >= 6)
            {

                break;
            }
            else
            {
                System.out.println(identity + " length should be >= 6!");
            }
        }

        return input;
    }
}
