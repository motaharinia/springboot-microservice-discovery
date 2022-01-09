package com.motaharinia.discoveryserver.config.log.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motaharinia.msutility.custom.customdto.ClientResponseDto;
import com.motaharinia.msutility.custom.customdto.exception.ExceptionDto;
import com.motaharinia.msutility.tools.exception.ExceptionTools;
import com.motaharinia.msutility.tools.string.StringTools;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_ERROR_FILTER_ORDER;

//https://github.com/ykkxdu/spring-cloud-netflix/blob/master/spring-cloud-netflix-core/src/main/java/org/springframework/cloud/netflix/zuul/filters/post/SendErrorFilter.java
@Slf4j
@Component
public class GatewayExceptionTranslator extends ZuulFilter {
    private static final String GATEWAY_EXCEPTION_UNKNOWN_HOST = "GATEWAY_EXCEPTION.UNKNOWN_HOST";
    private static final String GATEWAY_EXCEPTION_UNKNOWN_SERVICE = "GATEWAY_EXCEPTION.UNKNOWN_SERVICE";

    protected static final String SEND_ERROR_FILTER_RAN = "sendErrorFilter.ran";

    /**
     * شییی Environment
     */
    private final Environment environment;
    private final MessageSource messageSource;

    public GatewayExceptionTranslator(Environment environment, MessageSource messageSource) {
        this.environment = environment;
        this.messageSource = messageSource;
    }


    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_ERROR_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        return requestContext.getThrowable() != null && !requestContext.getBoolean(SEND_ERROR_FILTER_RAN, false);
    }

    @Override
    public Object run() {
        try {
            RequestContext requestContext = RequestContext.getCurrentContext();
            if (!ObjectUtils.isEmpty(requestContext.getThrowable())) {
                requestContext.set(SEND_ERROR_FILTER_RAN);
                requestContext.setResponseStatusCode(HttpStatus.BAD_GATEWAY.value());
                requestContext.getResponse().getWriter()
                        .write(new ObjectMapper().writeValueAsString(translate(requestContext)));
                requestContext.getResponse().setContentType("application/json");
            }
        } catch (Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }


    /**
     * متد بررسی خطا و تبدیل آن به خطای Zuul در صورت امکان
     *
     * @param throwable خطای پرتاب شده
     * @return خروجی: خطای ZuulException
     */
    private ZuulException findZuulException(Throwable throwable) {
        if (throwable.getCause() instanceof ZuulRuntimeException) {
            // this was a failure initiated by one of the local filters
            return (ZuulException) throwable.getCause().getCause();
        }

        if (throwable.getCause() instanceof ZuulException) {
            // wrapped zuul exception
            return (ZuulException) throwable.getCause();
        }

        if (throwable instanceof ZuulException) {
            // exception thrown by zuul lifecycle
            return (ZuulException) throwable;
        }

        // fallback, should never get here
        return new ZuulException(throwable, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }


    /**
     * متد ترجمه خطا
     *
     * @param requestContext شیی درخواست Gateway
     * @return خروجی: مدل خطای فرانت
     */
    private ClientResponseDto<String> translate(RequestContext requestContext) {
        //فورس کردن زبان فارسی برای تنظیم ترجمه ها
        LocaleContextHolder.setLocale(new Locale("fa", "IR"));

        ZuulException zuulException = findZuulException(requestContext.getThrowable());
        String serviceId = getService(requestContext);
        log.error("ZuulException serviceId:{} getMessage():{} nStatusCode:{} errorCause:{} getStackTrace():{}", serviceId, zuulException.getMessage(), zuulException.nStatusCode, zuulException.errorCause, Arrays.toString(zuulException.getStackTrace()));

        ClientResponseDto<String> clientResponseDto = new ClientResponseDto<>();
        ExceptionDto exceptionDto = new ExceptionDto(environment.getProperty("spring.application.name"), environment.getProperty("server.port"));
        exceptionDto.setUrl(ExceptionTools.getRequestUrl(requestContext.getRequest()));
        exceptionDto.setIpAddress(ExceptionTools.getRequestIpAddress(requestContext.getRequest()));
        clientResponseDto.setException(exceptionDto);
        if ("Forwarding error".equalsIgnoreCase(zuulException.getMessage())) {
            clientResponseDto.setMessage(StringTools.translateCustomMessage(messageSource, GATEWAY_EXCEPTION_UNKNOWN_SERVICE + "::" + serviceId));
        } else {
            clientResponseDto.setMessage(StringTools.translateCustomMessage(messageSource, GATEWAY_EXCEPTION_UNKNOWN_HOST + "::" + zuulException.getMessage()));
        }
        clientResponseDto.setReturnCode(HttpStatus.BAD_GATEWAY.value());
        return clientResponseDto;
    }


    /**
     * متد استخراج نام سرویس
     *
     * @param requestContext شیی درخواست Gateway
     * @return خروجی: نام سرویس
     */
    private String getService(RequestContext requestContext) {
        Object serviceId = requestContext.get("serviceId");
        if (serviceId != null) {
            //RibbonRoutingFilter
            return (String) serviceId;
        } else {
            //SimpleHostRoutingFilter
            URL host = requestContext.getRouteHost();
            if (host != null) {
                return host.toString();
            } else {
                return "";
            }
        }
    }
}
