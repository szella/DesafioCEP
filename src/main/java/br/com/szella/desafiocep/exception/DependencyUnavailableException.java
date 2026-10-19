package br.com.szella.desafiocep.exception;

import org.springframework.http.HttpStatus;

public class DependencyUnavailableException extends RuntimeException {

    private final HttpStatus status;

    public DependencyUnavailableException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
