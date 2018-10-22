package de.hpi.bpt.scylla.plugin.batch;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.SimulationTest;
import de.hpi.bpt.scylla.TestUtils;

public class BatchSimulationTest extends SimulationTest{
	

	protected BatchClusterExecutionType executionType;
	protected List<String[]> table;

	@Override
	protected String getFolderName() {return "BatchPlugin";}
	
	@Override
	protected void afterParsing() {
		super.afterParsing();
		if(executionType != null)changeBatchType();
	}
	
	@Override
	protected void runSimpleSimulation(String globalConfiguration, String simulationModel, String simulationConfiguration) {
		super.runSimpleSimulation(globalConfiguration, simulationModel, simulationConfiguration);
		parseTable();
	}
	
	protected void changeBatchType() {
		BatchActivity batchActivity = getBatchActivity();
		assert batchActivity != null;
		assert executionType != null;
		try {
			Field privateStringField = BatchActivity.class.getDeclaredField("executionType");
			privateStringField.setAccessible(true);
			privateStringField.set(batchActivity, executionType);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		assert batchActivity.getExecutionType() == executionType;
	}
	
	protected void parseTable() {
		File f = new File(".\\"+outputPath+getProcessId()+"_processBatchActivityStats.csv");
		assertTrue(f.exists());
		table = TestUtils.readCSV(f);
	}
	
	protected void assertExecutionType() {
		table.stream().forEach((each)->{assertEquals(executionType.toString(), each[7]);});
	}
	
	protected Map<String, List<String[]>> getClusters() {
		return TestUtils.groupBy(table,6);
	}
	
	protected Map<String, List<String[]>> getProcessInstances() {
		return TestUtils.groupBy(table,0);
	}
	
	protected BatchActivity getBatchActivity() {
		return BatchPluginUtils.getBatchActivities(getProcessModel()).values().iterator().next();
	}
	
	protected static final void assertActivitiesDoNotIntersect(String activityNameA, String activityNameB, List<String[]> cluster) {
		Map<Object, List<String[]>> e = cluster.stream()
				.collect(Collectors.groupingBy((row)->{return row[1];}));
			String[] lastActivityA = e.get(activityNameA).stream().max((a,b) -> {return a[4].compareTo(b[4]);}).get();
			String[] firstActivityB = e.get(activityNameB).stream().min((a,b) -> {return a[3].compareTo(b[3]);}).get();
			assertTrue(lastActivityA[4].compareTo(firstActivityB[3]) <= 0);
	}
	

}
