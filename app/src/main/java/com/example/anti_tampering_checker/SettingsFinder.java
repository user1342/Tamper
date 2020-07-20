package com.example.anti_tampering_checker;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * A class that recreates the functionality of the SNet Settings finder class
 */
public class SettingsFinder {

    private final String TAG = this.getClass().getSimpleName();
    private final String settingsClass;
    Context context;

    HashMap<Integer, String> fingerprintMap = new HashMap<Integer, String>() {
        {
            put(-1, "FINGERPRINT_UNKNOWN");
            put(0, "FINGERPRINT_NOT_SUPPORTED");
            put(1, "FINGERPRINT_ENROLLED");
            put(2, "FINGERPRINT_UNENROLLED");
        }
    };

    HashMap<Integer, String> encryptionMap = new HashMap<Integer, String>() {
        {
            put(-1, "ENCRYPTION_STATUS_UNKNOWN");
            put(0, "ENCRYPTION_STATUS_UNSUPPORTED");
            put(1, "ENCRYPTION_STATUS_INACTIVE");
            put(2, "ENCRYPTION_STATUS_ACTIVATING");
            put(3, "ENCRYPTION_STATUS_ACTIVE");
            put(4, "ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY");
            put(5, "ENCRYPTION_STATUS_ACTIVE_PER_USER");
        }
    };

    HashMap<Integer, String> lockscreenMap = new HashMap<Integer, String>() {
        {
            put(-1, "LOCK_SCREEN_TYPE_UNKNOWN");
            put(0, "LOCK_SCREEN_TYPE_NONE");
            put(1, "LOCK_SCREEN_TYPE_SECURE_UNKNOWN");
            put(2, "LOCK_SCREEN_TYPE_PIN");
            put(3, "LOCK_SCREEN_TYPE_PATTERN");
            put(4, "LOCK_SCREEN_TYPE_FACE_PIN");
            put(5, "LOCK_SCREEN_TYPE_FACE_PATTERN");
            put(6, "LOCK_SCREEN_TYPE_PASSWORD");
        }
    };

    HashMap<Integer, String> notificationMap = new HashMap<Integer, String>() {
        {
            put(2, "NOTIFICATION_TYPE_NONE");
            put(0, "NOTIFICATION_TYPE_PRIVATE");
            put(1, "NOTIFICATION_TYPE_PUBLIC");
            put(-1, "NOTIFICATION_TYPE_SECRET");
        }
    };

    public SettingsFinder(Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT < 17) {
            this.settingsClass = "android.provider.Settings$Secure";
        } else {
            this.settingsClass = "android.provider.Settings$Global";
        }
    }

    public void saveAllToSharedPrefs(){
        SharedPreferences prefs = this.context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            editor.putString("fingerprintStatus", this.fingerprintMap.get(this.getFingerprintStatus()));
            editor.putBoolean("adbEnabled", this.getAdbEnabled());
            editor.putInt("lockScreenTimeout", this.getLockScreenTimeout());
            editor.putString("lockScreenType", this.lockscreenMap.get(this.getLockScreenType()));
            editor.putBoolean("nonMarketAppsEnabled", this.nonMarketAppsEnabled());
            editor.putString("notificationVisibility", this.notificationMap.get(this.getNotificationVisibility()));
            //todo editor.putBoolean("smartLockEnabled", this.mDeviceSettings.smartLockEnabled);
            editor.putString("storageEncryptionStatus", this.encryptionMap.get(this.getStorageEncryptionStatus()));
            //todo editor.putBoolean("smartLockStatusObtained", this.mDeviceSettings.smartLockStatusObtained);

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        editor.apply();
    }

    public int getNotificationVisibility(){
        Notification notification = new Notification.Builder(this.context).build();
        int visibility = 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification != null) {
                visibility = notification.visibility;
            }
        }
        return  visibility;
    }


    public int getLockScreenType() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int lockScreenType = -1;

        if (Build.VERSION.SDK_INT >= 16) {
            if (((KeyguardManager) this.context.getSystemService("keyguard")).isKeyguardSecure()) {
                lockScreenType = 1;
            } else {
                lockScreenType = 0;
            }
        } else {
            switch (getInt("lockscreen.password_type", -1)) {
                case 32768:
                    if (!new File(Environment.getDataDirectory().getAbsolutePath(), "/system/password.key").exists()) {
                        lockScreenType = 5;
                        break;
                    } else {
                        lockScreenType = 4;
                        break;
                    }
                case 65536:
                    if (getInt("lock_pattern_autolock", 0) == 0) {
                        lockScreenType = 0;
                        break;
                    } else {
                        lockScreenType = 3;
                        break;
                    }
                case 131072:
                    lockScreenType = 2;
                    break;
                case 262144:
                case 327680:
                    lockScreenType = 6;
                    break;
                default:
                    lockScreenType = 0;
                    break;
            }
        }

        return lockScreenType;
    }


    public int getLockScreenTimeout() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getInt("lock_screen_lock_after_timeout", 0);
    }

    private boolean getAdbEnabled() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getInt("adb_enabled", 0) != 0;
    }

    private boolean nonMarketAppsEnabled() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return getInt("install_non_market_apps", 0) != 0;
    }

    public int getStorageEncryptionStatus() {
        if (Build.VERSION.SDK_INT >= 11) {
            return ((DevicePolicyManager) this.context.getSystemService("device_policy")).getStorageEncryptionStatus();
        } else {
            return  0;
        }
    }

    private int getFingerprintStatus() {
        int fingerprintStatus = -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) this.context.getSystemService("fingerprint");

            if (!fingerprintManager.isHardwareDetected()) {
                fingerprintStatus = 0;
            } else if (fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintStatus = 1;
            } else {
                fingerprintStatus = 2;
            }
        }

        return fingerprintStatus;
    }


    /**
     * A helper method to get an int property
     * @param setting
     * @param defaultValue
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private int getInt(String setting, int defaultValue) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return ((Integer) Class.forName(this.settingsClass).getMethod("getInt", new Class[]{ContentResolver.class, String.class, Integer.TYPE}).invoke(null, new Object[]{context.getContentResolver(), setting, Integer.valueOf(defaultValue)})).intValue();

    }


}
