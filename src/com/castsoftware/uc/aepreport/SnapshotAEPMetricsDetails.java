package com.castsoftware.uc.aepreport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SnapshotAEPMetricsDetails {
	
	private String applicationName = null;
	private SnapshotCharacteristics snapshotCharacteristics = null;
	private Double totalAEP = 0d;
	private Double totalAETP = 0d;
	private Double totalAEFP = 0d;
	private Double totalAEDAdded = 0d;
	private Double totalAEDModified = 0d;
	private Double totalAEDDeleted = 0d;
	
	private Double totalAETPAdded = 0d;
	private Double totalAETPModified = 0d;
	private Double totalAETPDeleted = 0d;
	private Double totalAEFPTransactionsAdded = 0d;
	private Double totalAEFPTransactionsModified = 0d;
	private Double totalAEFPTransactionsDeleted = 0d;
	private Double totalAEFPDatafunctionsAdded = 0d;
	private Double totalAEFPDatafunctionsModified = 0d;
	private Double totalAEFPDatafunctionsDeleted = 0d;	
	
	private List<AEPFunctionPointDetails> listAETP = new ArrayList<>();
	private List<AEPFunctionPointDetails> listAEFP = new ArrayList<>();	
	private List<AEPFunctionPointDetails> listAEPAdded = new ArrayList<>();		
	private List<AEPFunctionPointDetails> listAEPModified = new ArrayList<>();			
	private List<AEPFunctionPointDetails> listAEPDeleted = new ArrayList<>();
	
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public SnapshotCharacteristics getSnapshotCharacteristics() {
		return snapshotCharacteristics;
	}
	public void setSnapshotCharacteristics(SnapshotCharacteristics snapshotCharacteristics) {
		this.snapshotCharacteristics = snapshotCharacteristics;
	}
	public List<AEPFunctionPointDetails> getListAETP() {
		return listAETP;
	}
	public void setListAETP(List<AEPFunctionPointDetails> listAETP) {
		this.listAETP = listAETP;
	}
	public List<AEPFunctionPointDetails> getListAEFP() {
		return listAEFP;
	}
	public void setListAEFP(List<AEPFunctionPointDetails> listAEFP) {
		this.listAEFP = listAEFP;
	}
	public List<AEPFunctionPointDetails> getListAEPAdded() {
		return listAEPAdded;
	}
	public void setListAEPAdded(List<AEPFunctionPointDetails> listAEPAdded) {
		this.listAEPAdded = listAEPAdded;
	}
	public List<AEPFunctionPointDetails> getListAEPModified() {
		return listAEPModified;
	}
	public void setListAEPModified(List<AEPFunctionPointDetails> listAEPModified) {
		this.listAEPModified = listAEPModified;
	}
	public List<AEPFunctionPointDetails> getListAEPDeleted() {
		return listAEPDeleted;
	}
	public void setListAEPDeleted(List<AEPFunctionPointDetails> listAEPDeleted) {
		this.listAEPDeleted = listAEPDeleted;
	}
	public Double getTotalAETP() {
		return totalAETP;
	}
	public void setTotalAETP(Double totalAETP) {
		this.totalAETP = totalAETP;
	}
	public Double getTotalAEFP() {
		return totalAEFP;
	}
	public void setTotalAEFP(Double totalAEFP) {
		this.totalAEFP = totalAEFP;
	}
	public Double getTotalAEDAdded() {
		return totalAEDAdded;
	}
	public void setTotalAEDAdded(Double totalAEDAdded) {
		this.totalAEDAdded = totalAEDAdded;
	}
	public Double getTotalAEDModified() {
		return totalAEDModified;
	}
	public void setTotalAEDModified(Double totalAEDModified) {
		this.totalAEDModified = totalAEDModified;
	}
	public Double getTotalAEDDeleted() {
		return totalAEDDeleted;
	}
	public void setTotalAEDDeleted(Double totalAEDDeleted) {
		this.totalAEDDeleted = totalAEDDeleted;
	}			

	public Double getTotalAETPAdded() {
		return totalAETPAdded;
	}
	public void setTotalAETPAdded(Double totalAETPAdded) {
		this.totalAETPAdded = totalAETPAdded;
	}
	public Double getTotalAETPModified() {
		return totalAETPModified;
	}
	public void setTotalAETPModified(Double totalAETPModified) {
		this.totalAETPModified = totalAETPModified;
	}
	public Double getTotalAETPDeleted() {
		return totalAETPDeleted;
	}
	public void setTotalAETPDeleted(Double totalAETPDeleted) {
		this.totalAETPDeleted = totalAETPDeleted;
	}
	public Double getTotalAEFPTransactionsAdded() {
		return totalAEFPTransactionsAdded;
	}
	public void setTotalAEFPTransactionsAdded(Double totalAEFPTransactionsAdded) {
		this.totalAEFPTransactionsAdded = totalAEFPTransactionsAdded;
	}
	public Double getTotalAEFPTransactionsModified() {
		return totalAEFPTransactionsModified;
	}
	public void setTotalAEFPTransactionsModified(Double totalAEFPTransactionsModified) {
		this.totalAEFPTransactionsModified = totalAEFPTransactionsModified;
	}
	public Double getTotalAEFPTransactionsDeleted() {
		return totalAEFPTransactionsDeleted;
	}
	public void setTotalAEFPTransactionsDeleted(Double totalAEFPTransactionsDeleted) {
		this.totalAEFPTransactionsDeleted = totalAEFPTransactionsDeleted;
	}
	public Double getTotalAEFPDatafunctionsAdded() {
		return totalAEFPDatafunctionsAdded;
	}
	public void setTotalAEFPDatafunctionsAdded(Double totalAEFPDatafunctionsAdded) {
		this.totalAEFPDatafunctionsAdded = totalAEFPDatafunctionsAdded;
	}
	public Double getTotalAEFPDatafunctionsModified() {
		return totalAEFPDatafunctionsModified;
	}
	public void setTotalAEFPDatafunctionsModified(Double totalAEFPDatafunctionsModified) {
		this.totalAEFPDatafunctionsModified = totalAEFPDatafunctionsModified;
	}
	public Double getTotalAEFPDatafunctionsDeleted() {
		return totalAEFPDatafunctionsDeleted;
	}
	public void setTotalAEFPDatafunctionsDeleted(Double totalAEFPDatafunctionsDeleted) {
		this.totalAEFPDatafunctionsDeleted = totalAEFPDatafunctionsDeleted;
	}
	public Double getTotalAEP() {
		return totalAEP;
	}
	public void setTotalAEP(Double totalAEP) {
		this.totalAEP = totalAEP;
	}
	public String toString() {
		String sep = "#";
		StringBuffer sb = new StringBuffer();
		sb.append(applicationName);
		sb.append(sep);
		String snapId = "<N/A>";
		if (getSnapshotCharacteristics() !=null)
			snapId = getSnapshotCharacteristics().getId(); 
		sb.append(snapId);
		sb.append(sep);	
		sb.append(totalAEFP);
		sb.append(sep);		
		sb.append(totalAETP);
		sb.append(sep);		
		sb.append(totalAEDAdded);
		sb.append(sep);		
		sb.append(totalAEDModified);
		sb.append(sep);		
		sb.append(totalAEDDeleted);
		return sb.toString();
	}

	protected void recomputeTotalMetrics() {
		this.totalAEDAdded = round(this.totalAEDAdded, 0);
		this.totalAEDModified = round(this.totalAEDModified, 0);
		this.totalAEDDeleted= round(this.totalAEDDeleted, 0);
		this.totalAETPAdded = round(this.totalAETPAdded, 0);
		this.totalAETPModified = round(this.totalAETPModified, 0);
		this.totalAETPDeleted= round(this.totalAETPDeleted, 0);
		this.totalAEFPDatafunctionsAdded = round(this.totalAEFPDatafunctionsAdded, 0);
		this.totalAEFPDatafunctionsModified = round(this.totalAEFPDatafunctionsModified, 0);
		this.totalAEFPDatafunctionsDeleted= round(this.totalAEFPDatafunctionsDeleted, 0);		
		this.totalAEFPTransactionsAdded = round(this.totalAEFPTransactionsAdded, 0);
		this.totalAEFPTransactionsModified = round(this.totalAEFPTransactionsModified, 0);
		this.totalAEFPTransactionsDeleted= round(this.totalAEFPTransactionsDeleted, 0);		
		
		this.totalAETP = round(this.totalAETPAdded + this.totalAETPModified + this.totalAETPDeleted, 0); 
		this.totalAEFP = round(this.totalAEFPDatafunctionsAdded + this.totalAEFPDatafunctionsModified + this.totalAEFPDatafunctionsDeleted +
				this.totalAEFPTransactionsAdded + this.totalAEFPTransactionsModified + this.totalAEFPTransactionsDeleted, 0);
		
		this.totalAEP =  round(this.totalAETP + this.totalAEFP, 0);
		
	}
	
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	
}


