package se.inera.certificate.integration.util;

import iso.v21090.dt.v1.II;

public class InvalidIdException extends RuntimeException {

    private static final long serialVersionUID = -4295743153196733900L;

    public InvalidIdException(String id) {
        super(String.format("Invalid id: %s", id));
    }

    public InvalidIdException(II id) {
        this(id.getRoot(), id.getExtension());
    }

    public InvalidIdException(String root, String extension) {
        super(String.format("Invalid id: [root: %s, extension: %s]", root, extension));
    }
}
