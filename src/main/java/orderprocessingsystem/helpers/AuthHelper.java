package orderprocessingsystem.helpers;

public class AuthHelper {
    public static void authorizeRequest(final String accessToken) {
        if (!accessToken.endsWith("OP")) {
            throw new UnsupportedOperationException("Unauthorized request!!");
        }

    }
}
