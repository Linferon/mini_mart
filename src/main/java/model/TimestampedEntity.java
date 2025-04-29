package model;

import java.sql.Timestamp;

public interface TimestampedEntity {
    Timestamp getCreatedAt();
    void setCreatedAt(Timestamp timestamp);
    Timestamp getUpdatedAt();
    void setUpdatedAt(Timestamp timestamp);
}
