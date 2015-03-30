package org.zanata.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class HttpUtilTest {

    @BeforeMethod
    public void init() {
        setHeader("");
    }

    @Test
    public void getClientIdWithNoHeaderTest() {
        String expectedIP = "255.255.255.1";
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn(expectedIP);

        String ip = HttpUtil.getClientIp(mockRequest);
        assertThat(ip).isEqualTo(expectedIP);
        verify(mockRequest).getRemoteAddr();
    }

    @Test
    public void getClientIdWithWithHeaderTest() {
        String proxyHeader = "random-header-from-proxy-server";
        setHeader(proxyHeader);
        String expectedIP = "255.255.255.1";
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getHeader(proxyHeader)).thenReturn(expectedIP);

        String ip = HttpUtil.getClientIp(mockRequest);
        assertThat(ip).isEqualTo(expectedIP);
        verify(mockRequest).getHeader(proxyHeader);
    }

    @Test
    public void getClientIdWithWithHeaderListTest() {
        String proxyHeader = "random-header-from-proxy-server";
        setHeader(proxyHeader);
        String expectedIP = "255.255.255.1,255.255.255.2,255.255.255.3";
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getHeader(proxyHeader)).thenReturn(expectedIP);

        String ip = HttpUtil.getClientIp(mockRequest);
        assertThat(ip).isEqualTo("255.255.255.3");
        verify(mockRequest).getHeader(proxyHeader);
    }

    private void setHeader(String header) {
        System.setProperty("ZANATA_PROXY_HEADER", header);
        HttpUtil.refreshProxyHeader();
    }

    @Test
    public void isReadMethodTest() {
        assertThat(HttpUtil.isReadMethod(HttpMethod.DELETE)).isFalse();
        assertThat(HttpUtil.isReadMethod(HttpMethod.POST)).isFalse();
        assertThat(HttpUtil.isReadMethod(HttpMethod.PUT)).isFalse();

        assertThat(HttpUtil.isReadMethod(HttpMethod.GET)).isTrue();
        assertThat(HttpUtil.isReadMethod(HttpMethod.HEAD)).isTrue();
        assertThat(HttpUtil.isReadMethod(HttpMethod.OPTIONS)).isTrue();
    }
}
