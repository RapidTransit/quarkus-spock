package com.pss.zippopotamus.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ZippopotamusResult {

    @Id
    @JsonProperty("postal code")
    protected String zipcode;

    @JsonProperty("country")
    protected String country;

    @JsonProperty("country abbreviation")
    protected String abbreviation;

    @JsonProperty("places")
    @OneToMany(mappedBy = "parent")
    protected List<ZippopotamusPlaces> places = new ArrayList<>();

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<ZippopotamusPlaces> getPlaces() {
        return places;
    }

    public void setPlaces(List<ZippopotamusPlaces> places) {
        this.places = places;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZippopotamusResult that = (ZippopotamusResult) o;

        if (zipcode != null ? !zipcode.equals(that.zipcode) : that.zipcode != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        return abbreviation != null ? abbreviation.equals(that.abbreviation) : that.abbreviation == null;

    }

    @Override
    public int hashCode() {
        int result = zipcode != null ? zipcode.hashCode() : 0;
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (abbreviation != null ? abbreviation.hashCode() : 0);
        return result;
    }
}
