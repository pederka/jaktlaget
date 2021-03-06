package net.ddns.peder.jaktlaget;

/**
 * Created by peder on 3/22/17.
 */

public final class Constants {

    // Key names for shared preferences
    public static final String SHARED_PREF_LAT = "latitude";
    public static final String SHARED_PREF_LON = "longitude";
    public static final String SHARED_PREF_TIME = "last_update";
    public static final String SHARED_PREF_RUNNING = "runningService";
    public static final String SHARED_PREF_USER_ID = "userId";
    public static final String SHARED_PREF_TEAM_ID = "teamId";
    public static final String SHARED_PREF_NO_TEAM = "noTeam";
    public static final String SHARED_PREF_LANDMARK_TOGGLE = "landmark_toggle";
    public static final String SHARED_PREF_TEAM_TOGGLE = "team_toggle";
    public static final String SHARED_PREF_LINE_TOGGLE = "line_toggle";
    public static final String SHARED_PREF_CAMERA_POSITION = "camera_position";
    public static final String SHARED_PREF_LOCATION_HISTORY = "location_history";
    public static final String SHARED_PREF_TEAM_LOCATION_HISTORY = "team_location_history";
    public static final String SHARED_PREF_TEAM_CODE = "team_code";
    public static final String SHARED_PREF_FIRST_TIME_ACTIVE = "first_time_active";
    public static final String SHARED_PREF_FIRST_TIME_LANDMARK = "first_time_landmark";
    public static final String SHARED_PREF_AD_CONSENT = "ad_consent";

    // Intent extras
    public static final String EXTRA_MAP = "net.ddns.peder.INTENT_MAP";

    // Default names
    public static final String DEFAULT_USER_ID = "noUser";
    public static final String DEFAULT_TEAM_ID = "noTeam";

    // Numbered  constants (consider moving some of these to settings)
    public static final int LANDMARK_ID_SIZE = 10000000;
    public static final int NOTIFICATION_ID = 1337;
    public static final long ACTIVITY_GPS_UPDATE_TIME = 5000;
    public static final long ACTIVITY_GPS_DISTANCE = 10;
    public static final long DEFAULT_UPDATE_INTERVAL = 5;
    public static final long MAP_LOCATION_UPDATE_INTERVAL = 5000;
    public static final long MAP_TEAM_POSITION_UPDATE_INTERVAL = 5000;
    public static final long SYNC_DELAY_ACTIVITY = 10000;
    public static final long TEAM_TIME_FADE = 300000;
    public static final int CODE_LENGTH = 4;
    public static final int TEAM_LENGTH = 13;
    public static final int WEATHER_SYNC_COOLDOWN = 1000;
    public static final float SCALE_WIDTH = 200;
    public static final int AD_PERSONALIZED = 0;
    public static final int AD_NONPERSONALIZED = 1;
    public static final int AD_UNKNOWN = 2;

    // For tile cache
    public static final String DISK_CACHE_SUBDIR = "tiles";
    public static final long DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // For API
    public static final String API_URL = "https://1gq1t20qnk.execute-api.eu-central-1.amazonaws.com/dev";

    // For Google Ads
    public static final String privacy_url = "https://peder.ddns.net/jaktlaget-privacy";

    // For notifications
    public static final String CHANNEL_ID = "activeNotificationChannel";
}
