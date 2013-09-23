package org.mvbrock.bcgames.payment.rs;

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
	private TransactionDetails [] details;

	public Transaction() {
	}

	public Transaction(String account, String address, String category, Double amount, Double fee,
			Integer confirmations, String blockhash, Integer blockindex, Integer blocktime, String txid, Integer time,
			Integer timereceived, String comment, TransactionDetails [] details) {
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
		this.details = details;
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

	public TransactionDetails [] getDetails() {
		return details;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((txid == null) ? 0 : txid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (txid == null) {
			if (other.txid != null)
				return false;
		} else if (!txid.equals(other.txid))
			return false;
		return true;
	}
}
