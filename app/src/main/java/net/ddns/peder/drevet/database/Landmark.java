package net.ddns.peder.drevet.database;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName="TeamLandmarks")
public class Landmark {
    private String landmarkId;
    private String description;
    private String user;
    private String team;
    private float latitude;
    private float longitude;

    @DynamoDBHashKey(attributeName="LandmarkId")
    public String getLandmarkId() {
        return landmarkId;
    }
    public void setLandmarkId(String landmarkId) {
        this.landmarkId = landmarkId;
    }

    @DynamoDBAttribute(attributeName="description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBAttribute(attributeName="team")
    public String getTeam() {
        return team;
    }
    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBAttribute(attributeName="user")
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    @DynamoDBAttribute(attributeName="latitude")
    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName="longitude")
    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
