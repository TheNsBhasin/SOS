
package com.nsbhasin.sos.socketmanagers;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nsbhasin.sos.Configuration;

import lombok.Getter;

public class GroupOwnerSocketHandler extends Thread {

    private static final String TAG = "GroupOwnerSocketHandler";

    private ServerSocket socket = null;
    private Handler handler;
    @Getter
    InetAddress ipAddress;

    /**
     * Class constructor.
     *
     * @param handler Represents the Handler required in order to communicate
     * @throws IOException Exception throwed by {@link ServerSocket} (SERVERPORT).
     */
    public GroupOwnerSocketHandler(@NonNull Handler handler) throws IOException {
        try {
            socket = new ServerSocket(Configuration.GROUPOWNER_PORT);
            this.handler = handler;
            Log.d("GroupOwnerSocketHandler", "Socket Started");
        } catch (IOException e) {
            Log.e(TAG, "IOException during open ServerSockets with port 4545", e);
            pool.shutdownNow();
            throw e;
        }

    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            Configuration.THREAD_COUNT, Configuration.THREAD_COUNT,
            Configuration.THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * Method to close the group owner sockets and kill this entire thread.
     */
    public void closeSocketAndKillThisThread() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException during close Socket", e);
            }
            pool.shutdown();
        }
    }

    /**
     * Method to start the GroupOwnerSocketHandler.
     * Attention you can't stop this method, because there is a while(true) inside.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                if (socket != null && !socket.isClosed()) {
                    Socket clientSocket = socket.accept(); //because now i'm connected with the client/peer device
                    pool.execute(new ChatManager(clientSocket, handler));
                    ipAddress = clientSocket.getInetAddress();
                    Log.d(TAG, "Launching the I/O handler");
                }
            } catch (IOException e) {
                //if there is an exception, after closing socket and pool, the execution stops with a "break".
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ioe) {
                    Log.e(TAG, "IOException during close Socket", ioe);
                }
                pool.shutdownNow();
                break; //stop the while(true).
            }
        }
    }

}
