<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
        angular.module('sandManApp').constant('envInfo',
                {
                    "env":          "<%= System.getProperty("hspcEnv") %>",
                    "sandboxUserUri": "<%= System.getProperty("sandboxUserUri") %>",
                    "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
                    "baseServiceUrl": "<%= System.getProperty("baseServiceUrl") %>",
                    "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
                    "oauthAuthenticationUrl": "<%= System.getProperty("oauthAuthenticationUrl") %>",
                    "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>"
                });
