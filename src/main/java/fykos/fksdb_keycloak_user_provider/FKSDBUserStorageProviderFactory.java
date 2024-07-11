package fykos.fksdb_keycloak_user_provider;

import java.util.List;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

public class FKSDBUserStorageProviderFactory
		implements UserStorageProviderFactory<FKSDBUserStorageProvider> {

	public static final String PROVIDER_NAME = "FKSDB";

	private static final String DBURL_PROPERTY = "dburl";
	private static final String USERNAME_PROPERTY = "username";
	private static final String PASSWORD_PROPERTY = "password";

	protected static final List<ProviderConfigProperty> configMetadata;

	static {
		configMetadata = ProviderConfigurationBuilder.create()
				.property().name(DBURL_PROPERTY).type(ProviderConfigProperty.STRING_TYPE).label("API route").add()
				.property().name(USERNAME_PROPERTY).type(ProviderConfigProperty.STRING_TYPE).label("Username").add()
				.property().name(PASSWORD_PROPERTY).type(ProviderConfigProperty.PASSWORD).label("Password").add()
				.build();
	}

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}

	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProviderFactory.class);
	protected Properties properties = new Properties();

	@Override
	public void init(Config.Scope config) {

		logger.info("FKSDB storage provider created");
	}

	@Override
	public FKSDBUserStorageProvider create(KeycloakSession session, ComponentModel model) {

		String dbUrl = model.getConfig().getFirst(DBURL_PROPERTY);
		String username = model.getConfig().getFirst(USERNAME_PROPERTY);
		String password = model.getConfig().getFirst(PASSWORD_PROPERTY);

		FKSDBUserService userService = new FKSDBUserService(dbUrl, username, password);

		return new FKSDBUserStorageProvider(session, model);
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configMetadata;
	}

	@Override
	public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
			throws ComponentValidationException {
		String dbUrl = config.getConfig().getFirst(DBURL_PROPERTY);
		String username = config.getConfig().getFirst(USERNAME_PROPERTY);
		String password = config.getConfig().getFirst(PASSWORD_PROPERTY);

		if (dbUrl == null || dbUrl.isEmpty()) {
			logger.error("Database URL not defined");
			throw new ComponentValidationException("Database URL not defined");
		}
		if (username == null || username.isEmpty()) {
			logger.error("Username not defined");
			throw new ComponentValidationException("Username not defined");
		}
		if (password == null || password.isEmpty()) {
			logger.error("Password not defined");
			throw new ComponentValidationException("Password not defined");
		}
	}
}
