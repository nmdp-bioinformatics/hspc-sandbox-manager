<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
    angular.module('sandManApp').constant('envInfo',
        {
            "active": "<%= System.getProperty("envInfo.active") %>",
            "env": "<%= System.getProperty("hspcEnv") %>",
            "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
            "baseServiceUrl_1": "<%= System.getProperty("baseServiceUrl_1") %>",
            "baseServiceUrl_2": "<%= System.getProperty("baseServiceUrl_2") %>",
            "baseServiceUrl_3": "<%= System.getProperty("baseServiceUrl_3") %>",
            "baseServiceUrl_4": "<%= System.getProperty("baseServiceUrl_4") %>",
            "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
            "oauthUserInfoUrl": "<%= System.getProperty("oauthUserInfoUrl") %>",
            "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>",
            "sbmUrlHasContextPath": "<%= System.getProperty("sbmUrlHasContextPath") %>",
            "hostOrg": "<%= System.getProperty("hostOrg") %>",
            "hspcAccountCookieName": "<%= System.getProperty("hspcAccountCookieName") %>",
            "personaCookieName": "<%= System.getProperty("personaCookieName") %>",
            "personaCookieDomain": "<%= System.getProperty("personaCookieDomain") %>",
            "sandboxManagerApiUrl": "<%= System.getProperty("sandboxManagerApiUrl") %>"
        });
