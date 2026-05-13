package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.OfficeLocation;
import com.example.taskmanagement.repository.OfficeLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GeofenceService {

    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    private final OfficeLocationRepository officeRepository;

    public GeofenceService(OfficeLocationRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    /** Khoảng cách Haversine giữa hai điểm trên mặt đất, đơn vị mét. */
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /** Kết quả khi đánh giá một điểm GPS đối với danh sách office active. */
    public record Match(OfficeLocation office, double distanceMeters, boolean withinRadius) {}

    /** Tìm office gần nhất với (lat, lng) trong các office ACTIVE. */
    public Optional<Match> findNearestActive(double lat, double lng) {
        List<OfficeLocation> offices = officeRepository
                .findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE);
        if (offices.isEmpty()) return Optional.empty();
        OfficeLocation best = null;
        double bestDist = Double.MAX_VALUE;
        for (OfficeLocation o : offices) {
            double d = distanceMeters(lat, lng, o.getLatitude(), o.getLongitude());
            if (d < bestDist) {
                bestDist = d;
                best = o;
            }
        }
        if (best == null) return Optional.empty();
        return Optional.of(new Match(best, bestDist, bestDist <= best.getRadiusMeters()));
    }
}
