package com.saska.mypetapp.services;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.services.cognitoidentityprovider.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.saska.mypetapp.MainActivity;
import com.saska.mypetapp.RegisterActivity;
import com.saska.mypetapp.UserActivity;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Toaster;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class AwsClient {

    private Activity activity;
    private String CLASS_NAME;
    private Toaster toaster;
    private boolean signInResultFlag;

    public AwsClient(Activity activity) {
        this.activity = activity;
        CLASS_NAME = getClass().getName();
        toaster = new Toaster(activity);
    }

    public void initialize() {

        AWSMobileClient.getInstance().initialize(activity, new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i(CLASS_NAME, "INIT - onResult: " + userStateDetails.getUserState());

                        switch (userStateDetails.getUserState()) {
                            case SIGNED_IN:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String username = AWSMobileClient.getInstance().getUsername();
                                        Log.i(CLASS_NAME, String.format("USER '%s' STILL SIGNED IN", username));
                                        ListUsersQuery.Item user = DBHelper.getUserByUsername(true, username);
                                        if (user == null){
                                            Log.i(this.getClass().getName(), "No user found!");
                                            // First time login, update the database
                                            Intent registerIntent = new Intent(activity, RegisterActivity.class);
                                            registerIntent.putExtra("USERNAME", username);
                                            registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.getApplicationContext().startActivity(registerIntent);
                                        }
                                        else {
                                            // User already exists in the DB, proceed to registered page
                                            Intent userIntent = new Intent(activity, UserActivity.class);
                                            userIntent.putExtra("USER", new User(user));
                                            userIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.getApplicationContext().startActivity(userIntent);

                                        }

                                    }
                                });
                                break;
                            case SIGNED_OUT:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(CLASS_NAME, "USER SIGNED OUT");
                                    }
                                });
                                break;
                            default:
                                AWSMobileClient.getInstance().signOut();
                                break;
                        }

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(CLASS_NAME, "INIT - Initialization error.", e);
                    }
                }
        );


    }

    public void signUp(final EditText username, final EditText password, final EditText email, final EditText code, final Button button) {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", email.getText().toString());
        AWSMobileClient.getInstance().signUp(username.getText().toString(), password.getText().toString(), attributes, null, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.i(CLASS_NAME, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            toaster.make("Confirm sign-up with: " + details.getDestination() + " and insert the code.");
                            code.setEnabled(true);
                            button.setEnabled(true);
                            username.setEnabled(false);
                            password.setEnabled(false);
                            email.setEnabled(false);
                        } else {
                            toaster.make("Sign-up done.");
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e instanceof UsernameExistsException) {
                            toaster.make("Username Exists");
                            //Toast.makeText(activity, "Username Exists", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof InvalidParameterException || e instanceof InvalidPasswordException) {
                            //Toast.makeText(activity, "Username Exists", Toast.LENGTH_SHORT).show();
                            toaster.make("Invalid password");
                        }
                        Log.e(CLASS_NAME, "Sign-up error", e);
                    }
                });
            }
        });
    }

    public void confirmCode(String username, String code) {
        AWSMobileClient.getInstance().confirmSignUp(username, code, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CLASS_NAME, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            toaster.make("Confirm sign-up with: " + details.getDestination());
                        } else {
                            Intent login = new Intent(activity, MainActivity.class);
                            activity.startActivity(login);
                            toaster.make("Sign-up done.");
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(CLASS_NAME, "Confirm sign-up error", e);
            }
        });
    }


    public boolean signIn(boolean wait, String username, String password) {

        signInResultFlag = false;
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        AWSMobileClient.getInstance().signIn(username, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(CLASS_NAME, "Sign-in callback state: " + signInResult.getSignInState());
                        switch (signInResult.getSignInState()) {
                            case DONE:
                                Log.i(CLASS_NAME, "Sign-in done");
                                break;
                            case SMS_MFA:
                                toaster.make("Please confirm sign-in with SMS.");
                                break;
                            case NEW_PASSWORD_REQUIRED:
                                toaster.make("Please confirm sign-in with new password.");
                                break;
                            default:
                                toaster.make("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                                break;
                        }
                    }
                });
                signInResultFlag = true;
                countDownLatch.countDown();
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(CLASS_NAME, "Sign-in error", e);
                        toaster.make("Incorrect username or password");
                    }
                });
                signInResultFlag = false;
                countDownLatch.countDown();
            }
        });

        if (wait){
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return signInResultFlag;

    }


}
