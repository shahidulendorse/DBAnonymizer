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
    "policyProposal"
})

@XmlRootElement(name = "PolicyProposalResult")
public class PolicyProposalResult {

    @XmlElement(name = "PolicyProposal", required = true)
    protected List<PolicyProposal> policyProposal;
    
    /**
     * Gets the value of the column property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the column property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolicyProposal().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link getPolicyProposal }
     * 
     * 
     */
    public List<PolicyProposal> getPolicyProposalResult() {
        if (policyProposal == null) {
        	policyProposal = new ArrayList<PolicyProposal>();
        }
        return this.policyProposal;
    }

}

