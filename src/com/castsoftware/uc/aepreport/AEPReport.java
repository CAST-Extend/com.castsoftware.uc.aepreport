package com.castsoftware.uc.aepreport;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.castsoftware.uc.aepreport.CSSDbTripletQueryUtil;;

/**
 * AEP report data that is coming from central schemas
 * 
 * @author MMR
 *
 */
public class AEPReport {

	//////////////////////////////////////////////////////////////////////////////////////////////////

	// Please also change the version in version.properties file
	private static final String VERSION = "1.0.6";

	//////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String DEFAULT_ENCODING = "iso-8859-1";
	static String datenow_sdf = (new SimpleDateFormat("yyyyMMddHHmm")).format(new Date());

	// Report type possible options
	public static final String REPORTTYPE_METRICS_AEPREPORT = "AEPReport";

	// Filter version possible options
	public static final String FILTER_VERSIONS_LASTONE = "VERSIONS_LASTONE";
	public static final String FILTER_VERSIONS_LASTTWO = "VERSIONS_LASTTWO";
	public static final String FILTER_VERSIONS_ALL = "VERSIONS_ALL";

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Excel sheet names
	private static final String SHEET_SUMMARY="Summary";
	private static final String SHEET_AEFP="Functional AEP";
	private static final String SHEET_AETP="Technical AEP";
	private static final String SHEET_AEP_ADDED="AEP Added";
	private static final String SHEET_AEP_MODIFIED="AEP Modified";
	private static final String SHEET_AEP_DELETED="AEP Deleted";
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// command line short and long keys

	// short key params
	private final static String OPTION_SK_ENV = "env";
	private final static String OPTION_SK_RTYPE = "rt";
	private final static String OPTION_SK_APPFILTER = "pra";
	private final static String OPTION_SK_VERSIONFILTER = "pra";
	// DB
	private final static String OPTION_SK_DB_HOST = "dbh";
	private final static String OPTION_SK_DB_PORT = "dbpo";
	private final static String OPTION_SK_DB_DBNAME = "dbn";
	private final static String OPTION_SK_DB_SCHEMAS = "dbls";
	private final static String OPTION_SK_DB_USER = "dbu";
	private final static String OPTION_SK_DB_PWD = "dbpw";
	private final static String OPTION_SK_OUTPUT_FILE_PREFIX = "otfpr";
	private final static String OPTION_SK_OUTPUT_FOLDER = "otfold";
	
	
	// Lonk key params
	private final static String OPTION_LK_ENV = "environment";
	private final static String OPTION_LK_RTYPE = "reportType";
	private final static String OPTION_LK_APPFILTER = "processApplicationFilter";
	private final static String OPTION_LK_VERSIONFILTER = "versionFilter";
	// DB
	private final static String OPTION_LK_DB_HOST = "dbHost";
	private final static String OPTION_LK_DB_PORT = "dbPort";
	private final static String OPTION_LK_DB_DBNAME = "dbDatabaseName";
	private final static String OPTION_LK_DB_SCHEMAS = "dbSchemas";
	private final static String OPTION_LK_DB_USER = "dbUser";
	private final static String OPTION_LK_DB_PWD = "dbPassword";
	private final static String OPTION_LK_OUTPUT_FILE_PREFIX = "outputFilePrefix";
	private final static String OPTION_LK_OUTPUT_FOLDER = "outputFolder";
	
	/**
	 * Specific for Telefonica customer Hardcoded list of application with NO
	 * FP, separated by a comma Used to set a N/A value for FP metrics because
	 * we have used a FP licence key and want to hide those metrics
	 */
	private static final String[] TELEFONICASPECIFIC_APPLICATIONS_WITH_NOFP = new String[] {
			"U-057 - Unified-FSS-Billing" };


	/**
	 * Details of snapshot results
	 */
	private List<List<SnapshotAEPMetricsDetails>> listCentralSchemaMapSnapshotAEPMetrics = new ArrayList<List<SnapshotAEPMetricsDetails>>();

	/**
	 * Report type
	 */
	private String reportType = REPORTTYPE_METRICS_AEPREPORT;

	/**
	 * Output file prefix
	 */
	private String outputFilePrefix = "AEPReport";
	
	
	/**
	 * Output file prefix
	 */
	private String outputFolder = null;	
	
	
	/**
	 * Css DB Host Name (use temporarly to query central schema to collect
	 * additional metrics)
	 */
	private String cssDbHostname = null;

	/**
	 * Css DB Port
	 */
	private String cssDbPort = null;

	/**
	 * Css DB Database
	 */
	private String cssDbDatabase = null;

	/**
	 * Css DB user
	 */
	private String cssDbUser = null;

	/**
	 * Css DB password
	 */
	private String cssDbPassword = null;

	/**
	 * Css DB list of central schemas, separated by a comma
	 */
	private String cssDbListCentralSchemas = null;

	/**
	 * environment (PROD/DEV)
	 */
	private String environment = null;

	/**
	 * Used (optional) to filter application names
	 */
	private String filterApplicationNames = "";

	/**
	 * Used (optional) to filter versions
	 */
	private String filterVersions = FILTER_VERSIONS_LASTONE;

	/**
	 * Class logger
	 */
	private Logger logger = null;

	/**
	 * Constructor
	 */
	private AEPReport() {
		super();
		logger = Logger.getLogger("MainLogger");
	}

	/**
	 * Get report filename
	 * 
	 * @param appName
	 * @return
	 */
	private String getFileName(String appName, String snapshotVersionName) {
		StringBuffer sbFileName = new StringBuffer();
		if (this.outputFolder != null) {
			sbFileName.append(outputFolder);
			sbFileName.append("/");
		} 
		//else current folder
		
		sbFileName.append(outputFilePrefix);
		sbFileName.append("_");
		if (getEnvironment() != null) {
			sbFileName.append(getEnvironment());
			sbFileName.append("_");
		}
		if (appName != null) {
			sbFileName.append(appName);
			sbFileName.append("_");
		}
		if (FILTER_VERSIONS_LASTONE.equals(this.filterVersions) && snapshotVersionName != null) {
			sbFileName.append(snapshotVersionName);
			sbFileName.append("_");
		}
		sbFileName.append(datenow_sdf);
		sbFileName.append(".xlsx");
		// return "Export Data_"+sdfr+".xlsx";
		return sbFileName.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Report the outputs
	 * 
	 * @throws Exception
	 */
	protected void logInputs() throws Exception {
		logger.info("==============================");
		logger.info("Version:" + VERSION);
		logger.info("Parameters:");
		logger.info("CSS Host :" + getCssDbHostname());
		logger.info("CSS Port :" + getCssDbPort());
		logger.info("CSS User :" + getCssDbUser());
		logger.info("CSS central schemas :" + getCssDbListCentralSchemas());
		logger.info("Password:" + "*********");
		logger.info("Environment:" + getEnvironment());
		logger.info("Report type:" + getReportType());
		logger.info("Version filter:" + this.filterVersions);
		logger.info("Application filter:" + this.filterApplicationNames);
		logger.info("==============================");
	}

	/**
	 * Retrieve the data from the CAST Rest API
	 * 
	 * @throws Exception
	 */
	private void run() throws Exception {
		logInputs();

		retrieveDBCentralQueriesOutputs();
		// reportOutputstoLog();
		createExcelReportFiles();

	}

	/**
	 * Retrieve SQL queries outputs from the central schemas, if available
	 * 
	 * @throws Exception
	 */
	private void retrieveDBCentralQueriesOutputs() throws Exception {
		if (getCssDbHostname() == null || getCssDbListCentralSchemas() == null) {
			return;
		}
		String[] listCentralSchemas = getCssDbListCentralSchemas().split(",");
		for (int i = 0; i < listCentralSchemas.length; i++) {
			List<SnapshotAEPMetricsDetails> listSnapshotAEPMetrics = CSSDbTripletQueryUtil
					.runAEPReportMetrics(getCssDbHostname(), getCssDbPort(), getCssDbDatabase(), getCssDbUser(),
							getCssDbPassword(), listCentralSchemas[i], this.filterApplicationNames, this.filterVersions, logger);
			// recompute total values
			for (SnapshotAEPMetricsDetails snapshotAEPmetrics : listSnapshotAEPMetrics) {
				snapshotAEPmetrics.recomputeTotalMetrics();
			}
			this.listCentralSchemaMapSnapshotAEPMetrics.add(listSnapshotAEPMetrics);

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		AEPReport reportgen = new AEPReport();
		reportgen.parseCmdLineParameters(args);
		reportgen.checkParameters();

		reportgen.run();
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getCssDbHostname() {
		return cssDbHostname;
	}

	public void setCssDbHostname(String cssDbHostname) {
		this.cssDbHostname = cssDbHostname;
	}

	public String getCssDbListCentralSchemas() {
		return cssDbListCentralSchemas;
	}

	public void setCssDbListCentralSchemas(String cssDbListCentralSchemas) {
		this.cssDbListCentralSchemas = cssDbListCentralSchemas;
	}

	public String getCssDbPort() {
		return cssDbPort;
	}

	public void setCssDbPort(String cssDbPort) {
		this.cssDbPort = cssDbPort;
	}

	public String getCssDbDatabase() {
		return cssDbDatabase;
	}

	public void setCssDbDatabase(String cssDbDatabase) {
		this.cssDbDatabase = cssDbDatabase;
	}

	public String getCssDbUser() {
		return cssDbUser;
	}

	public void setCssDbUser(String cssDbUser) {
		this.cssDbUser = cssDbUser;
	}

	public String getCssDbPassword() {
		return cssDbPassword;
	}

	public void setCssDbPassword(String cssDbPassword) {
		this.cssDbPassword = cssDbPassword;
	}

	private boolean isTelefonicaSpecificApplicationWithNoFP(String applicationName) {
		for (int i = 0; i < TELEFONICASPECIFIC_APPLICATIONS_WITH_NOFP.length; i++) {
			if (TELEFONICASPECIFIC_APPLICATIONS_WITH_NOFP[i].equals(applicationName))
				return true;
		}
		return false;
	}

	private void flushExcelFile(String fileName, XSSFWorkbook workbook) throws IOException {
		// autosize columns
		setCellSizes(workbook);
		try {
			if (workbook == null)
				return;
			FileOutputStream outputStream = new FileOutputStream(fileName);
			logger.info("Creating file " + fileName);
			workbook.write(outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void createExcelReportFiles() throws Exception {

		String[] nameCellsTabSummary = new String[] { "Date", "Version", "AEP","Functional AEP",
				"Technical AEP", "Added AEP", "Modified AEP", "Deleted AEP", };
		String[] nameCellsTabAEFP = new String[] { "Date", "Version", "Object name", "No. FPs", "Impact factor", "Type", };		
		String[] nameCellsTabAETP = new String[] { "Date", "Version", "Object type", "Object name", "No. FPs", "Effort complexity", "Equivalent ratio", "Status", };
		String[] nameCellsTabAEPAddedModifiedDeleted = new String[] { "Date", "Version", "Object type", "Object name", "No. FPs", "Type", };

		// String[] nameCellsTabAETP

		for (List<SnapshotAEPMetricsDetails> listAEPDetails : this.listCentralSchemaMapSnapshotAEPMetrics) {
			String filename = null;
			XSSFWorkbook workbook = null;
			XSSFSheet sheetSummary = null;
			XSSFSheet sheetAEFP = null;
			XSSFSheet sheetAETP = null;
			XSSFSheet sheetAEPAdded = null;
			XSSFSheet sheetAEPModified = null;
			XSSFSheet sheetAEPDeleted = null;
			String previous_app = null;
			//Set<String> keys = listAEPDetails.keySet();
			// Iteration on snapshots
			String current_app = null;
			Row rowSummary = null;
			Cell cellSummary;
			
			Row rowSheet2 = null;
			int irowSheet2 = 0;
			Cell cellSheet2 = null;
			
			Row rowSheet3 = null;
			int irowSheet3 = 0;
			Cell cellSheet3 = null;
			
			Row rowSheet4 = null;
			int irowSheet4 = 0;
			Cell cellSheet4 = null;
			Row rowSheet5 = null;
			int irowSheet5 = 0;
			Cell cellSheet5 = null;			
			Row rowSheet6 = null;
			int irowSheet6 = 0;
			Cell cellSheet6 = null;			
			
			
			int iSnap = 0;
			for (SnapshotAEPMetricsDetails snapshotDetail : listAEPDetails) {
				// New application : this is done to manage multiple
				// applications in the same triplet
				current_app = snapshotDetail.getApplicationName();
				if (previous_app == null || !previous_app.equals(snapshotDetail.getApplicationName())) {
					if (previous_app != null && !previous_app.equals(snapshotDetail.getApplicationName())) {
						// Flush to excel file
						flushExcelFile(filename, workbook);
					}
					String strSnapshotVersionName = null;
					if (snapshotDetail.getSnapshotCharacteristics() != null && snapshotDetail.getSnapshotCharacteristics().getVersion() != null) { 
						strSnapshotVersionName = snapshotDetail.getSnapshotCharacteristics().getVersion();
					}
					filename = getFileName(current_app, strSnapshotVersionName);
					logger.info("Preparing data to generate file " + filename);					
					iSnap = 0;
					irowSheet2 = 0;
					irowSheet3 = 0;
					irowSheet4 = 0;
					irowSheet5 = 0;
					irowSheet6 = 0;					
					// intializing new file
					workbook = new XSSFWorkbook();
					// Sheet 1
					sheetSummary = workbook.createSheet(SHEET_SUMMARY);
					rowSummary = sheetSummary.createRow((short) iSnap++);
					int headerColNum = 0;
			        CellStyle styleBold = workbook.createCellStyle();
			        Font fontBold = workbook.createFont();
			        fontBold.setBold(true);
			        styleBold.setFont(fontBold);
				
					for (String nc : nameCellsTabSummary) {
						cellSummary = rowSummary.createCell(headerColNum++);
						cellSummary.setCellValue(nc);
						cellSummary.setCellStyle(styleBold);
					}
					// Sheet 2
					sheetAEFP = workbook.createSheet(SHEET_AEFP);
					rowSheet2 = sheetAEFP.createRow((short) irowSheet2++);
					headerColNum = 0;
					for (String nc : nameCellsTabAEFP) {
						cellSheet2 = rowSheet2.createCell(headerColNum++);
						cellSheet2.setCellValue(nc);
						cellSheet2.setCellStyle(styleBold);
					}
					// Sheet 3
					sheetAETP = workbook.createSheet(SHEET_AETP);
					rowSheet3 = sheetAETP.createRow((short) irowSheet3++);
					headerColNum = 0;
					for (String nc : nameCellsTabAETP) {
						cellSheet3 = rowSheet3.createCell(headerColNum++);
						cellSheet3.setCellValue(nc);
						cellSheet3.setCellStyle(styleBold);
					}
					// Sheet 4
					sheetAEPAdded = workbook.createSheet(SHEET_AEP_ADDED);
					rowSheet4 = sheetAEPAdded.createRow((short) irowSheet4++);
					headerColNum = 0;
					for (String nc : nameCellsTabAEPAddedModifiedDeleted) {
						cellSheet4 = rowSheet4.createCell(headerColNum++);
						cellSheet4.setCellValue(nc);
						cellSheet4.setCellStyle(styleBold);
					}					
					// Sheet 5
					sheetAEPModified = workbook.createSheet(SHEET_AEP_MODIFIED);					
					rowSheet5 = sheetAEPModified.createRow((short) irowSheet5++);
					headerColNum = 0;
					for (String nc : nameCellsTabAEPAddedModifiedDeleted) {
						cellSheet5 = rowSheet5.createCell(headerColNum++);
						cellSheet5.setCellValue(nc);
						cellSheet5.setCellStyle(styleBold);
					}					

					// Sheet 6
					sheetAEPDeleted = workbook.createSheet(SHEET_AEP_DELETED);
					rowSheet6 = sheetAEPDeleted.createRow((short) irowSheet6++);
					headerColNum = 0;
					for (String nc : nameCellsTabAEPAddedModifiedDeleted) {
						cellSheet6 = rowSheet6.createCell(headerColNum++);
						cellSheet6.setCellValue(nc);
						cellSheet6.setCellStyle(styleBold);
					}									
				}
				DataFormat formatSummaryNumeric = workbook.createDataFormat();
				CellStyle styleNumFormat = workbook.createCellStyle();
				styleNumFormat.setDataFormat(formatSummaryNumeric.getFormat("0"));
	
				CellStyle styleFPNumFormat = workbook.createCellStyle();
				styleFPNumFormat.setDataFormat(formatSummaryNumeric.getFormat("0.0"));				
				
//				logger.info("1:" + snapshotDetail.getTotalAEP());
//				logger.info("2:" + snapshotDetail.getTotalAEFP());
//				logger.info("3:" + snapshotDetail.getTotalAETP());
//				logger.info("4:" + snapshotDetail.getTotalAEFPDatafunctionsAdded());
//				logger.info("5:" + snapshotDetail.getTotalAEFPDatafunctionsModified());
//				logger.info("6:" + snapshotDetail.getTotalAEFPDatafunctionsDeleted());
//				logger.info("7:" + snapshotDetail.getTotalAEFPTransactionsAdded());
//				logger.info("8:" + snapshotDetail.getTotalAEFPTransactionsModified());
//				logger.info("9:" + snapshotDetail.getTotalAEFPTransactionsDeleted());
//				logger.info("10:" + snapshotDetail.getTotalAETPAdded());
//				logger.info("11:" + snapshotDetail.getTotalAETPModified());
//				logger.info("12:" + snapshotDetail.getTotalAETPDeleted());
				
				// processing the data for 1 snapshot
				// Summary Tab
				rowSummary = sheetSummary.createRow(iSnap++);
				int j = 0;
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAEP());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAEFP());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAETP());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAEDAdded());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAEDModified());
				cellSummary.setCellStyle(styleNumFormat);
				cellSummary = rowSummary.createCell(j++);
				cellSummary.setCellValue(snapshotDetail.getTotalAEDDeleted());
				cellSummary.setCellStyle(styleNumFormat);
				// Tab 2 (AEFP)
				for (AEPFunctionPointDetails fpd : snapshotDetail.getListAEFP()) {
					// Filter 0 FP values
					if (fpd.getFpValue() == 0) {
						continue;
					}
					rowSheet2 = sheetAEFP.createRow(irowSheet2++);
					int k = 0;
					cellSheet2 = rowSheet2.createCell(k++);
					cellSheet2.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
					cellSheet2 = rowSheet2.createCell(k++);
					cellSheet2.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
					//cellSheet2 = rowSheet2.createCell(k++);
					//cellSheet2.setCellValue(fpd.getObjectType());
					cellSheet2 = rowSheet2.createCell(k++);
					cellSheet2.setCellValue(fpd.getObjectFullname());
					cellSheet2 = rowSheet2.createCell(k++);
					cellSheet2.setCellValue(fpd.getFpValue());	
					cellSheet2.setCellStyle(styleFPNumFormat);
					cellSheet2 = rowSheet2.createCell(k++);
					if (fpd.getImpactFactor() != null )
						cellSheet2.setCellValue(fpd.getImpactFactor());
					else 
						cellSheet2.setCellValue("");
					cellSheet2 = rowSheet2.createCell(k++);
					cellSheet2.setCellValue(fpd.getType());
				}
				// Tab 3 (AETP)
				for (AEPFunctionPointDetails fpd : snapshotDetail.getListAETP()) {
					// Filter 0 FP values
					if (fpd.getFpValue() == 0) {
						continue;
					}					
					rowSheet3 = sheetAETP.createRow(irowSheet3++);
					int k = 0;
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(fpd.getObjectType());					
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(fpd.getObjectFullname());
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(fpd.getFpValue());	
					cellSheet3.setCellStyle(styleFPNumFormat);
					cellSheet3 = rowSheet3.createCell(k++);
					if (fpd.getEffortComplexity() != null )
						cellSheet3.setCellValue(fpd.getEffortComplexity());
					else 
						cellSheet3.setCellValue("");
					cellSheet3 = rowSheet3.createCell(k++);					
					if (fpd.getEquivalentRatio() != null )
						cellSheet3.setCellValue(fpd.getEquivalentRatio());
					else 
						cellSheet3.setCellValue("");					
					cellSheet3 = rowSheet3.createCell(k++);
					cellSheet3.setCellValue(fpd.getStatus());
				}
				// Tab 4 (Added AEP)
				for (AEPFunctionPointDetails fpd : snapshotDetail.getListAEPAdded()) {
					// Filter 0 FP values
					if (fpd.getFpValue() == 0) {
						continue;
					}					
					rowSheet4 = sheetAEPAdded.createRow(irowSheet4++);
					int k = 0;
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(fpd.getObjectType());					
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(fpd.getObjectFullname());
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(fpd.getFpValue());			
					cellSheet4.setCellStyle(styleFPNumFormat);
					cellSheet4 = rowSheet4.createCell(k++);
					cellSheet4.setCellValue(fpd.getType());
				}
				// Tab 5 (Modified AEP)
				for (AEPFunctionPointDetails fpd : snapshotDetail.getListAEPModified()) {
					// Filter 0 FP values
					if (fpd.getFpValue() == 0) {
						continue;
					}					
					rowSheet5 = sheetAEPModified.createRow(irowSheet5++);
					int k = 0;
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(fpd.getObjectType());					
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(fpd.getObjectFullname());
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(fpd.getFpValue());		
					cellSheet5.setCellStyle(styleFPNumFormat);
					cellSheet5 = rowSheet5.createCell(k++);
					cellSheet5.setCellValue(fpd.getType());
				}
				// Tab 6 (Deleted AEP)
				for (AEPFunctionPointDetails fpd : snapshotDetail.getListAEPDeleted()) {
					// Filter 0 FP values
					if (fpd.getFpValue() == 0) {
						continue;
					}					
					rowSheet6 = sheetAEPDeleted.createRow(irowSheet6++);
					int k = 0;
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(snapshotDetail.getSnapshotCharacteristics().getIsoDate());
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(snapshotDetail.getSnapshotCharacteristics().getVersion());
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(fpd.getObjectType());					
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(fpd.getObjectFullname());
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(fpd.getFpValue());		
					cellSheet6.setCellStyle(styleFPNumFormat);					
					cellSheet6 = rowSheet6.createCell(k++);
					cellSheet6.setCellValue(fpd.getType());
				}

				
				
				// logger.info(snapshotDetail.toString());
				previous_app = current_app;
			}
			if (current_app != null) {
				// Flush to excel file
				flushExcelFile(filename, workbook);
			}
		}

	}

	private void setCellSizes(XSSFWorkbook wb) {
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(0);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(0);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(1);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(2);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(3);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(4);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(5);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(6);
		wb.getSheet(SHEET_SUMMARY).autoSizeColumn(7);
		
		wb.getSheet(SHEET_AEFP).autoSizeColumn(0);
		wb.getSheet(SHEET_AEFP).autoSizeColumn(1);
		wb.getSheet(SHEET_AEFP).setColumnWidth(2, 25000);
		wb.getSheet(SHEET_AEFP).autoSizeColumn(3);
		wb.getSheet(SHEET_AEFP).autoSizeColumn(4);
		wb.getSheet(SHEET_AEFP).autoSizeColumn(5);
		
		wb.getSheet(SHEET_AETP).autoSizeColumn(0);
		wb.getSheet(SHEET_AETP).autoSizeColumn(1);
		wb.getSheet(SHEET_AEFP).autoSizeColumn(2);
		wb.getSheet(SHEET_AETP).setColumnWidth(3, 25000);
		wb.getSheet(SHEET_AETP).autoSizeColumn(4);
		wb.getSheet(SHEET_AETP).autoSizeColumn(5);
		wb.getSheet(SHEET_AETP).autoSizeColumn(6);
		wb.getSheet(SHEET_AETP).autoSizeColumn(7);
		
		
		wb.getSheet(SHEET_AEP_ADDED).autoSizeColumn(0);
		wb.getSheet(SHEET_AEP_ADDED).autoSizeColumn(1);
		wb.getSheet(SHEET_AEP_ADDED).setColumnWidth(2, 8500);
		wb.getSheet(SHEET_AEP_ADDED).setColumnWidth(3, 25000);
		wb.getSheet(SHEET_AEP_ADDED).autoSizeColumn(4);
		wb.getSheet(SHEET_AEP_ADDED).autoSizeColumn(5);
		
		wb.getSheet(SHEET_AEP_MODIFIED).autoSizeColumn(0);
		wb.getSheet(SHEET_AEP_MODIFIED).autoSizeColumn(1);
		wb.getSheet(SHEET_AEP_MODIFIED).setColumnWidth(2, 8500);
		wb.getSheet(SHEET_AEP_MODIFIED).setColumnWidth(3, 25000);
		wb.getSheet(SHEET_AEP_MODIFIED).autoSizeColumn(4);
		wb.getSheet(SHEET_AEP_MODIFIED).autoSizeColumn(5);
		
		wb.getSheet(SHEET_AEP_DELETED).autoSizeColumn(0);
		wb.getSheet(SHEET_AEP_DELETED).autoSizeColumn(1);
		wb.getSheet(SHEET_AEP_DELETED).setColumnWidth(2, 8500);
		wb.getSheet(SHEET_AEP_DELETED).setColumnWidth(3, 25000);
		wb.getSheet(SHEET_AEP_DELETED).autoSizeColumn(4);
		wb.getSheet(SHEET_AEP_DELETED).autoSizeColumn(5);			
	}
	

	public static void printUsage(final String applicationName, final Options options, final OutputStream out) {
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter usageFormatter = new HelpFormatter();
		usageFormatter.printUsage(writer, 80, applicationName, options);
		writer.close();
	}

	/**
	 * Write "help" to the provided OutputStream.
	 */
	public static void printHelp(final Options options, final int printedRowWidth, final String header,
			final String footer, final int spacesBeforeOption, final int spacesBeforeOptionDescription,
			final boolean displayUsage, final OutputStream out) {
		final String commandLineSyntax = "To be completed";
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, printedRowWidth, commandLineSyntax, header, options, spacesBeforeOption,
				spacesBeforeOptionDescription, footer, displayUsage);
		writer.close();
	}

	/////////////////////////////////////////////

	private Options constructCmdLineOptions() {
		Options options = new Options();
		// Optional
		options.addOption(OPTION_SK_ENV, OPTION_LK_ENV, true, "environment (PROD / DEV / DEMO)");
		// Optional, default = AEPReport
		options.addOption(OPTION_SK_RTYPE, OPTION_LK_RTYPE, true, "report type (AEPReport)");
		// Optional		
		options.addOption(OPTION_SK_APPFILTER, OPTION_LK_APPFILTER, true,
				"Application name to be processed (App1,App2)");
		// Optional, default = VERSIONS_LASTONE
		options.addOption(OPTION_SK_VERSIONFILTER, OPTION_LK_VERSIONFILTER,
		 true,
		 "Version filter (VERSIONS_LASTONE, VERSIONS_LASTTWO, VERSIONS_ALL)");
		
		// mandatory
		options.addOption(OPTION_SK_DB_HOST, OPTION_LK_DB_HOST, true, "DB host");
		options.addOption(OPTION_SK_DB_PORT, OPTION_LK_DB_PORT, true, "DB port");
		options.addOption(OPTION_SK_DB_DBNAME, OPTION_LK_DB_DBNAME, true, "DB name");
		options.addOption(OPTION_SK_DB_SCHEMAS, OPTION_LK_DB_SCHEMAS, true, "DB schemas list");
		options.addOption(OPTION_SK_DB_USER, OPTION_LK_DB_USER, true, "DB user");
		options.addOption(OPTION_SK_DB_PWD, OPTION_LK_DB_PWD, true, "DB password");
		
		// Optional default = AEPReport 
		options.addOption(OPTION_SK_OUTPUT_FILE_PREFIX, OPTION_LK_OUTPUT_FILE_PREFIX, true, "Output file prefix");
		// Optional default = current folder
		options.addOption(OPTION_SK_OUTPUT_FOLDER, OPTION_LK_OUTPUT_FOLDER, true, "Output folder (if empty, current folder)");
		

		return options;
	}

	/**
	 * Load the command line parameters
	 * 
	 * @throws ParseException
	 */
	protected void parseCmdLineParameters(String[] p_parameters) {
		logger.info("parsing command line ...");

		try {
			Options options = constructCmdLineOptions();
			CommandLineParser parser = new GnuParser();
			org.apache.commons.cli.CommandLine cmd = parser.parse(options, p_parameters);
			if (logger.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer("  command line=");
				for (String arg : p_parameters) {
					sb.append(arg);
					sb.append(" ");
				}
				logger.debug(sb.toString());
			}
			// set only if value not null, else default value
			if (cmd.getOptionValue(OPTION_LK_RTYPE) != null)
				this.reportType = cmd.getOptionValue(OPTION_LK_RTYPE);
			if (cmd.getOptionValue(OPTION_LK_ENV) != null && !"".equals(cmd.getOptionValue(OPTION_LK_ENV))) {
				this.environment = cmd.getOptionValue(OPTION_LK_ENV);
			}
			if (cmd.hasOption(OPTION_LK_APPFILTER)) {
				this.filterApplicationNames = cmd.getOptionValue(OPTION_LK_APPFILTER);
			}
			this.cssDbHostname = cmd.getOptionValue(OPTION_LK_DB_HOST);
			this.cssDbPort = cmd.getOptionValue(OPTION_LK_DB_PORT);
			this.cssDbDatabase = cmd.getOptionValue(OPTION_LK_DB_DBNAME);
			this.cssDbListCentralSchemas = cmd.getOptionValue(OPTION_LK_DB_SCHEMAS);
			this.cssDbUser = cmd.getOptionValue(OPTION_LK_DB_USER);
			this.cssDbPassword = cmd.getOptionValue(OPTION_LK_DB_PWD);
			if (cmd.hasOption(OPTION_LK_VERSIONFILTER)) {
				 this.filterVersions = cmd.getOptionValue(OPTION_LK_VERSIONFILTER);
			}
			if (cmd.getOptionValue(OPTION_LK_OUTPUT_FILE_PREFIX) != null && !"%OUTPUTFILEPREFIX%".equals(cmd.getOptionValue(OPTION_LK_OUTPUT_FILE_PREFIX))) {
				this.outputFilePrefix = cmd.getOptionValue(OPTION_LK_OUTPUT_FILE_PREFIX);
			}			 
			if (cmd.getOptionValue(OPTION_LK_OUTPUT_FOLDER) != null && !"%OUTPUT_FOLDER%".equals(cmd.getOptionValue(OPTION_LK_OUTPUT_FOLDER))) {
				this.outputFolder = cmd.getOptionValue(OPTION_LK_OUTPUT_FOLDER);
			}			 
			

			 

		} catch (org.apache.commons.cli.ParseException e) {
			logger.error("Error parsing command line : " + e.getMessage());
			printHelp(constructCmdLineOptions(), 80, "", "", 5, 3, true, System.out);
			System.exit(-1);
		}
		logger.info("parsing command line [OK]");
	}

	/**
	 * Check that the parameters are correct
	 */
	protected void checkParameters() {
		boolean abort = false;
		// TODO to be completed
		// logger.info("checking parameters ...");

		/*
		 * User parameters (command line)
		 */
		// Mandatory parameters
		// if (reportType == null || css == null || user == null || password ==
		// null || environment == null) {
		// if (reportType == null) {
		// logger.fatal("Aborting ! reportType parameter is not set");
		// }
		// if (rootURL == null) {
		// logger.fatal("Aborting ! rootURL parameter is not set");
		// }
		// if (user == null) {
		// logger.fatal("Aborting ! user parameter is not set");
		// }
		// if (password == null) {
		// logger.fatal("Aborting ! password parameter is not set");
		// }
		// if (environment == null) {
		// logger.fatal("Aborting ! environment parameter is not set");
		// }
		// System.exit(-1);
		// }
		//
		// // Check that the report type is in the accepted list
		// if (!reportType.equals(REPORTTYPE_METRICS_KPIREPORT) &&
		// !reportType.equals(REPORTTYPE_METRICS_FULLREPORT)
		// && !reportType.equals(REPORTTYPE_ENV_DELTAREPORT) &&
		// !reportType.equals(REPORTTYPE_QR_SIMPLEREPORT)
		// && !reportType.equals(REPORTTYPE_QR_FULLREPORT)) {
		// logger.fatal(
		// "Aborting ! reportType parameter do not have a correct value :
		// Metrics_KPIReport / Metrics_FullReport / Env_DeltaReport /
		// QR_QRSimpleReport / QR_QRReportWithBC");
		// System.exit(-1);
		// }
		//
		// //
		// if (!FILTER_VERSIONS_ALL.equals(filterVersions) &&
		// !FILTER_VERSIONS_LASTONE.equals(filterVersions)
		// && !FILTER_VERSIONS_LASTTWO.equals(filterVersions)) {
		// logger.fatal(
		// "Aborting ! Version filter paramater value do not have a correct
		// value : VERSIONS_LASTONE / VERSIONS_LASTTWO / VERSION_ALL");
		// abort = true;
		// }
		//
		// // Metrics report are done on engineering domains
		// if (isMetricsReport()) {
		// if (AEDDomains == null || "".equals("AEDDomains")) {
		// logger.fatal("Aborting ! engineering domains parameter is not
		// set/empty");
		// abort = true;
		// }
		// }
		// // QR report are done on health domain
		// if (isQRReport()) {
		// if (AADDomain == null || "".equals("AADDomain")) {
		// logger.fatal("Aborting ! health domains parameter is not set/empty");
		// abort = true;
		// }
		// }
		// // If DB parameters are set, all of them must be set
		// if (cssDbHostname != null) {
		// if (cssDbPort == null || "".equals("cssDbPort") || cssDbDatabase ==
		// null || "".equals("cssDbDatabase")
		// || cssDbListCentralSchemas == null ||
		// "".equals("cssDbListCentralSchemas") || cssDbUser == null
		// || "".equals("cssDbUser") || cssDbPassword == null ||
		// "".equals("cssDbPassword")) {
		// logger.fatal(
		// "Aborting ! One of the parameters is not set/empty : DB port, DB
		// name, DB schemas list, DB user or DB password");
		// abort = true;
		// }
		// }

		if (abort) {
			System.exit(-1);
		}
	}


	
}
