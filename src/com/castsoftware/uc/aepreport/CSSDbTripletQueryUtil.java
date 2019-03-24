package com.castsoftware.uc.aepreport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CSSDbTripletQueryUtil {

	private final static String CSSDriver = "org.postgresql.Driver";
	
	
	/**
	 * Set search path (required for CSS2)
	 */	
	private final static String SQL_SET_SEARCHPATH = "set search_path to '%1'";
	
	/**
	 * Extract the list of valid snapshots
	 */
	private final static String SQL_LIST_SNAPSHOTS = 
			"select appo.object_name as application_name, "+
			"sn.snapshot_id, " +
			"sn.application_id, " +
			"SI.object_version, " +
			"sn.functional_date " +
			"from %1.dss_snapshots sn "+
			"join %1.dss_objects appo on sn.application_id = appo.object_id and appo.object_type_id = -102 "+
			"join %1.dss_snapshot_info SI ON sn.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = sn.application_id " +
			"where 1=1 and SNAPSHOT_STATUS = 2 "+
			"order by appo.object_name, sn.snapshot_id desc";
	
	/*
	 * AETP details for one snapshot 
	 * %1 central schema name
	 * %2 snapshot id list
	 * */
	private final static String SQL_AETP_DETAILS = 

		"SELECT O.OBJECT_FULL_NAME," +
		 "object_type_name, "+ 		
		" (A.EC * DC.METRIC_NUM_VALUE) FP," +
		" A.EC as effort_complexity,"  +
		"DC.METRIC_NUM_VALUE as equivalent_ratio,"+
		" A.STATUS,"  +
		" appo.object_name as application_name, "+
		" SI.OBJECT_VERSION, "+
		" S.SNAPSHOT_ID "+
		" FROM %1.AEP_TECHNICAL_ARTIFACTS_VW A "+
		" JOIN %1.DSS_OBJECTS O ON A.OBJECT_ID = O.OBJECT_ID" +
		" JOIN %1.DSS_METRIC_RESULTS DC ON A.SNAPSHOT_ID = DC.SNAPSHOT_ID AND DC.METRIC_ID = 10359 " +
		" JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = A.SNAPSHOT_ID AND  S.SNAPSHOT_STATUS = 2 AND S.ENHANCEMENT_MEASURE = 'AEP' " +
		" JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
		" JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id " +
		" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
		" WHERE A.STATUS <> 'UNCHANGED' " +
		" AND DC.METRIC_VALUE_INDEX = 1" +
		" AND S.SNAPSHOT_ID IN (%2)"+ 
		" ORDER BY A.STATUS DESC, O.OBJECT_FULL_NAME";
	
	/*
	 * AEFP details for one snapshot 
	 * %1 central schema name
	 * %2 ssnapshot id list
	 * */
	private final static String SQL_AEFP_DETAILS = 
	"SELECT O.OBJECT_FULL_NAME ,"+
	"object_type_name, "+ 		
	 "R.METRIC_NUM_VALUE as FP,"+
	 "(SELECT TR.IMPACT_FACTOR "+
	 "FROM %1.EFP_TRAN_INFO TR "+
	 "WHERE TR.OBJECT_ID = R.OBJECT_ID "+
	 "AND TR.SNAPSHOT_ID = R.SNAPSHOT_ID) as impact_factor,"+
	 "CASE R.METRIC_ID "+
	 "   WHEN 10402 THEN 'Added Transactional Function' "+
	 "   WHEN 10412 THEN 'Deleted Transactional Function' "+
	 "   WHEN 10422 THEN 'Modified Transactional Function' "+
	 "   ELSE NULL " +
	 "END as type, "+
	 "appo.object_name as application_name, "+	 
	 "SI.OBJECT_VERSION, "+
	 "S.SNAPSHOT_ID, "+
	 "S.functional_date "+	 
	 "FROM %1.DSS_OBJECTS O "+
	 "JOIN %1.DSS_METRIC_RESULTS R ON R.OBJECT_ID = O.OBJECT_ID AND O.OBJECT_TYPE_ID <> (-102) AND O.OBJECT_TYPE_ID IN ( 30001 ,30002) "+
	 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = R.SNAPSHOT_ID " +
	 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+
	" JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
	" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+	 
	 "WHERE R.METRIC_ID  IN (10402, 10412,10422) " +
	 " and S.SNAPSHOT_ID IN (%2) "+
	 " UNION ALL "+
	 "SELECT O.OBJECT_FULL_NAME, "+
	  "object_type_name, "+ 		
	 "R.METRIC_NUM_VALUE, "+
	 "(SELECT DF.IMPACT_FACTOR "+
	 "FROM %1.EFP_DF_INFO DF "+
	 "WHERE DF.OBJECT_ID = R.OBJECT_ID " +
	 "AND DF.SNAPSHOT_ID = R.SNAPSHOT_ID),"+
	 "CASE R.METRIC_ID" +
	 "   WHEN 10401 THEN 'Added Data Function' "+
	 "   WHEN 10411 THEN 'Deleted Data Function' "+
	 "   WHEN 10421 THEN 'Modified Data Function' "+
	 "   ELSE NULL "+
	 "END, "+
	 "appo.object_name, "+	 
	 "SI.OBJECT_VERSION, "+
	 "S.SNAPSHOT_ID, "+
	 "S.functional_date "+	 
	 "FROM %1.DSS_OBJECTS O "+
	 "JOIN %1.DSS_METRIC_RESULTS R ON R.OBJECT_ID = O.OBJECT_ID AND O.OBJECT_TYPE_ID <> (-102) AND O.OBJECT_TYPE_ID IN ( 30001 ,30002) "+
	 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = R.SNAPSHOT_ID "+
	 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+ 	
     " JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
	 " JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+	 
	 "WHERE R.METRIC_ID  IN (10401, 10411,10421) " +
	 " and S.SNAPSHOT_ID IN (%2) ";	 
	
	/*
	 * Added AEP details
	 * %1 central schema name
	 * %2 snapshot id list
	 * */	
	private final static String SQL_AEP_ADDED_DETAILS = 
			 "SELECT O.OBJECT_FULL_NAME,"+
		     "ot.object_type_name, "+	
			 "(A.EC * DC.METRIC_NUM_VALUE) as FP,"+
			 "'Technical Function' as type,"+
			 "appo.object_name as application_name, "+	 
			 "SI.OBJECT_VERSION, "+
			 "S.SNAPSHOT_ID," +
			 "S.functional_date "+	 
			 "FROM %1.AEP_TECHNICAL_ARTIFACTS_VW A "+
			 "JOIN %1.DSS_OBJECTS O ON A.OBJECT_ID = O.OBJECT_ID "+
			 "JOIN %1.DSS_METRIC_RESULTS DC ON  A.SNAPSHOT_ID = DC.SNAPSHOT_ID AND DC.METRIC_ID = 10359 "+
			 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = A.SNAPSHOT_ID AND  S.SNAPSHOT_STATUS = 2 AND S.ENHANCEMENT_MEASURE = 'AEP' "+
			 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+
			" JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
			" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
			 "WHERE A.STATUS = 'ADDED' "+
			 " AND DC.METRIC_VALUE_INDEX = 1 "+
			 " and S.SNAPSHOT_ID IN (%2) "+			 
			 " UNION ALL "+
			 "SELECT "+
			 "O.OBJECT_FULL_NAME ,"+
			  "ot.object_type_name, "+ 		
			 "R.METRIC_NUM_VALUE ,"+
			 "CASE R.METRIC_ID "+
			 "   WHEN 10402 THEN 'Transactional Function' "+
			 "   WHEN 10401 THEN 'Data Function' "+
			 "   ELSE NULL "+
			 "END,"+
			 "appo.object_name, "+	 
			 "SI.OBJECT_VERSION, "+
			 "S.SNAPSHOT_ID," +
			 "S.functional_date "+	 
			 "FROM "+
			 "%1.DSS_OBJECTS O "+
			 "JOIN %1.DSS_METRIC_RESULTS R ON R.OBJECT_ID = O.OBJECT_ID AND O.OBJECT_TYPE_ID <> (-102) AND O.OBJECT_TYPE_ID IN ( 30001 ,30002) "+
			 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = R.SNAPSHOT_ID "+
			 "JOIN %1.DSS_SNAPSHOT_INFO SI ON SI.SNAPSHOT_ID = S.SNAPSHOT_ID AND SI.object_id = S.application_id "+	 
			" JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
			" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
			 "WHERE R.METRIC_ID = ANY (ARRAY[10402, 10401]) " +
			 " and S.SNAPSHOT_ID IN (%2) ";
	
	/*
	 * Modifier AEP details 
	 * %1 central schema name
	 * %2 snapshot id list
	 * */	

	private final static String SQL_AEP_MODIFIED_DETAILS = 
			 "SELECT O.OBJECT_FULL_NAME, "+
					  "ot.object_type_name, "+	
					 "(A.EC * DC.METRIC_NUM_VALUE) as FP, "+
					 "'Technical Function' as type, "+
					 "appo.object_name as application_name, "+	 
					 "SI.OBJECT_VERSION, "+	 	 
					 "S.SNAPSHOT_ID, "+
					 "S.functional_date "+	 
					 "FROM %1.AEP_TECHNICAL_ARTIFACTS_VW A "+
					 "JOIN %1.DSS_OBJECTS O ON A.OBJECT_ID = O.OBJECT_ID "+
					 "JOIN %1.DSS_METRIC_RESULTS DC ON  A.SNAPSHOT_ID = DC.SNAPSHOT_ID AND DC.METRIC_ID = 10359 "+
					 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = A.SNAPSHOT_ID "+
					 " JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
					 "AND  S.SNAPSHOT_STATUS = 2 "+
					 "AND S.ENHANCEMENT_MEASURE = 'AEP' "+
					 " JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+	 
					" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
					 "WHERE A.STATUS = 'UPDATED' "+
					 " AND DC.METRIC_VALUE_INDEX = 1 "+
					 " and S.SNAPSHOT_ID IN (%2) "+					 
					 " UNION ALL "+
					 " SELECT "+
					 "O.OBJECT_FULL_NAME , "+
					  "ot.object_type_name, "+					 
					 "R.METRIC_NUM_VALUE , "+
					 "CASE R.METRIC_ID "+
					 "   WHEN 10422 THEN 'Transactional Function' "+
					 "   WHEN 10421 THEN 'Data Function' "+
					 "   ELSE NULL "+
					 "END, "+
					 "appo.object_name, "+	 
					 "SI.OBJECT_VERSION,"+
					 "S.SNAPSHOT_ID, "+
					 "S.functional_date "+	 
					 "FROM %1.DSS_OBJECTS O "+
					 "JOIN %1.DSS_METRIC_RESULTS R ON R.OBJECT_ID = O.OBJECT_ID AND O.OBJECT_TYPE_ID <> (-102) AND O.OBJECT_TYPE_ID IN ( 30001 ,30002) "+
					 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = R.SNAPSHOT_ID "+
					 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+	 
  					 " JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
  					" JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
					 "WHERE R.METRIC_ID = ANY (ARRAY[10421, 10422]) " +
					 " and S.SNAPSHOT_ID IN (%2) ";
	
	/*
	 * Deleted AEP details 
	 * %1 central schema name
	 * %2 snapshot id list
	 * */	

	private final static String SQL_AEP_DELETED_DETAILS = 
	 "SELECT O.OBJECT_FULL_NAME, "+
     "ot.object_type_name, "+	
	 "(A.EC * DC.METRIC_NUM_VALUE) as FP, "+
	 "'Technical Function' as type, "+
	 "appo.object_name as application_name, "+	 
	 " SI.OBJECT_VERSION,"+
	 "S.SNAPSHOT_ID, "+
	 "S.functional_date "+	 
	 "FROM %1.AEP_TECHNICAL_ARTIFACTS_VW A " +
	 "JOIN %1.DSS_OBJECTS O ON A.OBJECT_ID = O.OBJECT_ID "+
	 "JOIN %1.DSS_METRIC_RESULTS DC ON  A.SNAPSHOT_ID = DC.SNAPSHOT_ID AND DC.METRIC_ID = 10359 "+
	 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = A.SNAPSHOT_ID AND  S.SNAPSHOT_STATUS = 2 AND S.ENHANCEMENT_MEASURE = 'AEP' "+
	 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+	 
     " JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
	  " JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
	 "WHERE A.STATUS = 'DELETED' AND DC.METRIC_VALUE_INDEX = 1 "+
	 " and S.SNAPSHOT_ID IN (%2) "+	  
	 " UNION ALL "+
	 " SELECT "+
	 "O.OBJECT_FULL_NAME , "+
     "ot.object_type_name, "+		 
	 "R.METRIC_NUM_VALUE , "+
	 "CASE R.METRIC_ID "+
	 "   WHEN 10412 THEN 'Transactional Function' "+
	 "   WHEN 10411 THEN 'Data Function' "+
	 "   ELSE NULL "+
	 "END, "+
	 "appo.object_name, "+	 
	 "SI.OBJECT_VERSION,"+
	 "S.SNAPSHOT_ID, "+
	 "S.functional_date "+	 	 
	 "FROM "+
	 "%1.DSS_OBJECTS O "+
	 "JOIN %1.DSS_METRIC_RESULTS R ON R.OBJECT_ID = O.OBJECT_ID AND O.OBJECT_TYPE_ID <> (-102) AND O.OBJECT_TYPE_ID IN ( 30001 ,30002) "+
	 "JOIN %1.DSS_SNAPSHOTS S ON S.SNAPSHOT_ID = R.SNAPSHOT_ID "+
	 "JOIN %1.DSS_SNAPSHOT_INFO SI ON S.SNAPSHOT_ID = SI.SNAPSHOT_ID AND SI.object_id = S.application_id "+	 	 
     " JOIN %1.dss_objects appo on S.application_id = appo.object_id and appo.object_type_id = -102 "+
	 " JOIN %1.dss_object_types ot on ot.object_type_id = O.object_type_id "+ 		
	 "WHERE R.METRIC_ID = ANY (ARRAY[10411, 10412]) " +
	 " and S.SNAPSHOT_ID IN (%2) ";
	 

	//	public static void main(String[] args) {
//		runAddedDeletedModifiedArtifactQueries("localhost","2280", "postgres", "operator", "CastAIP", "training_82_central", Logger.getLogger("MainLogger"));
//
//	}

	/**
	 * Get CSS connection string
	 * @return
	 */
	private static String getCSSConnectionString(String dbHost) {
		return getCSSConnectionString(dbHost, "2282", "postgres");
	}
	
	/**
	 * Get CSS connection string
	 * @return
	 */
	private static String getCSSConnectionString(String dbHost, String dbPort, String dbDatabase) {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:postgresql://");
		sb.append(dbHost);
		sb.append(":");
		sb.append(dbPort);
		sb.append("/");
		sb.append(dbDatabase);

		return sb.toString();
	}	
	
	private static String getListSnapshotIds(List<SnapshotCharacteristics> listSnapshots) {
		if (listSnapshots == null || listSnapshots.size() == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		for (SnapshotCharacteristics appSnapshot : listSnapshots) {
			sb.append(appSnapshot.getId());
			sb.append(",");
		}
		String out =sb.toString(); 
		return out.substring(0,out.length()-1); 
		
	}
	
	/**
	 * This application needs to be filtered or be processed ?
	 * 
	 * @param appName
	 */
	private static boolean applicationtoBeProcessed(String filterApplicationNames, String appName) {
		if (appName == null)
			return false;
		if (filterApplicationNames == null || "".equals(filterApplicationNames))
			return true;
		String[] appToFilter = filterApplicationNames.split(",");
		for (int i = 0; i < appToFilter.length; i++) {
			if (appName.equals(appToFilter[i])) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * Returns the snapshot metric details for a given snapshot
	 * @param list
	 * @param appName
	 * @param snapshotId
	 * @return
	 */
	private static SnapshotAEPMetricsDetails getSnapshotAEPMetricsDetails(List<SnapshotAEPMetricsDetails> list, String appName, String snapshotId) {
		if (list == null || appName == null || snapshotId == null)
			return null;
		for (SnapshotAEPMetricsDetails item : list) {
			if (appName.equals(item.getApplicationName()) && item.getSnapshotCharacteristics() != null & snapshotId.equals(item.getSnapshotCharacteristics().getId()))
				return item;
		}
		return null;
	}
	

	
	/**
	 * Run sql queries on central schemas to collect the AEP metrics details (AEFP, AETP, Added AEP, Modified AEP, Deleted AEP)
	 * @param dbHost
	 * @param dbPort
	 * @param dbDatabase
	 * @param dbUser
	 * @param dbPassword
	 * @param schema
	 * @param filterApplicationNames
	 * @param logger
	 * @return
	 */
	public static List<SnapshotAEPMetricsDetails> runAEPReportMetrics(String dbHost, String dbPort, String dbDatabase, 
			String dbUser, String dbPassword, String schema, String filterApplicationNames, String versionFilter, Logger logger) {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String sqlQuery = "";
		List<SnapshotCharacteristics> listSnapshots = new ArrayList<SnapshotCharacteristics> ();
		List<SnapshotAEPMetricsDetails> listSnapshotssAEPMetricsDetails = new ArrayList<>();
		
		try {
				Class.forName(CSSDriver);
				con = DriverManager.getConnection(getCSSConnectionString(dbHost, dbPort, dbDatabase), dbUser, dbPassword);
				logger.debug("  CSS server is " + dbHost+":"+dbPort+"/"+dbDatabase+ " : " + schema);
				
				// Set autocommit to true
				con.setAutoCommit(true);
				st = con.createStatement();
				
				// Set search_path
				// required for CSS2, else when we query on views like aep_technical_artifacts_vw it is failing
				sqlQuery = SQL_SET_SEARCHPATH.replaceAll("%1", schema);

				logger.debug("  running SQL query to set search_path : " + sqlQuery);
				st.execute(sqlQuery);
				
				// Collect all snapshots for the central schema
				//sqlQuery = String.format(SQL_LIST_SNAPSHOTS, schema);
				sqlQuery = SQL_LIST_SNAPSHOTS.replaceAll("%1", schema);
				if (versionFilter != null) {
					if (AEPReport.FILTER_VERSIONS_LASTONE.equals(versionFilter))
						// filter only last snapshot id
						sqlQuery += " limit 1";
					else if (AEPReport.FILTER_VERSIONS_LASTTWO.equals(versionFilter))
						// filter only last two snapshot id
						sqlQuery += " limit 2";
					else 
						; // we take all versions
				}				
				logger.debug("  running SQL query snapshots : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
	
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setApplicationName(rs.getString("application_name"));
					// Checked if the application name needs to be filterd based on filter input parameter
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setApplicationId(rs.getString("application_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setTime(rs.getTimestamp("functional_date").getTime());
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					String funcDate = df.format(snapChar.getTime());
					snapChar.setIsoDate(funcDate);
					logger.debug(snapChar.toString());
					listSnapshots.add(snapChar);
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = new SnapshotAEPMetricsDetails();
					snapshotAEPMetricsDetails.setApplicationName(snapChar.getApplicationName());
					snapshotAEPMetricsDetails.setSnapshotCharacteristics(snapChar);
					listSnapshotssAEPMetricsDetails.add(snapshotAEPMetricsDetails);
				}
				rs.close();
				if (listSnapshots.size() == 0) 
					return null;
			
				// queryAETP details				
				sqlQuery = SQL_AETP_DETAILS;
				sqlQuery = sqlQuery.replaceAll("%1", schema).replaceAll("%2", getListSnapshotIds(listSnapshots));
				logger.debug("  running AETP details query : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setApplicationName(rs.getString("application_name"));
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;					
					AEPFunctionPointDetails details = new AEPFunctionPointDetails ();
					details.setObjectFullname(rs.getString("OBJECT_FULL_NAME"));
					details.setFpValue(rs.getDouble("FP"));
					details.setStatus(rs.getString("status"));
					details.setEffortComplexity(rs.getDouble("effort_complexity"));
					details.setEquivalentRatio(rs.getDouble("equivalent_ratio"));
					details.setObjectType(rs.getString("object_type_name"));
				
					//set Total AETP value
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = getSnapshotAEPMetricsDetails(listSnapshotssAEPMetricsDetails, snapChar.getApplicationName(), snapChar.getId());
					snapshotAEPMetricsDetails.getListAETP().add(details);

					//logger.debug(snapChar.getId() + "/" + rs.getString("OBJECT_FULL_NAME") +  "/"+ rs.getDouble("FP"));
				}
				rs.close();
		
				// queryAEFP details				
				sqlQuery = SQL_AEFP_DETAILS;
				sqlQuery = sqlQuery.replaceAll("%1", schema).replaceAll("%2", getListSnapshotIds(listSnapshots));
				logger.debug("  running AEFP details query : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setApplicationName(rs.getString("application_name"));
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;					
					AEPFunctionPointDetails details = new AEPFunctionPointDetails ();
					details.setObjectFullname(rs.getString("OBJECT_FULL_NAME"));
					details.setFpValue(rs.getDouble("FP"));
					details.setType(rs.getString("type"));
					details.setImpactFactor(rs.getDouble("impact_factor"));
					details.setObjectType(rs.getString("object_type_name"));
					//set Total AEFP value
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = getSnapshotAEPMetricsDetails(listSnapshotssAEPMetricsDetails, snapChar.getApplicationName(), snapChar.getId());
					snapshotAEPMetricsDetails.getListAEFP().add(details);
					//logger.debug(snapChar.getId() + "/" + rs.getString("OBJECT_FULL_NAME") +  "/"+ rs.getDouble("FP"));
				}

				// query Added AEP details		
				sqlQuery = SQL_AEP_ADDED_DETAILS;				
				sqlQuery = sqlQuery.replaceAll("%1", schema).replaceAll("%2", getListSnapshotIds(listSnapshots));
				logger.debug("  running Added AEP details query : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setApplicationName(rs.getString("application_name"));
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;					
					AEPFunctionPointDetails details = new AEPFunctionPointDetails ();
					details.setObjectFullname(rs.getString("OBJECT_FULL_NAME"));
					details.setFpValue(rs.getDouble("FP"));
					details.setType(rs.getString("type"));
					details.setObjectType(rs.getString("object_type_name"));
					
					//set Total AED Added value
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = getSnapshotAEPMetricsDetails(listSnapshotssAEPMetricsDetails, snapChar.getApplicationName(), snapChar.getId());
					snapshotAEPMetricsDetails.setTotalAEDAdded(snapshotAEPMetricsDetails.getTotalAEDAdded() + details.getFpValue());
					if ("Transactional Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPTransactionsAdded(snapshotAEPMetricsDetails.getTotalAEFPTransactionsAdded() + details.getFpValue());						
					} else  if ("Data Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPDatafunctionsAdded(snapshotAEPMetricsDetails.getTotalAEFPDatafunctionsAdded() + details.getFpValue());
					} else  if ("Technical Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAETPAdded(snapshotAEPMetricsDetails.getTotalAETPAdded() + details.getFpValue());						
					}

					snapshotAEPMetricsDetails.getListAEPAdded().add(details);
					//logger.debug(snapChar.getId() + "/" + rs.getDouble("FP"));
				}

				// query Modified AEP details		
				sqlQuery = SQL_AEP_MODIFIED_DETAILS;				
				sqlQuery = sqlQuery.replaceAll("%1", schema).replaceAll("%2", getListSnapshotIds(listSnapshots));
				logger.debug("  running Modified AEP details query : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setApplicationName(rs.getString("application_name"));
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;					
					AEPFunctionPointDetails details = new AEPFunctionPointDetails ();
					details.setObjectFullname(rs.getString("OBJECT_FULL_NAME"));
					details.setFpValue(rs.getDouble("FP"));
					details.setType(rs.getString("type"));
					details.setObjectType(rs.getString("object_type_name"));
					
					//set Total AED Modified value
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = getSnapshotAEPMetricsDetails(listSnapshotssAEPMetricsDetails, snapChar.getApplicationName(), snapChar.getId());
					snapshotAEPMetricsDetails.setTotalAEDModified(snapshotAEPMetricsDetails.getTotalAEDModified() + details.getFpValue());
					if ("Transactional Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPTransactionsModified(snapshotAEPMetricsDetails.getTotalAEFPTransactionsModified() + details.getFpValue());						
					} else  if ("Data Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPDatafunctionsModified(snapshotAEPMetricsDetails.getTotalAEFPDatafunctionsModified() + details.getFpValue());
					} else  if ("Technical Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAETPModified(snapshotAEPMetricsDetails.getTotalAETPModified() + details.getFpValue());						
					}	
					
					snapshotAEPMetricsDetails.getListAEPModified().add(details);
					//logger.debug(snapChar.getId() + "/" + rs.getDouble("FP"));
				}					
				
				// query Deleted AEP details		
				sqlQuery = SQL_AEP_DELETED_DETAILS;				
				sqlQuery = sqlQuery.replaceAll("%1", schema).replaceAll("%2", getListSnapshotIds(listSnapshots));
				logger.debug("  running Deleted AEP details query : " + sqlQuery);
				rs = st.executeQuery(sqlQuery);								
				while (rs.next()) {
					SnapshotCharacteristics snapChar = new SnapshotCharacteristics();
					snapChar.setId(rs.getString("snapshot_id"));
					snapChar.setVersion(rs.getString("object_version"));
					snapChar.setApplicationName(rs.getString("application_name"));
					if (!applicationtoBeProcessed(filterApplicationNames, snapChar.getApplicationName()))
						continue;					
					AEPFunctionPointDetails details = new AEPFunctionPointDetails ();
					details.setObjectFullname(rs.getString("OBJECT_FULL_NAME"));
					details.setFpValue(rs.getDouble("FP"));
					details.setType(rs.getString("type"));
					details.setObjectType(rs.getString("object_type_name"));
					
					SnapshotAEPMetricsDetails snapshotAEPMetricsDetails = getSnapshotAEPMetricsDetails(listSnapshotssAEPMetricsDetails, snapChar.getApplicationName(), snapChar.getId());
					snapshotAEPMetricsDetails.setTotalAEDDeleted(snapshotAEPMetricsDetails.getTotalAEDDeleted() + details.getFpValue());
					if ("Transactional Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPTransactionsDeleted(snapshotAEPMetricsDetails.getTotalAEFPTransactionsDeleted() + details.getFpValue());						
					} else  if ("Data Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAEFPDatafunctionsDeleted(snapshotAEPMetricsDetails.getTotalAEFPDatafunctionsDeleted() + details.getFpValue());
					} else  if ("Technical Function".equals(details.getType())) {
						snapshotAEPMetricsDetails.setTotalAETPDeleted(snapshotAEPMetricsDetails.getTotalAETPDeleted() + details.getFpValue());						
					}	

					snapshotAEPMetricsDetails.getListAEPDeleted().add(details);					
					//logger.debug(snapChar.getId() + "/" + rs.getDouble("FP"));
				}					
				
		} catch (ClassNotFoundException e) {
//			try {
//				con.rollback();
//			} catch (SQLException e1) {
//				log.warn("Erreur rollbak : " +  e.getMessage());
//			}

//			try {
//				if (con != null) { 
//					con.rollback();
//				}
//			} catch (SQLException e1) {
//				log.warn("Erreur rollback : " +  e.getMessage());
//			}
			logger.error("Error ClassNotFoundException running SQL query : " + e.getMessage());
			//return null;
		} catch (Exception e) {
			logger.error("Error running SQL query : " + e.getMessage());
			//return null;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					logger.info("Error closing SQL resultset : " +  e.getMessage());
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					logger.info("Error closing SQL statement : " +  e.getMessage());
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					logger.info("Erreur closing SQL connection : " +  e.getMessage());
				}
			}
		}
		return listSnapshotssAEPMetricsDetails;
	}
	
	
}
