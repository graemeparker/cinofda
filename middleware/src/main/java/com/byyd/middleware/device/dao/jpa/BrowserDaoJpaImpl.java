package com.byyd.middleware.device.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.Browser;
import com.byyd.middleware.device.dao.BrowserDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class BrowserDaoJpaImpl extends BusinessKeyDaoJpaImpl<Browser> implements BrowserDao {

}
