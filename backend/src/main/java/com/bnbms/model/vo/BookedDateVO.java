package com.bnbms.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 已被预订的日期区间（供门户日历禁用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedDateVO {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
