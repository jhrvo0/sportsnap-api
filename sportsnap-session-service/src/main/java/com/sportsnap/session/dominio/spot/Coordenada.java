package com.sportsnap.session.dominio.spot;

import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.Objects;

public class Coordenada {

    private final double latitude;
    private final double longitude;

    public Coordenada(double latitude, double longitude) {
        inclusiveBetween(-90.0, 90.0, latitude, "A latitude deve estar entre -90 e 90");
        inclusiveBetween(-180.0, 180.0, longitude, "A longitude deve estar entre -180 e 180");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Coordenada) {
            var outra = (Coordenada) obj;
            return Double.compare(latitude, outra.latitude) == 0
                && Double.compare(longitude, outra.longitude) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
