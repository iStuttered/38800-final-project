package edu.iupui.cit388.project.model;

import java.text.NumberFormat;
import java.util.Calendar;

public class OnlineOrder extends Order {

	private String shipAddress;
	private CreditCard creditCardInfo;
	
	public OnlineOrder(long id, Calendar dateTime) {
		super(id, dateTime);
	}

	public String getShipAddress() {
		return shipAddress;
	}

	public void setShipAddress(String shipAddress) {
		this.shipAddress = shipAddress;
	}

	public CreditCard getCreditCardInfo() {
		return creditCardInfo;
	}

	public void setCreditCardInfo(CreditCard creditCardInfo) {
		this.creditCardInfo = creditCardInfo;
	}

	@Override
	public String getAdditionalInfo() {
		return shipAddress + "\n";
	}

	@Override
	public String toString() {
		return getId() + "\n" + getAdditionalInfo();
	}

	public String toStringClean() {

		String orders = "";
		double calculated_total = 0;

		if(getOrderLines() == null) {
			orders = "";
		} else {
			for (OrderLine line : getOrderLines()) {
				orders += line.toString() + "\n";
				calculated_total += line.getQuantity() * line.getUnitPrice();
			}
		}

		String calculated_total_string = NumberFormat.getCurrencyInstance().format(calculated_total);

		orders += "Total Amount: " + calculated_total_string;

		String additional_info = getAdditionalInfo();

		if(additional_info == null) {
			additional_info = "";
		}

		return "Order: " + getId() + "\n" + additional_info + "\n" + orders;
	}
}
