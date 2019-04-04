package com.pss.zippopotamus.service;

import com.pss.zippopotamus.dao.ZippotamusDao;
import com.pss.zippopotamus.domain.ZippopotamusResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ZippotamusServiceImpl implements ZippotamusService {

    @Inject
    ZippotamusDao dao;


    @Override
    @Transactional
    public ZippopotamusResult find(String id){
        return dao.findById(id);
    }

    @Override
    @Transactional
    public ZippopotamusResult persist(ZippopotamusResult result){
        dao.persist(result);
        return result;
    }

}
