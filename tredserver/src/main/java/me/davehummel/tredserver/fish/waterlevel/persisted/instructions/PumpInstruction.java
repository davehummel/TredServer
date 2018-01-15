package me.davehummel.tredserver.fish.waterlevel.persisted.instructions;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by dmhum on 2/14/2017.
 */

@Entity
public class PumpInstruction {
    @Id
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error")
    private String error;

    public PumpInstruction(String name){this.name = name;}

    protected PumpInstruction(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "PumpInstruction{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }
}
