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
    public static final String SHARED_PREF_LINE_TOGGLE = "line_toggle";
    public static final String SHARED_PREF_LOCATION_HISTORY = "location_history";
    public static final String SHARED_PREF_TEAM_LOCATION_HISTORY = "team_location_history";
    public static final String SHARED_PREF_TEAM_CODE = "team_code";

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
    public static final long MAP_TEAM_POSITION_UPDATE_INTERVAL = 10000;
    public static final long SYNC_DELAY_ACTIVITY = 30000;
    public static final long TEAM_TIME_FADE = 300000;
    public static final int CODE_LENGTH = 4;
    public static final int WEATHER_SYNC_COOLDOWN = 60000;

    // For SSL socket
    public static final String SOCKET_ADDR = "peder.ddns.net";
    public static final int SOCKET_PORT = 10024;

    // For Google Ads
    //public static final String ADMOB_ID = "ca-app-pub-3940256099942544~3347511713";
    public static final String ADMOB_ID = "ca-app-pub-1180457891371684~5778981859";
}
