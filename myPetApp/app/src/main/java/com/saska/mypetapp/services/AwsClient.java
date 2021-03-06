package com.saska.mypetapp.services;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.saska.mypetapp.MainActivity;
import com.saska.mypetapp.RegisterActivity;
import com.saska.mypetapp.UserActivity;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;
import com.saska.mypetapp.helper.Helper;
import com.saska.mypetapp.helper.LoginHelper;
import com.saska.mypetapp.helper.Toaster;
import com.saska.mypetapp.singletons.AppContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class AwsClient {

    private Activity activity;
    private String CLASS_NAME;
    private Toaster toaster;
    private LoginHelper loginHelper;

    public AwsClient(Activity activity) {
        this.activity = activity;
        CLASS_NAME = getClass().getName();
        toaster = new Toaster(activity);
        loginHelper = new LoginHelper(activity);
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
                                            AppContext.getContext().setUsername(username);
                                            registerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.getApplicationContext().startActivity(registerIntent);
                                        }
                                        else {
                                            // User already exists in the DB, proceed to registered page
                                            Intent userIntent = new Intent(activity, UserActivity.class);
                                            AppContext.getContext().setActiveUser(new User(user));
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
                        Log.i("QQQ", e.getMessage());
                        if (e instanceof UsernameExistsException) {
                            toaster.make("Username Exists");
                            //Toast.makeText(activity, "Username Exists", Toast.LENGTH_SHORT).show();
                        } else if (e.getMessage().contains("Password not long enough")) {
                            toaster.make("Password must contain at least 8 characters.");
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

    public static void signOut(final Activity activity, final ProgressBar progressBar, final Window window){
        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
            @Override
            public void onResult(final Void result) {
                LoginHelper.proceedLogout(activity);
                progressBar.setVisibility(View.INVISIBLE);
                Helper.unblockTouch(window);
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Helper.unblockTouch(window);
            }
        });
    }


    public void signIn(final ProgressBar progressBar, final Window window, final String username, String password) {

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
                                loginHelper.proceedLogin(username);
                                progressBar.setVisibility(View.INVISIBLE);
                                Helper.unblockTouch(window);
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
            }

            @Override
            public void onError(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(CLASS_NAME, "Sign-in error", e);
                        progressBar.setVisibility(View.INVISIBLE);
                        Helper.unblockTouch(window);
                        toaster.make("Incorrect username or password");
                    }
                });
            }
        });

    }


    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(activity.getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        File file = new File(activity.getApplicationContext().getFilesDir(), "sample.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("Howdy World!");
            writer.close();
        }
        catch(Exception e) {
            Log.e(CLASS_NAME, e.getMessage());
        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/sample.txt",
                        new File(activity.getApplicationContext().getFilesDir(),"sample.txt"));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(CLASS_NAME, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        Log.d(CLASS_NAME, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(CLASS_NAME, "Bytes Total: " + uploadObserver.getBytesTotal());
    }
}


