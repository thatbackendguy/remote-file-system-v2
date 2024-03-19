package com.remoteFSv2.client.handler;

import com.remoteFSv2.client.Client;
import com.remoteFSv2.utils.Constants;
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
        if(command.equals(Constants.LOGIN))
        {
            request.clear();

            request.put(Constants.COMMAND, Constants.LOGIN);

            request.put("username", username.trim());

            request.put("password", password.trim());

            writer.println(request.toString());

            var response = reader.readLine();

            JSONObject resJSON = new JSONObject(response);

            if(resJSON.getInt(Constants.STATUS_CODE) == 0)
            {

                userData.put(username, resJSON.getString(Constants.TOKEN));

                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.info(Constants.LOGIN_SUCCESS);

                return true;
            }
            else
            {
                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.error("Login Failed!");

                return false;
            }

        }
        else if(command.equals(Constants.REGISTER))
        {
            request.put(Constants.COMMAND, Constants.REGISTER);

            request.put("username", username.trim());

            request.put("password", password.trim());

            writer.println(request.toString());

            var response = reader.readLine();

            JSONObject resJSON = new JSONObject(response);

            if(resJSON.getInt(Constants.STATUS_CODE) == 0)
            {
                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.info(Constants.LOGIN_SUCCESS);

                return true;
            }
            else
            {
                System.out.println(resJSON.getString(Constants.MESSAGE));

                Client.logger.error(Client.fatal, Constants.REGISTRATION_ERROR);

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
