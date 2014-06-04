package com.sap.dpre.entities.policyproposal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sap.dpre.entities.jaxb.policy.Policy;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"proposalId",
		"computedRisk",
    "policyProposal"
    
})

@XmlRootElement(name = "PolicyProposal")
public class PolicyProposal {

    @XmlElement(name = "Policy", required = true)
    protected Policy policyProposal;

    @XmlElement(name = "ComputedRisk", required = true)
    protected float computedRisk;

    @XmlElement(name = "PolicyProposalID", required = true)
    protected int proposalId;
    
	/**
	 * @return the policyProposal
	 */
	public Policy getPolicyProposal() {
		return policyProposal;
	}

	/**
	 * @param policyProposal the policyProposal to set
	 */
	public void setPolicyProposal(Policy policyProposal) {
		this.policyProposal = policyProposal;
	}

	/**
	 * @return the computedRisk
	 */
	public float getComputedRisk() {
		return computedRisk;
	}

	/**
	 * @param computedRisk the computedRisk to set
	 */
	public void setComputedRisk(float computedRisk) {
		this.computedRisk = computedRisk;
	}

	/**
	 * @return the proposalID
	 */
	public int getProposalID() {
		return proposalId;
	}

	/**
	 * @param proposalID the proposalID to set
	 */
	public void setProposalID(int proposalID) {
		this.proposalId = proposalID;
	}
    

    
}

