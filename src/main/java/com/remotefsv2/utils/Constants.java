package com.remotefsv2.utils;

public class Constants
{
    public static final String MESSAGE_FORMATTER = "{}: {}";

    public static final String SERVER = "[Server] ";

    public static final String CLIENT = "[Client] ";

    // Switch case options - Request Handler
    public static final String LOGIN = "LOGIN";

    public static final String LOGOUT = "logout";

    public static final String REGISTER = "REGISTER";

    // status of req/res
    public static final String SUCCESS = "success";

    public static final String PENDING = "pending";

    public static final String FAILED = "failed";

    // commands

    public static final String LIST = "ls";

    public static final String DOWNLOAD = "get";

    public static final String REMOVE_FILE = "rm";

    public static final String UPLOAD = "put";

    public static final String DELETE = "rm";

    public static final String MKDIR = "mkdir";

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

    public static final String FILE_SIZE = "fileSize";

    public static final String PAYLOAD = "payload";

    public static final String OFFSET = "offset";

    public static final String FILE_NAME = "fileName";

    public static final String DIR_NAME = "dirName";

    public static final String CURRENT_DIR_PATH = "currPath";

    public static final String DEST_PATH = "destPath";

    public static final String PATH_SEP = "/";

    public static final String HELP = "help";

    public static final String PWD = "pwd";


    // Login Messages
    public static final String USER_NOT_FOUND = "User doesn't exists!";

    public static final String JWT_INVALID = "Invalid JWT Token!";

    public static final String INVALID_CREDENTIALS = "Invalid username or password!";

    public static final String LOGIN_SUCCESS = "Login successful!";

    // File Transfer Messages
    public static final String FILE_SENT_SUCCESS = "File sent successfully!";

    public static final String FILE_UPLOAD_ERROR = "Error uploading file!";

    public static final String FILE_UPLOAD_SUCCESS = "File uploaded successfully!";

    public static final String DIR_ALREADY_EXISTS = "Directory/File with same name already exists!";

    public static final String FILE_DOWNLOAD_ERROR = "Error downloading file!";

    public static final String FILE_DOWNLOAD_SUCCESS = "File downloaded successfully!";

    public static final String FILE_DELETE_ERROR = "Error deleting file!";

    public static final String DIR_DELETE_ERROR = "Error deleting directory!";

    public static final String FILE_DELETE_SUCCESS = "File deleted successfully!";

    public static final String DIR_DELETE_SUCCESS = "Directory deleted successfully!";


    // Server Messages
    public static final String SERVER_START_SUCCESS = "Server started successfully! at PORT: ";

    public static final String SERVER_STOP = "Server Socket closed!";

    public static final String SERVER_START_ERROR = "Error starting server!";

    public static final String SERVER_STOP_ERROR = "Error stopping server!";

    public static final String CLIENT_CONNECTED = "New client connected: ";

    public static final String SERVER_DOWN = "Server Down!";

    // Client Messages
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

    private Constants() {}
}