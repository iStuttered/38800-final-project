package edu.iupui.cit388.project.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;

import edu.iupui.cit388.project.model.*;
import edu.iupui.cit388.project.service.OrderProcessingSystem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

// fx:controller="edu.iupui.cit388.project.controller.OrderProcessingController"
public class OrderProcessingController {

	// your @FXML goes here

	@FXML
	private ListView<OnlineOrder> OnlineOrders;
	@FXML
	private TextArea OnlineOrderSummary;
	@FXML
	private ComboBox PotentialItems;
	@FXML
	private TextArea NewOrderSummary;
	@FXML
	private Button NewOrderButton;
	@FXML
	private Button AddItemButton;
	@FXML
	private TextField QuantityTextField;
	@FXML
	private TextField AddressTextField;
	@FXML
	private Button ConfirmButton;

	private OnlineOrder Current_Order;

	private OrderProcessingSystem system;

	private ArrayList<OnlineOrder> SystemOrders;
	
	@FXML
	void initialize() {
		
		Path itemDataFile = Paths.get("resource\\Item.txt");
		system = new OrderProcessingSystem(itemDataFile.toAbsolutePath());

		SystemOrders = (ArrayList) system.getOrders();

		Path orderDataFile = Paths.get("resource\\OnlineOrder.txt");
		loadOnlineOrder(system, orderDataFile.toAbsolutePath());

		OnlineOrders.setItems(FXCollections.observableArrayList(SystemOrders));
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

		ConfirmButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {

				if(Current_Order != null) {
					Current_Order = null;
				}

				OnlineOrders.setItems(FXCollections.observableArrayList(SystemOrders));
			}
		});

		NewOrderButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {

				if(Current_Order != null) {
					Current_Order.setOrderLines(null);
					Current_Order.setShipAddress(null);
					AddressTextField.setText("");
					QuantityTextField.setText("");
					NewOrderSummary.setText(Current_Order.toStringClean());
				}
			}
		});

		AddItemButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {

				int quantity = Integer.parseInt(QuantityTextField.getText());
				String address = AddressTextField.getText();

				if(Current_Order == null) {
					Current_Order = system.createOnlineOrder();
				}

				if(Current_Order.getShipAddress() == null) {
					Current_Order.setShipAddress(address);
				}

				Item selected = (Item) PotentialItems.getValue();

				if(quantity < 1) {
					return;
				}

				if(address == null || address.length() < 1) {
					return;
				}

				OrderLine new_order_line = new OrderLine(selected.getName(), quantity, selected.getPrice());

				Current_Order.addOrderLine(new_order_line);

				NewOrderSummary.setText(Current_Order.toStringClean());

			}
		});

		PotentialItems.setItems(getPotentialItems());

	}

	private ObservableList<String> getPotentialItems() {
		Path item_file_path = Paths.get("resource\\Item.txt");
		File item_file = new File(item_file_path.toAbsolutePath().toString());

		ArrayList item_list = new ArrayList();

		try (Scanner input = new Scanner(item_file)) {

			while(input.hasNext()) {

				String[] data = input.nextLine().split(" ");

				String item_name = data[0];
				double item_price = Double.parseDouble(data[1]);

				Item current_item_in_file = new Item(item_name, item_price);

				item_list.add(current_item_in_file);
			}

		} catch(FileNotFoundException e) {

		}

		return  FXCollections.observableArrayList(item_list);

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
