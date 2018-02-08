package demo;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Completable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.HttpException;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

public final class RestClient {

    SocketAddress serverAddress = new InetSocketAddress("localhost",9000);

    String users[] = { "Fred", "Barney", "Wilma", "Betty" };
    String names[] = { "Fred Flatstone", "Barney Robble", "Wilma Flatstone", "Betty Robble" };
    String friends[][] = { { "Barney", "Wilma" }, { "Betty", "Fred", "Wilma" }, { "Betty", "Fred" }, { "Wilma", "Barney" } };

    Random random = new Random(); 

    private static class UserInfo {
       String name;
       String userId;
       Collection<String> friends;
       public String getName(){ return name; }
       public void setName(String name){ this.name=name; }
       public String getUserId(){ return userId; }
       public void setUserId(String userId){ this.userId = userId; }
       public Collection<String> getFriends(){ return friends; }
       public void setFriends(Collection<String> friends){ this.friends = friends; }
       public String toString(){ return userId+"::"+name+(friends==null?"":"::"+friends.toString()); }
    } 
 
    public static class FriendId {
       String friendId;
       public String getFriendId(){ return friendId; }
       public void setFriendId(String friendId){ this.friendId = friendId; }
    }

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
    
    private interface UserAPI {
       @GET("/api/users/{id}")
       Observable<UserInfo> getUserInfo(@Path("id") String id);

       @POST("/api/users")
       Completable createUser(@Body UserInfo user);

       @POST("/api/users/{id}/friends")
       Completable addFriend(@Path("id") String id, @Body FriendId friend);
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
 
    private UserAPI userAPI = retrofit.create(UserAPI.class);
    private ChirpAPI chirpAPI = retrofit.create(ChirpAPI.class);

    private void createUser(String user){
      //find index for friend in data arrays.
      int i=0;
      for(i=0; i<users.length && !users[i].equals(user); i++){}
 
      //create user object
      UserInfo u = new UserInfo();
      u.setName(names[i]);
      u.setUserId(users[i]);

      //invoke create user api for user. 
      userAPI.createUser(u).subscribe( () -> {}, throwable -> { throwable.printStackTrace(); } );

      //addFriends
      addFriends(user);
    }

    private void addFriends(String user){
      //find index for friend in data arrays.
      int i=0;
      for(i=0; i<users.length && !users[i].equals(user); i++){}

      String f[] = friends[i];

      userAPI.getUserInfo(user).subscribe( u -> {
				Observable.fromArray(f).subscribe(friend -> {
					if( !u.getFriends().contains(friend) ){
                                          FriendId fid = new FriendId();
					  fid.setFriendId(friend);
                                          userAPI.addFriend(user, fid).subscribe( () -> {}, throwable -> { throwable.printStackTrace(); } ); 
                                        }
                                                   });
                                                });
      
    }

    private void checkAndCreateUsers(){
        Observable.fromArray(users).subscribe( user -> {
          Observable<UserInfo> ui = userAPI.getUserInfo(user);
          ui.subscribe(  info -> { 
				   //user exists.. nothing to do.
                                   System.out.println( info );
                                   //check friends.
                                   addFriends(user);
                                 } 
                      , error -> { 
				   //if the error is a 404, user didn't exist.. so create it.
                                   if(error instanceof HttpException && ((HttpException)error).code()==404){
                                    createUser(user);
                                   }else{
                                    error.printStackTrace();  
                                    }
                                 }
                      );
        } );
    }

    private void sendChirp(Chirp chirp){
       System.out.println("Sending "+chirp);
       chirpAPI.addChirp(chirp.getUserId(), chirp).subscribe( () -> {}, throwable -> { throwable.printStackTrace(); } ); 
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
    private void randomChirps(){

       Flowable<String> delay = Flowable.just("")
                                    .switchMap(dummy -> { long time=randomTime(); return Flowable.timer(time, TimeUnit.MILLISECONDS).map(a -> ""+time); } )
                                    .repeat();

       Flowable<String> messages = Flowable.using(
                      () -> new BufferedReader(new FileReader("chirps.txt")),
                      reader -> Flowable.fromIterable(() -> reader.lines().iterator()),
                      reader -> reader.close()
              ).repeat();

       delay.zipWith(messages, (d, msg) -> ""+d+"::"+msg  )
            .blockingSubscribe( chirp -> sendChirp(chirp) ); 
    }

    public static void main(String[] args) throws Exception{
       RestClient rc = new RestClient();
       //rc.logging.setLevel(Level.BODY);
       rc.checkAndCreateUsers();
       //rc.logging.setLevel(Level.BODY);
       rc.randomChirps(); //currently blockingSubscribe, will never return.
    }
}
