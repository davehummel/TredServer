package me.davehummel.tredserver.fish.history;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Embeddable
public class HistoryID implements Serializable {

    @NotNull
    @JsonFormat//(pattern = "yyyy-MM-ddThh:mm:ss" )
    private Date date;

    @NotNull
    private String name;

    public HistoryID(){}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryID historyID = (HistoryID) o;

        if (!date.equals(historyID.date)) return false;
        return name.equals(historyID.name);
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
