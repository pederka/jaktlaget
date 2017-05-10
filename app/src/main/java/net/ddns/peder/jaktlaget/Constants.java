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
    public static final String SHARED_PREF_LANDMARK_TOGGLE = "landmark_toggle";
    public static final String SHARED_PREF_TEAM_TOGGLE = "team_toggle";
    public static final String SHARED_PREF_WEATHER_TOGGLE = "weather_toggle";
    public static final String SHARED_PREF_LINE_TOGGLE = "weather_toggle";
    public static final String SHARED_PREF_LOCATION_HISTORY = "location_history";
    public static final String SHARED_PREF_TEAM_LOCATION_HISTORY = "team_location_history";
    public static final String SHARED_PREF_TEAM_CODE = "team_code";

    // Default names
    public static final String DEFAULT_USER_ID = "noUser";
    public static final String DEFAULT_TEAM_ID = "noTeam";

    // Numbered  constants (consider moving some of these to settings)
    public static final int LANDMARK_ID_SIZE = 10000000;
    public static final int NOTIFICATION_ID = 1337;
    public static final long ACTIVITY_GPS_UPDATE_TIME = 5000;
    public static final long ACTIVITY_GPS_DISTANCE = 10;
    public static final long DEFAULT_UPDATE_INTERVAL = 6*60000;
    public static final long MAP_LOCATION_UPDATE_INTERVAL = 5000;
    public static final long SYNC_DELAY_ACTIVITY = 60000;

    // File names
    public static final String TMP_FILE_NAME = "syncCache";

    // For Amazon S3
    public static final String MY_BUCKET = "jaktlaget";
    public static final String OBJECT_KEY_BASE = "jaktlag/";

    // For SSL socket
    //public static final String SOCKET_ADDR = "34.197.222.14";
    public static final String SOCKET_ADDR = "peder.ddns.net";
    public static final int SOCKET_PORT = 10024;
}
