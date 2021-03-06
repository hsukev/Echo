package com.stream.jerye.queue;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jerye on 7/8/2017.
 */

public class PreferenceUtility {
    private static SharedPreferences prefs;
    public static final String FIREBASE_REGISTRATION_TOKEN = "registration token";
    public static final String SPOTIFY_TOKEN = "token";
    public static final String ROOM_KEY = "room key";
    public static final String ROOM_TITLE = "room title";
    public static final String ROOM_PASSWORD = "room password";
    public static final String PROFILE_GENERIC = "profile";
    public static final String PROFILE_NAME = "profile name";
    public static final String PROFILE_PICTURE = "profile picture url";
    public static final String PROFILE_ID = "profile id";
    public static final String USER_KEY = "user key";

    public static void initialize(Context context) {
        prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static void setPreference(String preferenceKey, String... value) {


        switch (preferenceKey) {
            case SPOTIFY_TOKEN:
                prefs.edit().putString(SPOTIFY_TOKEN, value[0]).apply();
                break;
            case ROOM_KEY:
                prefs.edit().putString(ROOM_KEY, value[0]).apply();
                break;

            case ROOM_TITLE:
                prefs.edit().putString(ROOM_TITLE, value[0]).apply();
                break;

            case ROOM_PASSWORD:
                prefs.edit().putString(ROOM_PASSWORD, value[0]).apply();
                break;

            case PROFILE_GENERIC:
                prefs.edit()
                        .putString(PROFILE_NAME, value[0])
                        .putString(PROFILE_PICTURE, value[1])
                        .putString(PROFILE_ID, value[2])
                        .apply();
                break;
            case USER_KEY:
                prefs.edit().putString(USER_KEY,value[0]).apply();
                break;
            case FIREBASE_REGISTRATION_TOKEN:
                prefs.edit().putString(FIREBASE_REGISTRATION_TOKEN,value[0]).apply();
            default:
                break;

        }
    }

    public static String getPreference(String preferenceKey) {
        switch (preferenceKey) {
            case SPOTIFY_TOKEN:
                return prefs.getString(SPOTIFY_TOKEN, "");
            case ROOM_KEY:
                return prefs.getString(ROOM_KEY, "");
            case ROOM_TITLE:
                return prefs.getString(ROOM_TITLE, "");
            case ROOM_PASSWORD:
                return prefs.getString(ROOM_PASSWORD, "");
            case PROFILE_NAME:
                return prefs.getString(PROFILE_NAME, "");
            case USER_KEY:
                return prefs.getString(USER_KEY,"");
            default:
                return "";
        }

    }

    public static String[] getSpotifyPreference() {
        return new String[]{prefs.getString(PROFILE_NAME, ""),
                prefs.getString(PROFILE_PICTURE, ""),
                prefs.getString(PROFILE_ID, "")
        };
    }

    public static void deleteSpotifyPreferences() {
        prefs.edit().remove(PROFILE_NAME)
                .remove(PROFILE_ID)
                .remove(PROFILE_PICTURE)
                .remove(SPOTIFY_TOKEN)
                .apply();
    }

    public static void deleteRoomPreference(){
        prefs.edit().remove(ROOM_KEY).apply();
    }

    public static void deleteUserPreference(){
        prefs.edit().remove(USER_KEY).apply();
    }

}

