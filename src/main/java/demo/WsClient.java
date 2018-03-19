package demo;

import io.reactivex.Flowable;

import com.tagantroy.rxjava2ws.AutoManagedWebSocket;
import com.tagantroy.rxjava2ws.WebSocketEvent;

import java.time.Instant;

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
        logging.setLevel(Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();


        AutoManagedWebSocket rxsocket = new AutoManagedWebSocket(
             new Request.Builder().url("ws://localhost:9000/api/activity/Fred/live").build(),
             client);

        Flowable<Chirp> chirps = rxsocket.observe()
            .doOnNext( e -> {
                if ( e instanceof WebSocketEvent.OpenedEvent ) {
                   System.out.println("Connected.");
                }
            })
            .filter( e -> e instanceof WebSocketEvent.StringMessageEvent )
            .map( e -> (WebSocketEvent.StringMessageEvent) e )
            .map( e -> e.getText() )
            .map( e -> gson.fromJson(e, Chirp.class) )
            //remove historic messages.
            .filter( WsClient::chirpIsRecent );

        chirps.subscribe(e -> { System.out.println( e.getUserId() + "::" + e.getMessage() ); } );
    }
}
