package org.mvbrock.bcgames.payment.ws;

public class GetInfo {
    private Integer version;
    private Integer protocolversion;
    private Integer walletversion;
    private Double balance;
    private Integer blocks;
    private Integer connections;
    private String proxy;
    private Double difficulty;
    private Boolean testnet;
    private Integer keypoololdest;
    private Integer keypoolsize;
    private Double paytxfee;
    private String errors;
    
    public GetInfo() { }
    
    public GetInfo(Integer version, Integer protocolversion,
			Integer walletversion, Double balance, Integer blocks,
			Integer connections, String proxy, Double difficulty,
			Boolean testnet, Integer keypoololdest, Integer keypoolsize,
			Double paytxfee, String errors) {
		this.version = version;
		this.protocolversion = protocolversion;
		this.walletversion = walletversion;
		this.balance = balance;
		this.blocks = blocks;
		this.connections = connections;
		this.proxy = proxy;
		this.difficulty = difficulty;
		this.testnet = testnet;
		this.keypoololdest = keypoololdest;
		this.keypoolsize = keypoolsize;
		this.paytxfee = paytxfee;
		this.errors = errors;
	}
    
	public Integer getVersion() {
		return version;
	}
	public Integer getProtocolversion() {
		return protocolversion;
	}
	public Integer getWalletversion() {
		return walletversion;
	}
	public Double getBalance() {
		return balance;
	}
	public Integer getBlocks() {
		return blocks;
	}
	public Integer getConnections() {
		return connections;
	}
	public String getProxy() {
		return proxy;
	}
	public Double getDifficulty() {
		return difficulty;
	}
	public Boolean getTestnet() {
		return testnet;
	}
	public Integer getKeypoololdest() {
		return keypoololdest;
	}
	public Integer getKeypoolsize() {
		return keypoolsize;
	}
	public Double getPaytxfee() {
		return paytxfee;
	}
	public String getErrors() {
		return errors;
	}
}
