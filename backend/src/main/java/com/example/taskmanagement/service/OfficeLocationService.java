package com.example.taskmanagement.service;

import com.example.taskmanagement.dto.OfficeLocationRequest;
import com.example.taskmanagement.entity.OfficeLocation;
import com.example.taskmanagement.exception.ResourceNotFoundException;
import com.example.taskmanagement.repository.OfficeLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficeLocationService {

    private final OfficeLocationRepository repository;

    public OfficeLocationService(OfficeLocationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OfficeLocation> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OfficeLocation> listActive() {
        return repository.findAllByStatusOrderByNameAsc(OfficeLocation.Status.ACTIVE);
    }

    @Transactional(readOnly = true)
    public OfficeLocation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OfficeLocation", "id", id));
    }

    @Transactional
    public OfficeLocation create(OfficeLocationRequest req) {
        OfficeLocation o = new OfficeLocation();
        applyFields(o, req, true);
        return repository.save(o);
    }

    @Transactional
    public OfficeLocation update(Long id, OfficeLocationRequest req) {
        OfficeLocation o = getById(id);
        applyFields(o, req, false);
        return repository.save(o);
    }

    @Transactional
    public void delete(Long id) {
        OfficeLocation o = getById(id);
        repository.delete(o);
    }

    private void applyFields(OfficeLocation o, OfficeLocationRequest req, boolean isCreate) {
        o.setName(req.name());
        o.setAddress(req.address());
        o.setLatitude(req.latitude());
        o.setLongitude(req.longitude());
        o.setRadiusMeters(req.radiusMeters() != null ? req.radiusMeters() : 100);
        if (req.status() != null && !req.status().isBlank()) {
            o.setStatus(OfficeLocation.Status.valueOf(req.status()));
        } else if (isCreate) {
            o.setStatus(OfficeLocation.Status.ACTIVE);
        }
    }
}
