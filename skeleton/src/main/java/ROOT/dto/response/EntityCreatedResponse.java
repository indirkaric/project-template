package {{rootPackage}}.dto.response;

/**
 * Body of every 201.
 *
 * @param id entity id of the created entity
 */
public record EntityCreatedResponse(String id) {
}
