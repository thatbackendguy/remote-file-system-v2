package com.remoteFSv2.client.handler;

import static com.remoteFSv2.client.Client.*;
import static com.remoteFSv2.utils.Constants.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class User
{
    private JSONObject request = new JSONObject();

    public static HashMap<String, String> userData = new HashMap<>();

    public final Socket socket;

    public final BufferedReader reader;

    public final PrintWriter writer;

    public User(Socket socket) throws IOException
    {

        this.socket = socket;

        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.writer = new PrintWriter(socket.getOutputStream(), true);

    }

    public boolean authenticateUser(String command, String username, String password) throws JSONException, IOException, NullPointerException
    {
        if(command.equals(LOGIN))
        {
            request.clear();

            request.put(COMMAND, LOGIN);

            request.put("username", username.trim());

            request.put("password", password.trim());

            writer.println(request.toString());

            var response = reader.readLine();

            JSONObject resJSON = new JSONObject(response);

            if(resJSON.getString(STATUS_CODE).equals(SUCCESS))
            {

                userData.put(username, resJSON.getString(TOKEN));

                System.out.println(resJSON.getString(MESSAGE));

                logger.info(LOGIN_SUCCESS);

                return true;
            }
            else
            {
                System.out.println(resJSON.getString(MESSAGE));

                logger.error("Login Failed!");

                return false;
            }

        }
        
        else if(command.equals(REGISTER))
        {
            request.put(COMMAND, REGISTER);

            request.put("username", username.trim());

            request.put("password", password.trim());

            writer.println(request.toString());

            var response = reader.readLine();

            JSONObject resJSON = new JSONObject(response);

            if(resJSON.getString(STATUS_CODE).equals(SUCCESS))
            {
                System.out.println(resJSON.getString(MESSAGE));

                logger.info(LOGIN_SUCCESS);

                return true;
            }
            else
            {
                System.out.println(resJSON.getString(MESSAGE));

                logger.error(fatal, REGISTRATION_ERROR);

                return false;
            }
        }
        return false;
    }


    public void close() throws IOException
    {
        reader.close(); // Close input stream

        writer.close(); // Close output stream

        socket.close(); // Close socket connection
    }
}
