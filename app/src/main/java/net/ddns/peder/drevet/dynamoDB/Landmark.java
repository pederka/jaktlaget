package net.ddns.peder.drevet.dynamoDB;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName="TeamLandmarks")
public class Landmark {
    private String LandmarkId;
    private String description;
    private String user;
    private String Team;
    private float latitude;
    private float longitude;

    @DynamoDBHashKey(attributeName="LandmarkId")
    public String getLandmarkId() {
        return LandmarkId;
    }
    public void setLandmarkId(String LandmarkId) {
        this.LandmarkId = LandmarkId;
    }

    @DynamoDBAttribute(attributeName="description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    @DynamoDBRangeKey(attributeName="Team")
    public String getTeam() {
        return Team;
    }
    public void setTeam(String Team) {
        this.Team = Team;
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
