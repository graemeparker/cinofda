package com.adfonic.sso;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.adfonic.util.PasswordUtils;

public class ByydJdbcTemplateAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final Logger LOG = Logger.getLogger(ByydJdbcTemplateAuthenticationHandler.class.getName());

    private JdbcTemplate jdbcTemplate;

    protected static final String DEFAULT_PREPARED_STATEMENT = "SELECT ID, STATUS, EMAIL, PASSWORD, SALT FROM ADFONIC_USER WHERE EMAIL=?";
    protected static final String DEFAULT_POSITIVE_USER_STATUS = "ACTIVE";

    private String preparedStatement = DEFAULT_PREPARED_STATEMENT;
    private String positiveUserStatus = DEFAULT_POSITIVE_USER_STATUS;
    
    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credentials) throws GeneralSecurityException, PreventedException{
        Map<String, Object> result = null;
        try{
            result = jdbcTemplate.queryForMap(preparedStatement, credentials.getUsername());
        }catch(EmptyResultDataAccessException erde){
            String errorMessage = "No user found with id = " + credentials.getUsername() + " with preparedStatement " + preparedStatement;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(errorMessage);
            }
            throw new FailedLoginException(errorMessage);
        }

        if (result != null) {
            Long id = (Long) result.get("ID");
            String status = (String) result.get("STATUS");
            String email = (String) result.get("EMAIL");
            String password = (String) result.get("PASSWORD");
            String salt = (String) result.get("SALT");

            if (!positiveUserStatus.equals(status)) {
                String errorMessage = "AdfonicUser id=" + id + " (" + email + ") is not " + positiveUserStatus + " (status=" + status + ")";
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(errorMessage);
                }
                throw new FailedLoginException(errorMessage);
            }else if (!PasswordUtils.checkPassword(credentials.getPassword(), password, salt)) {
                String errorMessage = "Password mismatch for AdfonicUser id=" + id + " (" + email + ")";
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(errorMessage);
                }
                throw new FailedLoginException(errorMessage);
            }else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Successfully authenticated AdfonicUser id=" + id + " (" + email + ")");
                }
                return new DefaultHandlerResult(this, new BasicCredentialMetaData(credentials), this.principalFactory.createPrincipal(credentials.getId()));
            }
        }
        throw new FailedLoginException("Can not authenticate user, invalid authentication termination.");
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @return the preparedStatement
     */
    public String getPreparedStatement() {
        return preparedStatement;
    }

    /**
     * @param preparedStatement the preparedStatement to set
     */
    public void setPreparedStatement(String preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    /**
     * @return the positiveUserStatus
     */
    public String getPositiveUserStatus() {
        return positiveUserStatus;
    }

    /**
     * @param positiveUserStatus the positiveUserStatus to set
     */
    public void setPositiveUserStatus(String positiveUserStatus) {
        this.positiveUserStatus = positiveUserStatus;
    }

}
