/*
 * MIT License
 *
 * Copyright (c) 2024 RUB-SE-LAB-2024
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.msbattery.batterymonitoringapp.connection;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient extends WebSocketListener {

    private static WebSocketClient instance;

    private List<Consumer<String>> listenerList = new ArrayList<>();

    private String uID;
    private WebSocket socket;

    private boolean opened;

    private boolean closed;

    // Send the last message once more when we lose connection
    private String message;

    boolean ready = false;
    boolean decoding = false;
    boolean isConnecting = false;

    private Handler handler;
    private int reconnectInterval = 5000;

    // Decode every 5th message. --> Maybe later calculate seconds after last decoded message?
    private int decodeRate = 8;
    private int decodeCounter = 0;

    public static synchronized WebSocketClient getInstance() {
        if (instance == null) {
            instance = new WebSocketClient();
        }
        return instance;
    }

    private WebSocketClient() {
        this.uID = "MMMDTP";
        this.opened = false;
        this.closed = false;

        handler = new Handler(Looper.getMainLooper());

        connect();

    }

    public void connect() {
        if(isConnecting) return;
        isConnecting = true;
        if (this.socket != null) {
            this.socket.close(1000, "Reconnecting"); // Close the previous socket gracefully
            Log.d("WebSocket", "Reconnecting");
            this.socket = null; // Ensure we start fresh
        }
        decodeCounter = 0;
        // With basic auth, with HTTPS
        this.run("selab", "DawQDtwyzzrw", "tsac.rubmotorsport.de/live", true);
        // Without basic auth, disabled HTTPS (only HTTP)
        // IMPORTANT! : TO USE HTTP TAKE NOTE OF COMMENT IN AndroidManifest.xml !!! ADD android:usesCleartextTraffic="true"
        //this.run("motorsport.cookiezz.de", false);

        // Keine Ahnung ob das ein besserer Weg ist als einfach ne while schleife
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            if(this.decoding) {
                scheduler.shutdown();
            }
            if (this.ready) {
                try {
                    // Send the start command
                    start();
                    // set ready to false, in case we need to reconnect
                    this.ready = false;
                } catch (Exception e) {
                    Log.e("WebSocket", "Error in ready check", e);
                }
            } else if (this.closed) {
                Log.e("WebSocket", "Websocket was closed or failed to open!");
                scheduler.shutdown();
            }
        }, 0, 2, TimeUnit.SECONDS); // Initial delay, then run every 2 seconds
        isConnecting = false;
    }

    public void setDecodeRate(int decodeRate) {
        this.decodeRate = decodeRate; //for testing purposes
    }


    private void run(String username, String password, String url, boolean https) {
        OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .authenticator(new Authenticator() {
                        @Nullable
                        @Override
                        public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
                            if (responseCount(response) >= 3) {
                                return null;
                            }
                            // https://stackoverflow.com/questions/22490057/android-okhttp-with-basic-authentication
                            String credential = Credentials.basic(username, password);
                            return response.request().newBuilder().header("Authorization", credential).build();
                        }
                    }).retryOnConnectionFailure(true)
                    .build();

        String conType;
        conType = https ? "wss" : "ws";

        Request request = new Request.Builder()
                .url(conType + "://" + url)
                .build();
        this.socket = client.newWebSocket(request, this);
    }

    private void run(String url, boolean https) {

        OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();

        String conType;
        conType = https ? "wss" : "ws";

        Request request = new Request.Builder()
                .url(conType + "://" + url)
                .build();
        this.socket = client.newWebSocket(request, this);
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        super.onOpen(webSocket, response);
        this.opened = true;
        webSocket.send("ping");
        webSocket.send("setID/" + this.uID);

        Log.d("WebSocket", "Opened");
    }

    public void start() {
        Log.d("WebSocket", "Send start command");
        if(this.socket != null)
            this.socket.send("start");
        else
            Log.d("WebSocket", "Websocket is not opened. Please open the websocket or wait a few seconds.");
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        super.onMessage(webSocket, text);
        if(text.equals("ok " + this.uID)) {
            ready = true;
            return;
        }
        if(text.endsWith("=")) {
            if(!this.decoding)
                this.decoding = true;
            if(this.decodeCounter != this.decodeRate) {
                this.decodeCounter++;
                return;
            }
            this.message = text;
            this.notifyListeners(text);
            //Log.d("Received and processing", text);
            this.decodeCounter = 0;
        }
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.d("Received", bytes.hex());
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
        this.opened = false;
        this.closed = false;
        this.decoding = false;
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        Log.e("WebSocket", "Failed to connect to websocket!");
        if(t.getMessage() != null)
            Log.e("WebSocket", t.getMessage());
        if (response != null) {
            Log.e("WebSocket", "Response : " + response.toString());
        }
        this.opened = false;
        this.closed = false;
        this.decoding = false;
        this.notifyListeners(this.message);

        handler.postDelayed(this::connect, reconnectInterval);
    }


    public boolean getOpened() {
        return this.opened;
    }

    public void addListener(Consumer<String> consumer) {
        listenerList.add(consumer);
    }

    public void removeListener(Consumer<String> consumer) {
        listenerList.remove(consumer);
    }

    public void notifyListeners(String message) {
        try {
            for (Consumer<String> listener : listenerList) {
                listener.accept(message);
            }
        } catch(Exception e) {
            Log.e("Websocket", "error while parsing message: "  + e.getMessage());
        }

    }

    public boolean isClosed() {
        return closed;
    }
}
