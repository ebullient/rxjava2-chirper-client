package demo;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Completable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tagantroy.rxjava2ws.AutoManagedWebSocket;
import com.tagantroy.rxjava2ws.WebSocketEvent;
import com.tagantroy.rxjava2ws.WebSocketEvent.StringMessageEvent;
import com.tagantroy.rxjava2ws.RxWebSocket;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.Collection;
import java.util.Arrays;
import java.util.UUID;
import java.util.Random;
import java.time.Instant;
import java.io.BufferedReader;
import java.io.FileReader;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public final class WsClient {

    public static class Chirp {
       String userId;
       String message;
       String timestamp;
       String uuid;
       public String getUserId() { return userId; }
       public void setUserId(String userId){ this.userId = userId; }
       public String getMessage() { return message; }
       public void setMessage(String message) { this.message = message; }
       public String getTimestamp() { return timestamp; }
       public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
       public String getUuid() { return uuid; }
       public void setUuid(String uuid) { this.uuid = uuid; }
       public String toString() { return ""+timestamp+"::"+userId+"::"+message; }
    }

    private static boolean chirpIsRecent(Chirp e){
	String timestamp=e.getTimestamp();
        long time=Long.valueOf( timestamp.substring(0,timestamp.indexOf('.')));
        long now=Instant.now().toEpochMilli();
        now-=500; //allow for some latency, as time will have elapsed since msg was sent.
        return time >= now;
    }

    public static void main(String[] args) throws Exception{

        Gson gson = new Gson();
 
	HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    	OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        //logging.setLevel(Level.BODY);

    	AutoManagedWebSocket rxsocket = new AutoManagedWebSocket(
			 new Request.Builder().url("ws://localhost:9000/api/activity/Fred/live").build(),
			 client); 

        Flowable<Chirp> chirps = rxsocket.observe() 
           .doOnNext( e -> { if ( e instanceof WebSocketEvent.OpenedEvent ) { System.out.println("Connected."); } } )
           .filter( e -> e instanceof WebSocketEvent.StringMessageEvent )
           .map( e -> (WebSocketEvent.StringMessageEvent)e )
           .map( e -> e.getText() )
           .map( e -> gson.fromJson(e, Chirp.class) )
           //remove historic messages.
           .filter( WsClient::chirpIsRecent );

        chirps.subscribe(e -> { System.out.println( e.getUserId() + "::" + e.getMessage() ); } ); 
        
    }
}
