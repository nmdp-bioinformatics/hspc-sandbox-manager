<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
        angular.module('sandManApp').constant('envInfo',
                {
                    "env":          "<%= System.getProperty("hspcEnv") %>",
                    "profileUpdateUri": "<%= System.getProperty("profileUpdateUri") %>",
                    "defaultServiceUrl": "<%= System.getProperty("defaultServiceUrl") %>",
                    "baseServiceUrl": "<%= System.getProperty("baseServiceUrl") %>",
                    "oauthLogoutUrl": "<%= System.getProperty("oauthLogoutUrl") %>",
                    "oauthLogoutSuccessUrl": "<%= System.getProperty("oauthLogoutSuccessUrl") %>",
                    "userManagementUrl": "<%= System.getProperty("userManagementUrl") %>"
                });
