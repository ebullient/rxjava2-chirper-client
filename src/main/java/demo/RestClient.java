package demo;

import io.reactivex.Observable;
import io.reactivex.Completable;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.HttpException;
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

    private interface UserAPI {
       @GET("/api/users/{id}")
       Observable<UserInfo> getUserInfo(@Path("id") String id);

       @POST("/api/users")
       Completable createUser(@Body UserInfo user);

       @POST("/api/users/{id}/friends")
       Completable addFriend(@Path("id") String id, @Body FriendId friend);
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

    void checkAndCreateUsers(){
        // logging.setLevel(Level.BODY);

        // #6: look for this println in the output
        Observable.fromArray(users).subscribe( user -> {
            //find index for friend in data arrays.
            int i=0;
            for(i=0; i<users.length && !users[i].equals(user); i++){}
            final int index = i;

            System.out.println(index + ": " + user);

            // #7, uncomment the next chunk of lines, except for call to addFriends
            // Observable<UserInfo> ui = userAPI.getUserInfo(user);
            // ui.subscribe(
            //     info -> {
            //         //user exists.. nothing to do.
            //         System.out.println( info );
            //         // #9, uncomment to add friends.
            //         addFriends(user, index);
            //     },
            //     error -> {
            //         //if the error is a 404, user didn't exist.. so create it.
            //         if ( error instanceof HttpException && ((HttpException)error).code()==404 ) {
            //             System.out.println( "User not found: " + error );
            //             createUser(user, index);
            //         }else{
            //             System.out.println(  "Some other error occurred: " + error );
            //             error.printStackTrace();
            //         }
            //     }
            // );
        });
    }

    private void createUser(String user, int i){
        //create user object
        UserInfo u = new UserInfo();
        u.setName(names[i]);
        u.setUserId(users[i]);

        // #8 invoke create user api for user.
        userAPI.createUser(u).subscribe(
            () -> {},
            throwable -> { throwable.printStackTrace(); }
        );
        //
        // #9, addFriends
        // addFriends(user,i);
    }

    private void addFriends(String user, int i){
        String f[] = friends[i];

        // Get info from the user
        userAPI.getUserInfo(user).subscribe(
            u -> {
                // Create an observable from the array of friends
                Observable.fromArray(f).subscribe(
                    friend -> {
                        // For each friend, if they aren't in this user's friend list, add them
                        if( ! u.getFriends().contains(friend) ) {
                            FriendId fid = new FriendId();
                            fid.setFriendId(friend);
                            userAPI.addFriend(user, fid).subscribe(
                                () -> {},
                                throwable -> { throwable.printStackTrace(); }
                            );
                        }
                    }
                );
            }
        );
    }


}
