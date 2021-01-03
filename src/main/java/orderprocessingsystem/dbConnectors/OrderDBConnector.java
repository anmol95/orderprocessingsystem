package orderprocessingsystem.dbConnectors;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.time.Instant;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import orderprocessingsystem.models.*;

public class OrderDBConnector {

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static String tableName = "Order";

    public static void createOrder(String food_id, Integer quantity) {

        Table table = dynamoDB.getTable(tableName);
        try {

            Double base_price = quantity*getPrice();

            Item item = new Item().withPrimaryKey("id", UUID.randomUUID().toString())
                .withNumber("base_price", base_price)
                .withNumber("tax", base_price*0.05)
                .withNumber("estimated_prep_time", quantity*getEstimatedPrepTime())
                .withNumber("estimated_delivery_time", getEstimatedDeliveryTime())
                .withString("timestamp", getTimestamp())
                .withString("status", "STARTED")
                .withNumber("delivery_charges", getDeliveryCharges());

            table.putItem(item);

        }
        catch (Exception e) {
            System.err.println("Create order failed.");
            System.err.println(e.getMessage());
        }
    }

    public static void updateOrder(final String order_id, final String status) {
        Table table = dynamoDB.getTable(tableName);

        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("id", order_id)
                .withUpdateExpression("set #na = :val1").withNameMap(new NameMap().with("#na", "status"))
                .withValueMap(new ValueMap().withString(":val1", status)).withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);

            // Check the response.
            System.out.println("Printing item after adding new attribute...");
            System.out.println(outcome.getItem().toJSONPretty());

        } catch (Exception e) {
            System.err.println("Failed to add new attribute in " + tableName);
            System.err.println(e.getMessage());
        }
    }

    public static OrderTimeResponse getTime(final String order_id) {
        Table table = dynamoDB.getTable(tableName);
        OrderTimeResponse response = new OrderTimeResponse();

        try {

            Item item = table.getItem("id", order_id, "estimated_prep_time, estimated_delivery_time", null);

            response = gson.fromJson(item.toJSONPretty(), OrderTimeResponse.class);

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.toJSONPretty());

        } catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }

        return response;
    }

    public static OrderFareResponse getFare(final String order_id) {
        Table table = dynamoDB.getTable(tableName);
        OrderFareResponse response = new OrderFareResponse();

        try {
            Item item = table.getItem("id", order_id, "base_price, tax, delivery_charges", null);

            response = gson.fromJson(item.toJSONPretty(), OrderFareResponse.class);

            System.out.println("Printing item after retrieving it....");
            System.out.println(item.toJSONPretty());

        } catch (Exception e) {
            System.err.println("GetItem failed.");
            System.err.println(e.getMessage());
        }

        return response;
    }

    private static Double getDeliveryCharges() {
        return 20.0; // to be replaced with charges based on location coordinates of customer and restaurant.
    }

    private static Double getEstimatedDeliveryTime() {
        return 10.0; // to be replaced with time based on location coordinates of customer and restaurant assuming 40km/h speed.
    }

    private static Double getEstimatedPrepTime() {
        return 5.0; // to be replaced with prep time by reading restaurant data to get the food item preparation time by them.
    }

    private static Double getPrice() {
        Random rn = new Random();
        return new Double(rn.nextInt(100) + 1); // to be replaced with price by reading restaurant data to get the food item price quoted by them.
    }

    private static String getTimestamp() {
        return String.valueOf(Instant.now().toEpochMilli());
    }

}

