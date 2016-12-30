<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
        angular.module('sandManApp').constant('envInfo',
                {
                    "active":          "<%= System.getProperty("envInfo.active") %>",
                    "env":          "<%= System.getProperty("hspcEnv") %>",
                    "sandboxUserUri": "<%= System.getProperty("sandboxUserUri") %>",
                    "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
                    "baseServiceUrl_1": "<%= System.getProperty("baseServiceUrl_1") %>",
                    "baseServiceUrl_2": "<%= System.getProperty("baseServiceUrl_2") %>",
                    "basePersonaServiceUrl_1": "<%= System.getProperty("basePersonaServiceUrl_1") %>",
                    "basePersonaServiceUrl_2": "<%= System.getProperty("basePersonaServiceUrl_2") %>",
                    "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
                    "oauthPersonaAuthenticationUrl_1": "<%= System.getProperty("oauthPersonaAuthenticationUrl_1") %>",
                    "oauthPersonaAuthenticationUrl_2": "<%= System.getProperty("oauthPersonaAuthenticationUrl_2") %>",
                    "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>",
                    "hostOrg": "<%= System.getProperty("hostOrg") %>"
                });
