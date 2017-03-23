package net.ddns.peder.drevet.dynamoDB;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName="Positions")
public class Position {
    private String User;
    private String Team;
    private float latitude;
    private float longitude;
    private String time;

    @DynamoDBHashKey(attributeName="User")
    public String getUser() {
        return User;
    }
    public void setUser(String User) {
        this.User = User;
    }

    @DynamoDBRangeKey(attributeName="Team")
    public String getTeam() {
        return Team;
    }
    public void setTeam(String Team) {
        this.Team = Team;
    }

    @DynamoDBAttribute(attributeName="time")
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
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
