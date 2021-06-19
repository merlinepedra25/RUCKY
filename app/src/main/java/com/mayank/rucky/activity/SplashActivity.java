package com.mayank.rucky.activity;

import static android.util.Base64.decode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mayank.rucky.R;
import com.mayank.rucky.utils.Config;
import com.mayank.rucky.utils.Constants;
import com.mayank.rucky.utils.Networks;

import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class SplashActivity extends AppCompatActivity {

    private Config config;

    public static int distro = 0;

    public static String getSHA512;
    public static int minAndroidSDK;
    public static boolean nightly;

    static double currentVersion;
    static double newVersion;

    static int currentNightly;
    static int newNightly;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        config = new Config(this);
        if (config.getDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setTheme(Constants.themeList[config.getAccentTheme()]);
        if (config.getInitState() && config.getSec()) biometric();
        else splash();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (config.getDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setTheme(Constants.themeList[config.getAccentTheme()]);
    }

    public void biometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                finishAffinity();
                System.exit(0);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                splash();
                getKey();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                finishAffinity();
                System.exit(0);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getResources().getString(R.string.unlock))
                .setSubtitle(getResources().getString(R.string.auth))
                .setNegativeButtonText(getResources().getString(R.string.btn_cancel))
                .setConfirmationRequired(false)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    void getKey() {
        try{
            KeyStore keyStore = KeyStore.getInstance(Constants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE);
            keyStore.load(null);
            KeyStore.Entry entry = keyStore.getEntry(Constants.KEYSTORE_PROVIDER_ANDROID_KEYSTORE,null);
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            String k64 = config.getKeyStore1();
            byte[] tmp = decode(k64, Base64.DEFAULT);
            EditorActivity.key = new SecretKeySpec(cipher.doFinal(tmp),"AES");
            String k642 = config.getKeyStore2();
            byte[] tmp2 = decode(k642,Base64.DEFAULT);
            EditorActivity.iv = new IvParameterSpec(cipher.doFinal(tmp2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void launchNext() {
        if(config.getInitState()) {
            startActivity(new Intent(SplashActivity.this, EditorActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, InitActivity.class));
        }
        finish();
    }

    private void splash() {
        queue = Volley.newRequestQueue(this);
        getReleaseSigningHash();
        if (config.getUpdateFlag()) {
            preCheckAppUpdate();
        }
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(flags);
        setContentView(R.layout.activity_splash);
        RelativeLayout parentLayout = findViewById(R.id.splashParentLayout);
        parentLayout.setBackground(ContextCompat.getDrawable(this, Constants.themeSplashBorder[config.getAccentTheme()]));
        ImageView i1 = findViewById(R.id.imageViewBG);
        FrameLayout l2= findViewById(R.id.splashTextView);
        i1.setAnimation(AnimationUtils.loadAnimation(this,R.anim.rotate));
        l2.setAnimation(AnimationUtils.loadAnimation(this,R.anim.downtoup));
        new Handler().postDelayed(this::launchNext, 3000);
    }

    private void getReleaseSigningHash() {
        String NetHunter = "x2j+O+TND/jjH0ryjO/2ROPpjCvHoHK/XnjrgdAHJfM=";
        String GitHubRelease = "0Xv/I6xP6Q1wKbIqCgXi4CafhKZtOZLOR575TiqN93s=";
        String GitHubNightly = "eEk+yGxeE5dXukQ4HiGYS4eEyTAcoC6Mfm1OX/1l12c=";
        String debug = "im5KgLli2rx4iEvMVXotXGpfiR1/eqXEwBO2YQ6uP70=";
        ArrayList<String> hashList = new ArrayList<>();
        nightly = false;
        try {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P) {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (info.signingInfo.hasMultipleSigners()) {
                    for (Signature signature: info.signingInfo.getApkContentsSigners()) {
                        MessageDigest md = MessageDigest.getInstance("SHA256");
                        md.update(signature.toByteArray());
                        hashList.add(new String(Base64.encode(md.digest(), 0)));
                    }
                } else {
                    for (Signature signature: info.signingInfo.getSigningCertificateHistory()) {
                        MessageDigest md = MessageDigest.getInstance("SHA256");
                        md.update(signature.toByteArray());
                        hashList.add(new String(Base64.encode(md.digest(), 0)));
                    }
                }
            } else {
                @SuppressLint("PackageManagerGetSignatures")
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA256");
                    md.update(signature.toByteArray());
                    hashList.add(new String(Base64.encode(md.digest(), 0)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < hashList.size(); i++) {
            if(hashList.get(i).trim().equals(NetHunter)) {
                distro = R.string.releaseNetHunter;
                config.setUpdateFlag(false);
            } else if(hashList.get(i).trim().equals(GitHubRelease)) {
                distro = R.string.releaseGitHub;
                config.setUpdateFlag(true);
            } else if(hashList.get(i).trim().equals(GitHubNightly)) {
                distro = R.string.releaseGitHubNightly;
                nightly = true;
                config.setUpdateFlag(true);
            } else if(hashList.get(i).trim().equals(debug)) {
                distro = R.string.releaseTest;
                nightly = true;
                config.setUpdateFlag(true);
            } else {
                distro = R.string.releaseOthers;
                config.setUpdateFlag(false);
            }
        }
    }

    private void preCheckAppUpdate() {
        getCurrentAppVersion();
        getNewAppVersion();
    }

    private void getCurrentAppVersion() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion = Double.parseDouble(pInfo.versionName);
            currentNightly = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getNewAppVersion() {
        Networks n = new Networks();
        if (n.isNetworkPresent(this)) {
            Runnable runnable = () -> {
                try {
                    URL url = new URL("https://github.com/mayankmetha/Rucky/releases/latest");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.getInputStream();
                    String str = conn.getHeaderField( "Location" );
                    if (str.isEmpty()) {
                        newVersion = 0;
                    } else {
                        newVersion = Double.parseDouble(str.substring(str.lastIndexOf('/') + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (nightly || newVersion > currentVersion) {
                    queue.add(getUpdateMetadata());
                }
            };
            new Thread(runnable).start();
        }
    }

    private StringRequest getUpdateMetadata() {
        String url = nightly ? "https://raw.githubusercontent.com/mayankmetha/Rucky/master/nightly/rucky.cfg" : "https://github.com/mayankmetha/Rucky/releases/download/" + newVersion + "/rucky.cfg";
        return new StringRequest(Request.Method.GET, url,
            (Response.Listener<String>) response -> {
                String[] lines = response.split("\\r?\\n");
                try {
                    minAndroidSDK = lines[0] != null ? Integer.parseInt(lines[0]) : 0;
                    newNightly = lines[1] != null ? Integer.parseInt(lines[1]) : currentNightly;
                } catch (Exception e) {
                    minAndroidSDK = 0;
                    newNightly = currentNightly;
                }
                if (Build.VERSION.SDK_INT != 0 && Build.VERSION.SDK_INT >= minAndroidSDK) queue.add(getUpdateSignature());
            }, (Response.ErrorListener) error -> {
                minAndroidSDK = 0;
                newNightly = currentNightly;
            }
        );
    }

    private StringRequest getUpdateSignature() {
        String url = nightly ? "https://raw.githubusercontent.com/mayankmetha/Rucky/master/nightly/rucky.sha512" : "https://github.com/mayankmetha/Rucky/releases/download/" + newVersion + "/rucky.sha512";
        return new StringRequest(Request.Method.GET, url, (Response.Listener<String>) response -> getSHA512 = response, (Response.ErrorListener) error -> getSHA512 = "");
    }
}
