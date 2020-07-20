package com.example.anti_tampering_checker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;

/**
 * A class which recreates some of the checks performed by the SNet DeviceData class.
 */
public class DeviceData {
    public String TAG = this.getClass().getSimpleName();
    private Context context;

    public DeviceData(Context context) {
        this.context = context;
    }

    /**
     * This method is used to save all Device Data variable to a shared preference under it's name.
     */
    public void saveAllToSharedPref(){
        SharedPreferences prefs = this.context.getSharedPreferences(this.TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            editor.putString("verifiedBootState", this.getVerifyBootState());
            editor.putString("verityMode", this.getVerifyMode());
            editor.putString("securityPatchLevel", this.getSecurityPatchLevel());
            editor.putInt("oemUnlockSupported", this.getOemUnlockSupported());
            editor.putString("productBrand", this.getProductBrand());
            editor.putString("productModel", this.getProductModel());
            editor.putInt("oemLocked", this.getOemLocked(this.context));

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public String getVerifyBootState() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // https://source.android.com/devices/bootloader/unlock-trusty#properties
        return this.getStringProperty("ro.boot.verifiedbootstate");
    }

    public String getVerifyMode() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // https://android.googlesource.com/platform/system/core/+/ac5c1224cfc959b96f7a34068a807db9aaab9358%5E!/
        return this.getStringProperty("ro.boot.veritymode");
    }

    public String getSecurityPatchLevel() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // https://stackoverflow.com/questions/41545674/how-to-determine-the-security-patch-level-of-an-android-device
        return this.getStringProperty("ro.build.version.security_patch");
    }

    public int getOemUnlockSupported() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // https://source.android.com/devices/bootloader/unlock-trusty#properties
        return this.getIntProperty("ro.oem_unlock_supported");
    }

    public String getProductBrand() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.getStringProperty("ro.product.brand");
    }

    public String getProductModel() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return this.getStringProperty("ro.product.model");
    }

    public int getOemLocked(Context context) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        int oemLocked = -1;
        if (Build.VERSION.SDK_INT > 23) {
            Class<?> cPersistentDataBlockManager = Class.forName("android.service.persistentdata.PersistentDataBlockManager");
            Object oPersistentDataBlockManager = context.getSystemService((String) Class.forName("android.content.Context").getField("PERSISTENT_DATA_BLOCK_SERVICE").get(null));
            if (oPersistentDataBlockManager != null) {
                oemLocked= ((Integer) cPersistentDataBlockManager.getDeclaredMethod("getFlashLockState", new Class[0]).invoke(oPersistentDataBlockManager, new Object[0])).intValue();
            }
        } else {
            // https://source.android.com/devices/bootloader/unlock-trusty#properties
            oemLocked = this.getIntProperty("ro.boot.flash.locked");
        }
        return oemLocked;
    }

    private String getStringProperty(String propertyName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // https://github.com/phhusson/SafetyNet/blob/master/com/google/android/gms/people/PeopleConstants.java
        return (String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class}).invoke(null, new Object[]{propertyName});
    }

    private int getIntProperty(String propertyName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // https://github.com/phhusson/SafetyNet/blob/master/com/google/android/gms/people/PeopleConstants.java
        return ((Integer) Class.forName("android.os.SystemProperties").getMethod("getInt", new Class[]{String.class, Integer.TYPE}).invoke(null, new Object[]{propertyName, -1})).intValue();
    }

}
