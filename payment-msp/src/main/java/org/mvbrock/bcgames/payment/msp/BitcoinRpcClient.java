package org.mvbrock.bcgames.payment.msp;

public interface BitcoinRpcClient {
	public GetInfo getinfo();
	public String getnewaddress();
	public Transaction gettransaction(String txid);
	public Transaction [] listtransactions();
	public Transaction [] listtransactions(String account, Integer count, Integer from);
	public Transaction [] listreceivedbyaddress();
	public String sendtoaddress(String address, Double amount, String comment);
}
