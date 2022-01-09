//package com.motaharinia.discoveryserver.config.gateway;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.google.common.io.CharStreams;
//import com.netflix.config.DynamicIntProperty;
//import com.netflix.config.DynamicPropertyFactory;
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import com.netflix.zuul.exception.ZuulException;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//
//@Slf4j
//public class GatewayResponseFilter extends ZuulFilter {
//
//    @Override
//    public String filterType() {
//        return "post";
//    }
//
//    @Override
//    public int filterOrder() {
//        return 0;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//
//    @Override
//    public Object run() throws ZuulException {
//        RequestContext context = RequestContext.getCurrentContext();
//        HttpServletRequest request = context.getRequest();
//        /*---- implementation strategy changed----*/
//        try (final InputStream responseDataStream = context.getResponseDataStream()) {
//            if (responseDataStream == null) {
//                log.info("BODY: {}", "");
//                return null;
//            }
//            String responseData = CharStreams.toString(new InputStreamReader(responseDataStream, StandardCharsets.UTF_8));
////          JsonNode jsonNode = new ObjectMapper().readTree(responseData);
//            log.info("BODY: {}", responseData);
////                String secondres = "";
////                String code = jsonNode.findValue("code").toPrettyString();
////                if (code.equals("200")) {
////                    String phone = getPhoneNumberFromRequerstBody(context);
////                    String token = authorizationTokenProvider.createJwtTokenForAppUser(phone);
////                    appUserService.loginAppUser(getPhoneNumberFromRequerstBody(context));
////                    ObjectNode objectNode = ((ObjectNode) jsonNode).put("x_token", token);
////                    secondres = objectNode.toString();
////                    context.setResponseBody(secondres);
////                    log.info("BODY: {}", secondres);
////                }else {
////                    context.setResponseBody(responseData);
////                }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new ZuulException(e, INTERNAL_SERVER_ERROR.value(), e.getMessage());
//        }
//        return null;
//    }
//
//}
