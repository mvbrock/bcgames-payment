package org.mvbrock.bcgames.payment.ws;

import org.mvbrock.bcgames.common.json.JsonObject;

public class TransactionDetails extends JsonObject {
	private String account;
	private String address;
	private String category;
	private Double amount;
	
	public TransactionDetails() {
	}

	public TransactionDetails(String account, String address, String category, Double amount) {
		super();
		this.account = account;
		this.address = address;
		this.category = category;
		this.amount = amount;
	}

	public String getAccount() {
		return account;
	}

	public String getAddress() {
		return address;
	}

	public String getCategory() {
		return category;
	}

	public Double getAmount() {
		return amount;
	}
}
