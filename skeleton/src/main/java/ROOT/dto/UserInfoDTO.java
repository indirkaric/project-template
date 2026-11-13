package {{rootPackage}}.dto;

/**
 * Payload of the {@code Sqa-user-id} header, before base64 encoding.
 *
 * @param userId user id
 */
public record UserInfoDTO(String userId) {
}
