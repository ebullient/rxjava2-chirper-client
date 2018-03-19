package demo;

import io.reactivex.Flowable;
import io.reactivex.Completable;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import java.util.Random;
import java.time.Instant;
import java.io.BufferedReader;
import java.io.FileReader;

import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public final class Chirper {

    SocketAddress serverAddress = new InetSocketAddress("localhost",9000);

    Random random = new Random();

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

    private interface ChirpAPI {
       @POST("/api/chirps/live/{id}")
       Completable addChirp(@Path("id") String id, @Body Chirp chirp );
    }

    private HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(logging)
        .build();

    private Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("http://localhost:9000")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(client)
        .build();

    private ChirpAPI chirpAPI = retrofit.create(ChirpAPI.class);

    void randomChirps(){
        // logging.setLevel(Level.BASIC);

        // #8 from Reactive Event Source: uncomment next 7 lines
        // Flowable<String> delay = Flowable.just("")
        //     .switchMap(dummy -> {
        //         long time=randomTime();
        //         return Flowable.timer(time, TimeUnit.MILLISECONDS)
        //             .map(a -> ""+time);
        //     })
        //     .repeat();

        Flowable<String> messages = Flowable.using(
                () -> new BufferedReader(new FileReader("chirps.txt")),
                    reader -> Flowable.fromIterable(() -> reader.lines().iterator()),
                    reader -> reader.close()
                );
        // #8 from Reactive Event Source: comment line above, uncomment lines below
        //     )
        //     .repeat();
        //
        // delay.zipWith(messages, (d, msg) -> ""+d+"::"+msg  )
        //     .blockingSubscribe( chirp -> sendChirp(chirp) );

        // #8 from Reactive Event Source: comment next 5 lines
        messages.zipWith(messages, (d, msg) -> ""+d+"::"+msg  )
            .blockingSubscribe(
                chirp -> sendChirp(chirp),
                throwable -> { throwable.printStackTrace(); }
            );
    }

    private void sendChirp(Chirp chirp){
        System.out.println("Sending "+chirp);
        chirpAPI.addChirp(chirp.getUserId(), chirp).subscribe(
            () -> {},
            throwable -> { throwable.printStackTrace(); }
        );
    }

    private void sendChirp(String chirpString){
       String parts[] = chirpString.split("::");
       Chirp chirp = new Chirp();
       chirp.setUserId(parts[1]);
       chirp.setMessage(parts[2]);
       chirp.setTimestamp(""+Instant.now().toEpochMilli());
       chirp.setUuid( UUID.randomUUID().toString() );
       sendChirp(chirp);
    }

    private long randomTime(){
       return 500L + random.nextInt(6000);
    }
}
