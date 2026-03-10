package app.dissension.api.application.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found: " + id);
    }

    public ResourceNotFoundException(String resource, String identifier) {
        super(resource + " not found: " + identifier);
    }
}
