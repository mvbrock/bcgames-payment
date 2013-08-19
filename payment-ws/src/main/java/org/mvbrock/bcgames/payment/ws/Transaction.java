package org.mvbrock.bcgames.payment.ws;

import org.mvbrock.bcgames.common.json.JsonObject;

public class Transaction extends JsonObject {
	private String account;
	private String address;
	private String category;
	private Double amount;
	private Double fee;
	private Integer confirmations;
	private String blockhash;
	private Integer blockindex;
	private Integer blocktime;
	private String txid;
	private Integer time;
	private Integer timereceived;
	private String comment;

	public Transaction() {
	}

	public Transaction(String account, String address, String category, Double amount, Double fee,
			Integer confirmations, String blockhash, Integer blockindex, Integer blocktime, String txid, Integer time,
			Integer timereceived, String comment) {
		this.account = account;
		this.address = address;
		this.category = category;
		this.amount = amount;
		this.fee = fee;
		this.confirmations = confirmations;
		this.blockhash = blockhash;
		this.blockindex = blockindex;
		this.blocktime = blocktime;
		this.txid = txid;
		this.time = time;
		this.timereceived = timereceived;
		this.comment = comment;
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

	public Double getFee() {
		return fee;
	}

	public Integer getConfirmations() {
		return confirmations;
	}

	public String getBlockhash() {
		return blockhash;
	}

	public Integer getBlockindex() {
		return blockindex;
	}

	public Integer getBlocktime() {
		return blocktime;
	}

	public String getTxid() {
		return txid;
	}

	public Integer getTime() {
		return time;
	}

	public Integer getTimereceived() {
		return timereceived;
	}

	public String getComment() {
		return comment;
	}
}
