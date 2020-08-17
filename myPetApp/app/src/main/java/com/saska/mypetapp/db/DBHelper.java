package com.saska.mypetapp.db;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.amazonaws.amplify.generated.graphql.CreateFFactMutation;
import com.amazonaws.amplify.generated.graphql.CreatePetMutation;
import com.amazonaws.amplify.generated.graphql.CreateUserMutation;
import com.amazonaws.amplify.generated.graphql.ListPetsQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.UpdateUserMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.saska.mypetapp.PetDetailsActivity;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.LoginHelper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

import type.CreateFFactInput;
import type.CreatePetInput;
import type.CreateUserInput;
import type.ModelPetFilterInput;
import type.ModelStringInput;
import type.ModelUserFilterInput;
import type.UpdateUserInput;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class DBHelper {

    private static String CLASS_NAME = "DBHelper";

    private static ArrayList<ListUsersQuery.Item> mUsers;
    private static ListUsersQuery.Item user;
    private static ListPetsQuery.Item pet;

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

    public static void updateUser(final ProgressBar progressBar, final Window window, final Toaster toaster, User user){
        UpdateUserInput input = UpdateUserInput.builder()
                .id(user.getIdUser())
                .name(user.getName())
                .surname(user.getSurname())
                .phone(user.getPhone())
                .profilePicture(user.getNewProfilePicture())
                .build();

        GraphQLCall.Callback<UpdateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<UpdateUserMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<UpdateUserMutation.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "User updated!");
                        toaster.make("Profile info updated!");
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
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
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                    }
                });
            }
        };

        UpdateUserMutation updateUserMutation = UpdateUserMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(updateUserMutation).enqueue(mutateCallback);

    }

    public static void createUser(final Activity activity, final ProgressBar progressBarRegister,
                                  final Window window, final String username, String name, String surname, String phone){

        CreateUserInput input = CreateUserInput.builder()
                .username(username)
                .name(name)
                .surname(surname)
                .phone(phone)
                .type(1)
                .profilePicture("public/avatar.png")
                .build();

        // Mutation callback code
        GraphQLCall.Callback<CreateUserMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateUserMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreateUserMutation.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "User added!");
                        progressBarRegister.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        LoginHelper.proceedToProfile(activity, username);
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failed to add user!");
                        progressBarRegister.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                    }
                });
            }
        };

        CreateUserMutation addUserMutation = CreateUserMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addUserMutation).enqueue(mutateCallback);

    }


    // PETS

    public static void addPet(final Toaster toaster, final ProgressBar progressBarAddPet, final Window window, String name, String description,
                              String location, String type, int adoption, String picture){

        CreatePetInput input = CreatePetInput.builder()
                .name(name)
                .description(description)
                .location(location)
                .type(type)
                .addoption(adoption)
                .picture(picture)
                .build();

        // Mutation callback code
        GraphQLCall.Callback<CreatePetMutation.Data> mutateCallback = new GraphQLCall.Callback<CreatePetMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreatePetMutation.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Pet added!");
                        progressBarAddPet.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        toaster.make("Pet added! :)");
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failed to add pet!");
                        progressBarAddPet.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        toaster.make("Failed to add pet!");
                    }
                });
            }
        };

        CreatePetMutation addPetMutation = CreatePetMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addPetMutation).enqueue(mutateCallback);

    }

    public static void loadPetDetails(final ProgressBar progressBar, final Context context, String name){


        Log.i("ABC", "starting load pet details");
        GraphQLCall.Callback<ListPetsQuery.Data> queryCallback = new GraphQLCall.Callback<ListPetsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<ListPetsQuery.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("ABC", "Success");
                        pet = new ArrayList<>(response.data().listPets().items()).get(0);
                        Pet dbPet = new Pet(pet);
                        Log.i("ABC", "ITEM IS : " + dbPet.getName());
                        AppContext.getContext().setSelectedPet(dbPet);
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(((Activity)context).getWindow());

                        Log.i("ABC", "STARTING ACTIVITY");
                        Intent details = new Intent((Activity)context, PetDetailsActivity.class);
                        ((Activity)context).startActivity(details);
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failure");
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(((Activity)context).getWindow());
                    }
                });
            }
        };

        ModelStringInput modelStringInput = ModelStringInput.builder().eq(name).build();
        ModelPetFilterInput modelPetFilterInput = ModelPetFilterInput.builder().name(modelStringInput).build();
        ClientFactory.appSyncClient().query(ListPetsQuery.builder().filter(modelPetFilterInput).build())
                .enqueue(queryCallback);

    }



    // Fun Facts

    public static void addFunFact(final Toaster toaster, final ProgressBar progressBarFF, final Window window, final EditText text){

        CreateFFactInput input = CreateFFactInput.builder()
                .text(text.getText().toString())
                .build();

        // Mutation callback code
        GraphQLCall.Callback<CreateFFactMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateFFactMutation.Data>() {
            @Override
            public void onResponse(@Nonnull final Response<CreateFFactMutation.Data> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Fun Fact added!");
                        progressBarFF.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        text.getText().clear();
                        toaster.make("Fun Fact added! :)");
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull final ApolloException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(CLASS_NAME, "Failed to add pet!");
                        progressBarFF.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        toaster.make("Failed to add pet!");
                    }
                });
            }
        };

        CreateFFactMutation addFFmutation = CreateFFactMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addFFmutation).enqueue(mutateCallback);

    }



}
