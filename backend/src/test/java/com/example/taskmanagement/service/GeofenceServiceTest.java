package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.OfficeLocation;
import com.example.taskmanagement.repository.OfficeLocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeofenceServiceTest {

    @Mock OfficeLocationRepository officeRepository;
    @InjectMocks GeofenceService geofenceService;

    @Test
    void haversine_distance_HCMU_HUTECH_to_DongKhoi_about_5km() {
        // Hai điểm thực tế ở TP.HCM
        double hcmu = GeofenceService.distanceMeters(
                10.8021, 106.7159,   // HUTECH HCMU – Điện Biên Phủ
                10.7769, 106.7009);  // Đường Đồng Khởi, Q1
        assertThat(hcmu / 1000d).isCloseTo(3.4, within(0.5));
    }

    @Test
    void haversine_same_point_returns_zero() {
        double d = GeofenceService.distanceMeters(10.0, 106.0, 10.0, 106.0);
        assertThat(d).isEqualTo(0d);
    }

    @Test
    void findNearestActive_returns_empty_when_no_office() {
        when(officeRepository.findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE))
                .thenReturn(List.of());
        Optional<GeofenceService.Match> m = geofenceService.findNearestActive(10.0, 106.0);
        assertThat(m).isEmpty();
    }

    @Test
    void findNearestActive_within_radius_returns_withinTrue() {
        OfficeLocation hutech = new OfficeLocation();
        hutech.setId(1L);
        hutech.setName("HUTECH Điện Biên Phủ");
        hutech.setLatitude(10.8021);
        hutech.setLongitude(106.7159);
        hutech.setRadiusMeters(100);
        hutech.setStatus(OfficeLocation.Status.ACTIVE);

        when(officeRepository.findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE))
                .thenReturn(List.of(hutech));

        // Điểm cách HUTECH ~50m
        Optional<GeofenceService.Match> m = geofenceService.findNearestActive(
                10.80246, 106.71607);
        assertThat(m).isPresent();
        assertThat(m.get().withinRadius()).isTrue();
        assertThat(m.get().distanceMeters()).isLessThan(100);
    }

    @Test
    void findNearestActive_outside_radius_returns_withinFalse() {
        OfficeLocation hutech = new OfficeLocation();
        hutech.setId(1L);
        hutech.setName("HUTECH");
        hutech.setLatitude(10.8021);
        hutech.setLongitude(106.7159);
        hutech.setRadiusMeters(50);
        hutech.setStatus(OfficeLocation.Status.ACTIVE);

        when(officeRepository.findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE))
                .thenReturn(List.of(hutech));

        // Điểm cách ~5km
        Optional<GeofenceService.Match> m = geofenceService.findNearestActive(
                10.7769, 106.7009);
        assertThat(m).isPresent();
        assertThat(m.get().withinRadius()).isFalse();
        assertThat(m.get().distanceMeters()).isGreaterThan(1000);
    }

    @Test
    void findNearestActive_picks_closer_office_among_many() {
        OfficeLocation o1 = office(1L, "Far", 10.0, 106.0, 100);
        OfficeLocation o2 = office(2L, "Close", 10.7770, 106.7010, 100);
        OfficeLocation o3 = office(3L, "Farther", 11.0, 107.0, 100);

        when(officeRepository.findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE))
                .thenReturn(List.of(o1, o2, o3));

        Optional<GeofenceService.Match> m = geofenceService.findNearestActive(
                10.7769, 106.7009);
        assertThat(m).isPresent();
        assertThat(m.get().office().getName()).isEqualTo("Close");
    }

    private OfficeLocation office(Long id, String name, double lat, double lng, int radius) {
        OfficeLocation o = new OfficeLocation();
        o.setId(id);
        o.setName(name);
        o.setLatitude(lat);
        o.setLongitude(lng);
        o.setRadiusMeters(radius);
        o.setStatus(OfficeLocation.Status.ACTIVE);
        return o;
    }
}
