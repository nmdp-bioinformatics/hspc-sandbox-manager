<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
        angular.module('sandManApp').constant('envInfo',
                {
                    "env":          "<%= System.getProperty("hspcEnv") %>",
                    "sandboxUserUri": "<%= System.getProperty("sandboxUserUri") %>",
                    "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
                    "baseServiceUrl_1": "<%= System.getProperty("baseServiceUrl_1") %>",
                    "baseServiceUrl_2": "<%= System.getProperty("baseServiceUrl_2") %>",
                    "basePersonaServiceUrl_1": "<%= System.getProperty("basePersonaServiceUrl_1") %>",
                    "basePersonaServiceUrl_2": "<%= System.getProperty("basePersonaServiceUrl_2") %>",
                    "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
                    "oauthAuthenticationUrl": "<%= System.getProperty("oauthAuthenticationUrl") %>",
                    "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>"
                });
