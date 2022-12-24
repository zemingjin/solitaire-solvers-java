package org.solitaire.model;

public class NoSolutionFoundException extends RuntimeException {
    public NoSolutionFoundException(String path) {
        super("No solution found: " + path);
    }
}
