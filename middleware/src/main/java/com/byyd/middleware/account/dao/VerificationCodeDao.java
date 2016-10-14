package com.byyd.middleware.account.dao;

import java.util.List;

import com.adfonic.domain.User;
import com.adfonic.domain.VerificationCode;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface VerificationCodeDao extends BusinessKeyDao<VerificationCode> {

    Long countAllForUser(User user);
    List<VerificationCode> getAllForUser(User user, FetchStrategy... fetchStrategy);
    List<VerificationCode> getAllForUser(User user, Sorting sort, FetchStrategy... fetchStrategy);
    List<VerificationCode> getAllForUser(User user, Pagination page, FetchStrategy... fetchStrategy);

    VerificationCode getForCodeTypeAndCodeValue(VerificationCode.CodeType codeType, String codeValue, FetchStrategy... fetchStrategy);
    VerificationCode getForCodeValue(String codeValue, FetchStrategy... fetchStrategy);
}
