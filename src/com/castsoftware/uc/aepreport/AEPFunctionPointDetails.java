package com.castsoftware.uc.aepreport;

public class AEPFunctionPointDetails {
	
	private String objectName;
	private String objectFullname;
	private String objectType;
	private Double fpValue;
	private Double effortComplexity;
	private Double equivalentRatio;
	private String type;
	private Double impactFactor;
	private String status;
	 
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getObjectFullname() {
		return objectFullname;
	}
	public void setObjectFullname(String objectFullname) {
		this.objectFullname = objectFullname;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public Double getFpValue() {
		return fpValue;
	}
	public void setFpValue(Double fpValue) {
		this.fpValue = fpValue;
	}
	public Double getEffortComplexity() {
		return effortComplexity;
	}
	public void setEffortComplexity(Double effortComplexity) {
		this.effortComplexity = effortComplexity;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Double getImpactFactor() {
		return impactFactor;
	}
	public void setImpactFactor(Double impactFactor) {
		this.impactFactor = impactFactor;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
		
	}
	public Double getEquivalentRatio() {
		return equivalentRatio;
	}
	public void setEquivalentRatio(Double equivalentRatio) {
		this.equivalentRatio = equivalentRatio;
	}
	
}
