
package com.fesskiev.mediacenter.utils.billing;

/**
 * Exception thrown when encountering an invalid Base64 input character.
 *
 * @author nelson
 */
final class Base64DecoderException extends Exception {
    private static final long serialVersionUID = 1L;

    public Base64DecoderException() {
        super();
    }

    public Base64DecoderException(String s) {
        super(s);
    }
}
