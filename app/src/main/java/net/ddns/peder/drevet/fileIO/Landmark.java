package net.ddns.peder.drevet.fileIO;

public class Landmark {
    private long id;
    private String description;
    private String user;
    private String team;
    private int showed;
    private int shared;
    private double latitude;
    private double longitude;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeam() {
        return team;
    }
    public void setTeam(String team) {
        this.team = team;
    }

    public int getShowed() {
        return showed;
    }
    public void setShowed(int showed) {
        this.showed = showed;
    }

    public int getShared() {
        return shared;
    }
    public void setShared(int shared) {
        this.shared = shared;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
