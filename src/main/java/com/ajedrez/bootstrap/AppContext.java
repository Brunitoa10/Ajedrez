package com.ajedrez.bootstrap;

import com.ajedrez.ai.EngineFactory;
import com.ajedrez.ai.PieceSquareEvaluator;
import com.ajedrez.application.GameHistoryService;
import com.ajedrez.application.GameService;
import com.ajedrez.application.LearningService;
import com.ajedrez.application.StatisticsService;
import com.ajedrez.application.port.GameRepository;
import com.ajedrez.application.port.ProgressRepository;
import com.ajedrez.domain.learning.BuiltInLearningCatalog;
import com.ajedrez.domain.learning.LearningCatalog;
import com.ajedrez.domain.rules.ChessRules;
import com.ajedrez.infrastructure.config.AppPaths;
import com.ajedrez.infrastructure.config.DatabaseConfig;
import com.ajedrez.infrastructure.config.DatabaseType;
import com.ajedrez.infrastructure.persistence.ConnectionFactories;
import com.ajedrez.infrastructure.persistence.ConnectionFactory;
import com.ajedrez.infrastructure.persistence.JdbcGameRepository;
import com.ajedrez.infrastructure.persistence.JdbcProgressRepository;
import com.ajedrez.infrastructure.persistence.PersistenceException;
import com.ajedrez.infrastructure.persistence.SchemaInitializer;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Composition Root: el ÚNICO lugar donde se crean y conectan las implementaciones concretas
 * (inyección de dependencias manual, sin singletons globales). El resto del código recibe
 * sus dependencias por constructor.
 */
public final class AppContext implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(AppContext.class.getName());

    private final AppPaths paths;
    private final DatabaseConfig databaseConfig;
    private final String startupWarning;
    private final GameService gameService;
    private final GameHistoryService historyService;
    private final LearningService learningService;
    private final StatisticsService statisticsService;

    private AppContext(AppPaths paths, DatabaseConfig databaseConfig, ConnectionFactory connections,
                       String startupWarning) {
        this.paths = paths;
        this.databaseConfig = databaseConfig;
        this.startupWarning = startupWarning;

        ChessRules rules = ChessRules.standard();
        LearningCatalog catalog = new BuiltInLearningCatalog();
        GameRepository gameRepository = new JdbcGameRepository(connections);
        ProgressRepository progressRepository = new JdbcProgressRepository(connections);
        EngineFactory engineFactory = new EngineFactory(rules, new PieceSquareEvaluator());

        this.gameService = new GameService(rules, engineFactory, gameRepository);
        this.historyService = new GameHistoryService(gameRepository, rules);
        this.learningService = new LearningService(catalog, progressRepository, rules);
        this.statisticsService = new StatisticsService(gameRepository, progressRepository, catalog);
    }

    /**
     * Arranca con la base configurada. Si es PostgreSQL y no responde, cae a SQLite
     * para que la aplicación siempre pueda abrirse (modo portable / sin conexión).
     */
    public static AppContext start(AppPaths paths) {
        DatabaseConfig config = DatabaseConfig.load(paths);
        try {
            return new AppContext(paths, config, initialize(config), null);
        } catch (PersistenceException e) {
            if (config.type() == DatabaseType.SQLITE) {
                throw e;
            }
            LOG.log(Level.WARNING, "No se pudo usar PostgreSQL, se usa SQLite", e);
            DatabaseConfig fallback = DatabaseConfig.defaultSqlite(paths);
            String warning = "No se pudo conectar a PostgreSQL (" + rootMessage(e) + ").\n"
                    + "Se está usando la base local SQLite: " + fallback.describe();
            return new AppContext(paths, fallback, initialize(fallback), warning);
        }
    }

    private static ConnectionFactory initialize(DatabaseConfig config) {
        ConnectionFactory connections = ConnectionFactories.create(config);
        new SchemaInitializer(connections).initialize();
        return connections;
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage();
    }

    public AppPaths paths() {
        return paths;
    }

    public DatabaseConfig databaseConfig() {
        return databaseConfig;
    }

    public Optional<String> startupWarning() {
        return Optional.ofNullable(startupWarning);
    }

    public GameService gameService() {
        return gameService;
    }

    public GameHistoryService historyService() {
        return historyService;
    }

    public LearningService learningService() {
        return learningService;
    }

    public StatisticsService statisticsService() {
        return statisticsService;
    }

    @Override
    public void close() {
        gameService.close();
    }
}
