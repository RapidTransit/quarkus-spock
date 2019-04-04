package com.pss.zippopotamus.dao;

import com.pss.zippopotamus.domain.ZippopotamusResult;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ZippotamusDao implements PanacheRepositoryBase<ZippopotamusResult, String> {


}
