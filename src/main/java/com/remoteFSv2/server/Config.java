package com.remoteFSv2.server;

import java.util.concurrent.*;

public class Config
{
    public static final String ROOT_DIR_SERVER = "/home/yash/IdeaProjects/remoteFSv2/src/main/resources/ServerFS/";

    public static final int PORT = 12345;
    public static final String JWT_SECRET = "remoteFS";

    public static final long JWT_EXPIRATION_TIME_MS = 3600000; // 1 hour

    public static final int corePoolSize = 8; // Number of core threads
    public static final int maximumPoolSize = 12; // Maximum number of threads
    public static final long keepAliveTime = 60; // Time (in seconds) for idle threads to wait before termination
    public static final TimeUnit unit = TimeUnit.SECONDS;
    public static final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100); // Queue capacity


}
