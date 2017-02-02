<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
        angular.module('sandManApp').constant('envInfo',
                {
                    "active":          "<%= System.getProperty("envInfo.active") %>",
                    "env":          "<%= System.getProperty("hspcEnv") %>",
                    "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
                    "baseServiceUrl_1": "<%= System.getProperty("baseServiceUrl_1") %>",
                    "baseServiceUrl_2": "<%= System.getProperty("baseServiceUrl_2") %>",
                    "baseServiceUrl_3": "<%= System.getProperty("baseServiceUrl_3") %>",
                    "basePersonaServiceUrl_1": "<%= System.getProperty("basePersonaServiceUrl_1") %>",
                    "basePersonaServiceUrl_2": "<%= System.getProperty("basePersonaServiceUrl_2") %>",
                    "basePersonaServiceUrl_3": "<%= System.getProperty("basePersonaServiceUrl_3") %>",
                    "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
                    "oauthPersonaAuthenticationUrl": "<%= System.getProperty("oauthPersonaAuthenticationUrl") %>",
                    "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>",
                    "sbmUrlHasContextPath": "<%= System.getProperty("sbmUrlHasContextPath") %>",
                    "hostOrg": "<%= System.getProperty("hostOrg") %>"
                });
