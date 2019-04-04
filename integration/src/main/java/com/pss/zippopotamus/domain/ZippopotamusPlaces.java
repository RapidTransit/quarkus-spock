package com.pss.zippopotamus.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class ZippopotamusPlaces {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne
    protected ZippopotamusResult parent;

    @JsonProperty("place name")
    protected String name;

    @JsonProperty("state")
    protected String state;

    @JsonProperty("state abbreviation")
    protected String abbreviation;

    @JsonProperty("longitude")
    protected BigDecimal longitude;

    @JsonProperty("latitude")
    protected BigDecimal latitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZippopotamusResult getParent() {
        return parent;
    }

    public void setParent(ZippopotamusResult parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZippopotamusPlaces that = (ZippopotamusPlaces) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (abbreviation != null ? !abbreviation.equals(that.abbreviation) : that.abbreviation != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        return latitude != null ? latitude.equals(that.latitude) : that.latitude == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        return result;
    }
}
