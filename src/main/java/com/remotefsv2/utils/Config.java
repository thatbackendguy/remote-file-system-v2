package com.remotefsv2.utils;

public class Config
{
    private Config() {}

    public static final String ROOT_DIR_CLIENT = "ClientFS/";

    public static int CLIENT_PORT = 12345;

    public static String HOST = "localhost";

    public static final String ROOT_DIR_SERVER = "ServerFS/";

    public static final int SERVER_PORT = 12345;

    public static final int CHUNK_SIZE = 8192;

    public static final String JWT_SECRET = "remoteFS";

    public static final long JWT_EXPIRATION_TIME_MS = 3600000; // 1 hour

}
