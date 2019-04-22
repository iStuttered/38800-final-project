package edu.iupui.cit388.project.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Scanner;

import edu.iupui.cit388.project.model.CreditCard;
import edu.iupui.cit388.project.model.OnlineOrder;
import edu.iupui.cit388.project.model.OrderLine;
import edu.iupui.cit388.project.service.OrderProcessingSystem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

// fx:controller="edu.iupui.cit388.project.controller.OrderProcessingController"
public class OrderProcessingController {

	// your @FXML goes here

	@FXML
	private ListView<OnlineOrder> OnlineOrders;
	@FXML
	private TextArea OnlineOrderSummary;

	private OrderProcessingSystem system;
	
	@FXML
	void initialize() {
		
		Path itemDataFile = Paths.get("resource\\Item.txt");
		system = new OrderProcessingSystem(itemDataFile.toAbsolutePath());

		Path orderDataFile = Paths.get("resource\\OnlineOrder.txt");
		loadOnlineOrder(system, orderDataFile.toAbsolutePath());

		OnlineOrders.setItems(FXCollections.observableArrayList(system.getOrders()));
		OnlineOrders.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OnlineOrder>() {
			@Override
			public void changed(ObservableValue<? extends OnlineOrder> observable, OnlineOrder oldValue, OnlineOrder newValue) {

				String orders = "";
				double sum = 0;

				for(OrderLine order : newValue.getOrderLines()){
					orders += order.toString() + "\n";
					sum += order.getQuantity() * order.getUnitPrice();
				}

				OnlineOrderSummary.setText(
						"Order: " + newValue.getId() + "\n"
						+ newValue.getAdditionalInfo() + "\n\n"
						+ orders
						+ "Total Amount: " + NumberFormat.getCurrencyInstance().format(sum)
				);
			}
		});

	}

	private void loadOnlineOrder(OrderProcessingSystem system, Path orderDataFile) {

		try (Scanner input = new Scanner(orderDataFile)) {

			while (input.hasNext()) {

				OnlineOrder order = system.createOnlineOrder();

				String number = input.next();
				int expireMonth = input.nextInt();
				int expireYear = input.nextInt();
				input.nextLine(); // finish reading the current line
				CreditCard creditCard = new CreditCard(number, expireMonth, expireYear);
				order.setCreditCardInfo(creditCard);

				String shipAddress = input.nextLine();
				order.setShipAddress(shipAddress);

				int numberOfOrderLine = input.nextInt();
				input.nextLine(); // finish reading the current line

				for (int i = 0; i < numberOfOrderLine; i++) {
					String itemDescription = input.next();
					int quantity = input.nextInt();
					double unitPrice = system.getPrice(itemDescription);
					OrderLine orderLine = new OrderLine(itemDescription, quantity, unitPrice);
					order.addOrderLine(orderLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
