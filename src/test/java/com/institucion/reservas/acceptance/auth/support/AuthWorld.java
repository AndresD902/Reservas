package com.institucion.reservas.acceptance.auth.support;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Estado compartido entre los pasos de un escenario de autenticación. El
 * alcance de Cucumber garantiza que cada escenario recibe un estado limpio.
 */
public class AuthWorld {

    private MvcResult lastResponse;
    private MockHttpSession session;
    private String csrfHeader;
    private String csrfToken;
    private String adminCorreo;

    public MvcResult getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(MvcResult lastResponse) {
        this.lastResponse = lastResponse;
    }

    public MockHttpSession getSession() {
        return session;
    }

    public void setSession(MockHttpSession session) {
        this.session = session;
    }

    public String getCsrfHeader() {
        return csrfHeader;
    }

    public void setCsrfHeader(String csrfHeader) {
        this.csrfHeader = csrfHeader;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getAdminCorreo() {
        return adminCorreo;
    }

    public void setAdminCorreo(String adminCorreo) {
        this.adminCorreo = adminCorreo;
    }
}
