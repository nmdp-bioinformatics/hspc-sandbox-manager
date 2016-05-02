package org.hspconsortium.sandboxmanager.controllers;

public class UnauthorizedException extends RuntimeException {

    UnauthorizedException(String message) {
        super(message);
    }
}
