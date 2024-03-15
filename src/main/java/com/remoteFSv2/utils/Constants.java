package com.remoteFSv2.utils;

public class Constants
{

    public static final String SERVER = "[Server] ";

    public static final String IO_ERROR = "Error in Input/Output stream!";

    public static final String CLIENT = "[Client] ";

    // Switch case options - Request Handler
    public static final String LOGIN = "LOGIN";

    public static final String LOGOUT = "logout";

    public static final String REGISTER = "REGISTER";

    // commands

    public static final String LIST = "ls";

    public static final String DOWNLOAD = "get";

    public static final String REMOVE_FILE = "rm";

    public static final String START_SENDING = "START_SENDING";

    public static final String UPLOAD = "put";

    public static final String DELETE = "rm";

    public static final String MKDIR = "mkdir";

    public static final String BACK = "back";

    public static final String CD = "cd";

    public static final String RMDIR = "rmdir";


    // Register Messages
    public static final String REGISTRATION_ERROR = "Registration failed!";

    public static final String REGISTRATION_SUCCESS = "Registration successful!";

    // JSON KEYS

    public static final String MESSAGE = "message";

    public static final String STATUS_CODE = "status";

    public static final String TOKEN = "token";

    public static final String COMMAND = "command";


    // Login Messages
    public static final String LOGIN_ERROR = "Login failed!";

    public static final String JWT_INVALID = "Invalid JWT Token!";

    public static final String JWT_EMPTY = "JWT Token not found!";

    public static final String INVALID_CREDENTIALS = "Invalid username or password!";

    public static final String LOGIN_SUCCESS = "Login successful!";

    // File Transfer Messages
    public static final String FILE_TRANSFER_ERROR = "Error during file transfer!";

    public static final String FILE_UPLOAD_ERROR = "Error uploading file!";

    public static final String FILE_UPLOAD_SUCCESS = "File uploaded successfully!";

    public static final String FILE_DOWNLOAD_ERROR = "Error downloading file!";

    public static final String FILE_DOWNLOAD_SUCCESS = "File downloaded successfully!";

    public static final String FILE_DELETE_ERROR = "Error deleting file!";

    public static final String FILE_DELETE_SUCCESS = "File deleted successfully!";


    // Server Messages
    public static final String SERVER_START_SUCCESS = "Server started successfully!\nPORT: ";

    public static final String SERVER_STOP = "Server Socket closed!";

    public static final String SERVER_START_ERROR = "Error starting server!";

    public static final String SERVER_STOP_ERROR = "Error stopping server!";

    public static final String CLIENT_CONNECTED = "New client connected: ";

    public static final String CLIENT_DISCONNECTED = "Client disconnected: ";

    public static final String SERVER_DOWN = "Server Down!";


    // Client Messages
    public static final String CLIENT_START_SUCCESS = "Client started successfully!";

    public static final String CLIENT_START_ERROR = "Error starting client!";

    public static final String CONNECTION_ESTABLISHED = "Connection established with server! ";

    public static final String CONNECTION_ERROR = "Error connecting to server!";

    // File System Messages
    public static final String FILE_NOT_FOUND = "File not found!";

    public static final String DIRECTORY_NOT_FOUND = "Directory not found!";

    public static final String EMPTY_DIRECTORY = "Directory is empty!";

    public static final String INVALID_FILE_CHOICE = "Invalid file choice!";

    // General Messages
    public static final String OPERATION_SUCCESSFUL = "Operation successful!";

    public static final String OPERATION_FAILED = "Operation failed!";

    public static final String INVALID_INPUT = "Invalid input!";

    public static final String NETWORK_ERROR = "Network error!";

    public static final String IMPROPER_JSON = "Improper JSON format!";

    private Constants()
    {
        // Private constructor to prevent instantiation
    }
}