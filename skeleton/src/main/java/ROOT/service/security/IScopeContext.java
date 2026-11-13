package {{rootPackage}}.service.security;

/**
 * Who is calling, and the correlation id of the current request.
 */
public interface IScopeContext {

    /**
     * @return email of the JWT user, the caller id of a service call, or the app name without authentication
     */
    String getCurrentUser();

    /**
     * @return user id of the JWT user, the caller id of a service call, or the app name without authentication
     */
    String getCurrentUserId();

    /**
     * @param correlationId value of {@code Sqa-request-id}
     */
    void setCorrelationId(String correlationId);

    /**
     * @return correlation id of the current request, or null
     */
    String getCorrelationId();

    /**
     * Removes the correlation id from the current thread.
     */
    void clearCorrelationId();

    /**
     * @param userId end-user id received in {@code Sqa-user-id}
     */
    void setSqaUserId(String userId);

    /**
     * @return end-user id received in {@code Sqa-user-id}, or null
     */
    String getSqaRequestUserId();

    /**
     * Removes the end-user id from the current thread.
     */
    void clearSqaUserId();

    /**
     * @return value for an outbound {@code Sqa-user-id} header
     */
    String getBase64EncodedUserInfo();
}
