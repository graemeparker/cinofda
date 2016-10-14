package com.byyd.middleware.common.dao.jpa;

import org.springframework.stereotype.Repository;

import com.adfonic.domain.UploadedContent;
import com.byyd.middleware.common.dao.UploadedContentDao;
import com.byyd.middleware.iface.dao.jpa.BusinessKeyDaoJpaImpl;

@Repository
public class UploadedContentDaoJpaImpl extends BusinessKeyDaoJpaImpl<UploadedContent> implements UploadedContentDao {

}
