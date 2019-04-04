package com.pss.zippopotamus.service;

import com.pss.zippopotamus.domain.ZippopotamusResult;

import javax.transaction.Transactional;

public interface ZippotamusService {
    @Transactional
    ZippopotamusResult find(String id);

    @Transactional
    ZippopotamusResult persist(ZippopotamusResult result);
}
