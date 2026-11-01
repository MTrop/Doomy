package net.mtrop.doomy.managers;

import com.blackrook.sql.SQLConnection;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Execution plan manager singleton.
 * @author Matthew Tropiano
 */
public class ExecutionPlanManager 
{
	// ============================== QUERIES ================================

	private static final String QUERY_GET_BY_ENGINE_ID
		= "SELECT * FROM ExecutionPlans WHERE engineId = ?"; 
	private static final String QUERY_NEW
		= "INSERT INTO ExecutionPlans (engineId, planTypeId, clientName) VALUES (?,0,'')"; 
	private static final String QUERY_UPDATE
		= "UPDATE ExecutionPlans SET " +
			"planTypeId = ?, " + 
			"workDir = ?, " +
			"packageName = ?, "+
			"clientName = ?, " + 
			"setupName = ?, " +
			"serverName = ?, " +
			"commandLine = ? " +
		"WHERE engineId = ?";
	
	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<ExecutionPlanManager> INSTANCE = new SingletonProvider<>(() -> new ExecutionPlanManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static ExecutionPlanManager get()
	{
		return INSTANCE.get();
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;

	private ExecutionPlanManager()
	{
		this.connection = DatabaseManager.get().getConnection();
	}

	/**
	 * Gets an execution plan by its engine id.
	 * @param engineId the engine id.
	 * @return the corresponding execution plan, or null if no corresponding plan.
	 */
	public ExecutionPlan getByEngineId(int engineId)
	{
		return connection.getRow(ExecutionPlan.class, QUERY_GET_BY_ENGINE_ID, engineId);
	}
	
	/**
	 * Creates a new execution plan stub for an engine.
	 * @param engineId the engine id.
	 * @return the new plan with fields initialized, or null if it couldn't be created.
	 */
	public ExecutionPlan createNew(int engineId)
	{
		if (connection.getUpdateResult(QUERY_NEW, engineId).getRowCount() > 0)
			return getByEngineId(engineId);
		else
			return null;
	}
	
	/**
	 * Updates an execution plan.
	 * @param plan the the plan to commit.
	 * @return true if saved, false if not.
	 */
	public boolean updatePlan(ExecutionPlan plan)
	{
		return connection.getUpdateResult(QUERY_UPDATE,
			plan.planTypeId.ordinal(),
			plan.workDir,
			plan.packageName,
			plan.clientName,
			plan.setupName,
			plan.serverName,
			plan.commandLine,
			plan.engineId
		).getRowCount() > 0;
	}
	
	/**
	 * A single plan entry.
	 */
	public static class ExecutionPlan
	{
		public int engineId;
		public PlanType planTypeId;
		public String workDir;
		public String packageName;
		public String clientName;
		public String setupName;
		public String serverName;
		public String commandLine;
	}
	
	public enum PlanType
	{
		NORMAL,
		DOSBOX,
		FLATPAK,
		APPIMAGE,
		;
	}
	
}
