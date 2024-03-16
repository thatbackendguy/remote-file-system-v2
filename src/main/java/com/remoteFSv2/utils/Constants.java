package com.remoteFSv2.utils;

public class Constants
{

    public static final String SERVER = "[Server] ";

    public static final String CLIENT = "[Client] ";

    // Switch case options - Request Handler
    public static final String LOGIN = "LOGIN";

    public static final String LOGOUT = "logout";

    public static final String REGISTER = "REGISTER";

    // commands

    public static final String LIST = "ls";

    public static final String DOWNLOAD = "get";

    public static final String REMOVE_FILE = "rm";

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

    public static final String FILE_NAME = "fileName";

    public static final String DIR_NAME = "dirName";

    public static final String CURRENT_DIR_PATH = "currPath";

    public static final String DEST_PATH = "destPath";


    // Login Messages
    public static final String USER_NOT_FOUND = "User doesn't exists!";

    public static final String JWT_INVALID = "Invalid JWT Token!";

    public static final String JWT_EMPTY = "JWT Token not found!";

    public static final String INVALID_CREDENTIALS = "Invalid username or password!";

    public static final String LOGIN_SUCCESS = "Login successful!";

    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access!";

    // File Transfer Messages
    public static final String FILE_SENT_SUCCESS = "File sent successfully!";

    public static final String FILE_SENT_ERROR = "Error! File not sent!";

    public static final String FILE_UPLOAD_ERROR = "Error uploading file!";

    public static final String FILE_UPLOAD_SUCCESS = "File uploaded successfully!";

    public static final String DIR_ALREADY_EXISTS = "Directory already exists!";

    public static final String FILE_DOWNLOAD_ERROR = "Error downloading file!";

    public static final String FILE_DOWNLOAD_SUCCESS = "File downloaded successfully!";

    public static final String FILE_DELETE_ERROR = "Error deleting file!";

    public static final String DIR_DELETE_ERROR = "Error deleting directory!";

    public static final String FILE_DELETE_SUCCESS = "File deleted successfully!";

    public static final String DIR_DELETE_SUCCESS = "Directory deleted successfully!";


    // Server Messages
    public static final String SERVER_START_SUCCESS = "Server started successfully!\nPORT: ";

    public static final String SERVER_STOP = "Server Socket closed!";

    public static final String SERVER_START_ERROR = "Error starting server!";

    public static final String SERVER_STOP_ERROR = "Error stopping server!";

    public static final String CLIENT_CONNECTED = "New client connected: ";

    public static final String SERVER_DOWN = "Server Down!";

    public static final String IO_ERROR = "Error in Input/Output stream!";

    // Client Messages

    public static final String CLIENT_START_ERROR = "Error starting client!";

    public static final String CONNECTION_ERROR = "Error connecting to server!";

    // File System Messages
    public static final String FILE_NOT_FOUND = "File not found!";

    public static final String MKDIR_SUCCESS = "Directory created successfully!";

    public static final String MKDIR_FAIL = "Directory failed to create!";

    public static final String EMPTY_DIRECTORY = "Directory is empty!";

    public static final String INVALID_PATH = "Invalid path!";

    // General Messages


    public static final String INVALID_INPUT = "Invalid input!";

    public static final String IMPROPER_JSON = "Improper JSON format!";

    private Constants()
    {
        // Private constructor to prevent instantiation
    }
}