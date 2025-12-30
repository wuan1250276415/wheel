package com.basebackend.wheel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basebackend.wheel.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {

    PaymentOrder selectByOrderNo(@Param("orderNo") String orderNo);

    int updatePaymentStatus(@Param("orderNo") String orderNo, @Param("paymentStatus") Integer paymentStatus);
}
