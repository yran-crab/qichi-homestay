package com.bnbms.service;

import com.bnbms.entity.Facility;
import com.bnbms.model.dto.FacilitySaveDTO;

import java.util.List;

public interface FacilityService {

    List<Facility> listAll();

    Facility getById(Long id);

    Long create(FacilitySaveDTO dto);

    void update(Long id, FacilitySaveDTO dto);

    void remove(Long id);
}
