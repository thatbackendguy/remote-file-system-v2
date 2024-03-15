package com.remoteFSv2.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Config
{
    public static final String ROOT_DIR_CLIENT = "/home/yash/IdeaProjects/remoteFSv2/src/main/resources/ClientFS/";

    public static final int CLIENT_PORT = 12345;

    public static final String HOST = "localhost";

    public static final String ROOT_DIR_SERVER = "/home/yash/IdeaProjects/remoteFSv2/src/main/resources/ServerFS/";

    public static final int SERVER_PORT = 12345;
    public static final String JWT_SECRET = "remoteFS";

    public static final long JWT_EXPIRATION_TIME_MS = 3600000; // 1 hour

    public static final int CORE_POOL_SIZE = 8; // Number of core threads
    public static final int MAXIMUM_POOL_SIZE = 12; // Maximum number of threads
    public static final long KEEP_ALIVE_TIME = 60; // Time (in seconds) for idle threads to wait before termination
    public static final TimeUnit UNIT = TimeUnit.SECONDS;
    public static final BlockingQueue<Runnable> WORKERS = new ArrayBlockingQueue<>(100); // Queue capacity



}
