package com.saska.mypetapp.db;

import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreateUserMutation;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.helper.Toaster;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import type.CreateUserInput;
import type.ModelStringInput;
import type.ModelUserFilterInput;
import type.UpdateUserInput;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class DBHelper {

    private static String CLASS_NAME = "DBHelper";

    private static ArrayList<ListUsersQuery.Item> mUsers;
    private static ListUsersQuery.Item user;

    public static final ArrayList<ListUsersQuery.Item> listAllUsers(boolean wait){

        mUsers = null;

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        GraphQLCall.Callback<ListUsersQuery.Data> queryCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {

                mUsers = new ArrayList<>(response.data().listUsers().items());

                Log.i("LIST USERS TAG", "Retrieved list items: " + mUsers.toString());
                countDownLatch.countDown();
                return;

            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("LIST USERS TAG", e.toString());
                countDownLatch.countDown();
            }
        };

        ClientFactory.appSyncClient().query(ListUsersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

        if (wait){
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return mUsers;

    }

    public static ListUsersQuery.Item getUserByUsername(boolean wait, String username){

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        GraphQLCall.Callback<ListUsersQuery.Data> queryCallback = new GraphQLCall.Callback<ListUsersQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListUsersQuery.Data> response) {
                Log.i(CLASS_NAME, "Success");
                user = response.data().listUsers().items().isEmpty()? null : new ArrayList<>(response.data().listUsers().items()).get(0);
                countDownLatch.countDown();
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failure");
                        countDownLatch.countDown();
                    }
                });
            }
        };

        ModelStringInput modelStringInput = ModelStringInput.builder().eq(username).build();
        ModelUserFilterInput modelUserFilterInput = ModelUserFilterInput.builder().username(modelStringInput).build();
        ClientFactory.appSyncClient().query(ListUsersQuery.builder().filter(modelUserFilterInput).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

        if (wait){
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return user;
    }

    public static void updateUser(final Toaster toaster, User user){
        UpdateUserInput input = UpdateUserInput.builder()
                .id(user.getIdUser())
                .name(user.getName())
                .surname(user.getSurname())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .build();

        GraphQLCall.Callback<UpdateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<UpdateUserMutation.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "User updated!");
                        toaster.make("Profile info updated!");
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failed to update user!");
                        toaster.make("Something went wrong!");
                        //countDownLatch.countDown();
                    }
                });
            }
        };

        UpdateUserMutation updateUserMutation = UpdateUserMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(updateUserMutation).enqueue(mutateCallback);

    }

    public static void createUser(boolean wait, String username, String name, String surname, String phone){

        final CountDownLatch countDownLatch = new CountDownLatch(1);

        CreateUserInput input = CreateUserInput.builder()
                .username(username)
                .name(name)
                .surname(surname)
                .phone(phone)
                .type(1)
                .build();

        // Mutation callback code
        GraphQLCall.Callback<CreateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateUserMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreateUserMutation.Data> response) {
                       Log.i(CLASS_NAME, "User added!");
                        countDownLatch.countDown();
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failed to add user!");
                        countDownLatch.countDown();
                    }
                });
            }
        };

        CreateUserMutation addUserMutation = CreateUserMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addUserMutation).enqueue(mutateCallback);

        if (wait){
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
