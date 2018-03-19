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
        String timestamp = e.getTimestamp();
        long time = Long.valueOf( timestamp.substring(0,timestamp.indexOf('.')));
        long now = Instant.now().toEpochMilli();
        now -= 500; //allow for some latency, as time will have elapsed since msg was sent.
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

        // #4 from Reactive Transformation: display received chirps via subscribe, subscribe receives Chirp instances.
        chirps.subscribe(e -> { System.out.println( e.getUserId() + "::" + e.getMessage() ); } );

        // #7 from Reactive Transformation: comment line above, uncomment lines below
        //     display 3 chirps. use Chirp.toString to display, note this doesn't exit..
        // chirps.take(3)
        //     .subscribe(
        //         chirp -> System.out.println(chirp),
        //         Throwable::printStackTrace,
        //         () -> System.out.println("Done")
        //     );

        // #6 from Reactive Transformation: uppercase messages? map converts to String, subscribe dumps Strings using System.out.println
        // chirps.map( e -> e.getUserId() +"::" + e.getMessage().toUpperCase() )
        //     .subscribe(System.out::println );

        // #7 from Reactive Transformation: sliding window over messages? subscribe gets list of 3 chirps at a time
        // chirps.buffer(3,1)
        //     .subscribe(System.out::println);

        // #8 from Reactive Transformation:
        //     map converts each chirp into a message length
        //     sliding window over chirp lengths,
        //     gives observable of each set of lens,
        //     flatmapsingle then drops that to a single total using reduce.
        //     then map converts that total into an average by dividing by no of chirps in the window.
        // int chirpsToCount=3;
        // chirps.map(e -> e.getMessage().length() )
        //     .window(chirpsToCount,1)
        //     .flatMapSingle(lens -> lens.reduce(0, (total,next) -> total + next))
        //     .map(e -> (e*1.0)/(chirpsToCount*1.0) )
        //     .subscribe(System.out::println);
    }
}
