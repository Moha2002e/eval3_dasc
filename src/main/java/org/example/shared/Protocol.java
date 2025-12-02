package org.example.shared;

/**
 * Protocol constants for Medical Reports Protocol Secure (MRPS)
 */
public class Protocol {
    // Commands
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_ADD_REPORT = "ADD_REPORT";
    public static final String CMD_EDIT_REPORT = "EDIT_REPORT";
    public static final String CMD_LIST_REPORTS = "LIST_REPORTS";
    public static final String CMD_LIST_PATIENTS = "LIST_PATIENTS";
    public static final String CMD_LOGOUT = "LOGOUT";

    // Responses
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_SALT = "SALT";
    public static final String RESP_DIGEST = "DIGEST";

    // Delimiters
    public static final String DELIMITER = "|";

    // Crypto algorithms
    public static final String SYMMETRIC_ALGORITHM = "AES";
    public static final String ASYMMETRIC_ALGORITHM = "RSA";
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String HMAC_ALGORITHM = "HmacSHA256";

    // Key sizes
    public static final int AES_KEY_SIZE = 256;
    public static final int RSA_KEY_SIZE = 2048;
    public static final int SALT_SIZE = 16;
}
