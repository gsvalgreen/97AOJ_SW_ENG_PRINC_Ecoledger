package support;

/**
 * Holds per-scenario helpers shared across step definitions.
 */
public final class ScenarioContext {

    private static final ThreadLocal<UserProvisioner> USER_PROVISIONER = new ThreadLocal<>();

    private ScenarioContext() {
    }

    public static void init() {
        USER_PROVISIONER.set(new UserProvisioner());
    }

    public static ProvisionedUser ensureProducer(String alias) {
        return getProvisioner().ensureProducer(alias);
    }

    public static String resolveProducerId(String alias) {
        return ensureProducer(alias).usuarioId();
    }

    public static void cleanup() {
        UserProvisioner provisioner = USER_PROVISIONER.get();
        if (provisioner != null) {
            try {
                provisioner.cleanup();
            } finally {
                USER_PROVISIONER.remove();
            }
        }
    }

    private static UserProvisioner getProvisioner() {
        UserProvisioner provisioner = USER_PROVISIONER.get();
        if (provisioner == null) {
            throw new IllegalStateException("ScenarioContext not initialized");
        }
        return provisioner;
    }
}
