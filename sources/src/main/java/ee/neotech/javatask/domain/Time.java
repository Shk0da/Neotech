package ee.neotech.javatask.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Основная сущность для хранения timestamp
 */
@Entity
@Table(name = "time")
public class Time {

    @Id
    private Timestamp timestamp;

    public Time() {
    }

    public Time(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Time{timestamp=" + timestamp + "}";
    }
}
