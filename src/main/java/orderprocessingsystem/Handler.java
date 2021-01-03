package orderprocessingsystem;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import orderprocessingsystem.models.*;
import orderprocessingsystem.dbConnectors.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.HashMap;

// Handler value: example.HandlerApiGateway
public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{
  Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context)
  {
    final String path = event.getPath();
    final String bodyString = event.getBody();

    String responseBody = "";
    if (path.equals("/order/fare")) {
      final GetOrderFareRequest request = gson.fromJson(bodyString, GetOrderFareRequest.class);
      final OrderFareResponse response = OrderDBConnector.getFare(request.order_id);
      responseBody = gson.toJson(response);
    } else if(path.equals("/order/create")) {
      final OrderRequest orderRequest = gson.fromJson(bodyString, OrderRequest.class);
      OrderDBConnector.createOrder(orderRequest.food_id, orderRequest.quantity);
    } else if(path.equals("/order/time")) {
      final GetOrderTimeRequest request = gson.fromJson(bodyString, GetOrderTimeRequest.class);
      final OrderTimeResponse response = OrderDBConnector.getTime(request.order_id);
      responseBody = gson.toJson(response);
    } else if(path.equals("/order/update")) {
      final UpdateOrderRequest request = gson.fromJson(bodyString, UpdateOrderRequest.class);
      OrderDBConnector.updateOrder(request.order_id, request.status);
    }

    LambdaLogger logger = context.getLogger();
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
    response.setIsBase64Encoded(false);
    response.setStatusCode(200);
    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "text/html");
    response.setHeaders(headers);
    response.setBody(responseBody);
    // log execution details
    Util.logEnvironment(event, context, gson);
    return response;
  }
}

