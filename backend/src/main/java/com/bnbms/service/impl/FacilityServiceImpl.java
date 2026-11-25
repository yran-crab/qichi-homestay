package com.bnbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bnbms.common.BusinessException;
import com.bnbms.entity.Facility;
import com.bnbms.entity.HomestayFacility;
import com.bnbms.mapper.FacilityMapper;
import com.bnbms.mapper.HomestayFacilityMapper;
import com.bnbms.model.dto.FacilitySaveDTO;
import com.bnbms.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityServiceImpl implements FacilityService {

    private final FacilityMapper facilityMapper;
    private final HomestayFacilityMapper homestayFacilityMapper;

    @Override
    public List<Facility> listAll() {
        return facilityMapper.selectList(new QueryWrapper<Facility>().orderByAsc("sort").orderByAsc("id"));
    }

    @Override
    public Facility getById(Long id) {
        Facility f = facilityMapper.selectById(id);
        if (f == null) {
            throw new BusinessException("设施不存在");
        }
        return f;
    }

    @Override
    public Long create(FacilitySaveDTO dto) {
        checkNameUnique(dto.getName(), null);
        Facility f = new Facility();
        f.setName(dto.getName());
        f.setIcon(dto.getIcon());
        f.setSort(dto.getSort() == null ? 0 : dto.getSort());
        facilityMapper.insert(f);
        return f.getId();
    }

    @Override
    public void update(Long id, FacilitySaveDTO dto) {
        Facility f = getById(id);
        checkNameUnique(dto.getName(), id);
        f.setName(dto.getName());
        f.setIcon(dto.getIcon());
        f.setSort(dto.getSort() == null ? 0 : dto.getSort());
        facilityMapper.updateById(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        getById(id);
        homestayFacilityMapper.delete(new QueryWrapper<HomestayFacility>().eq("facility_id", id));
        facilityMapper.deleteById(id);
    }

    private void checkNameUnique(String name, Long excludeId) {
        QueryWrapper<Facility> qw = new QueryWrapper<Facility>().eq("name", name);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        if (facilityMapper.selectCount(qw) > 0) {
            throw new BusinessException("设施名称已存在");
        }
    }
}
