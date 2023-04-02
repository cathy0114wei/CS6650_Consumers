package controller;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class DBController {
    public void putItemInTable(DynamoDbClient ddb,
                                      String tableName,
                                      String userFrom,
                                      String userFromVal,
                                      String userTo,
                                      String userToVal){

        // Get the previous values if applicable.
        Set<Integer> userToSet = getItems(ddb, tableName, userFrom, userFromVal, userTo);
        if(userToSet == null || userToSet.isEmpty()){
            userToSet = new HashSet<>();
        }
        userToSet.add(Integer.valueOf(userToVal));

        // Generate the added string value.
        List<String> intString = new ArrayList<>();
        int i = 0;
        for (int s : userToSet) {
            //We only store 100 swipees for each user.
            if(++i > 100) break;
            intString.add(String.valueOf(s));
            intString.add(",");
        }
        intString.remove(intString.size() - 1);
        String updatedUserToVal = String.join("", intString);
        System.out.println("after update: " + updatedUserToVal);

        HashMap<String,AttributeValue> itemValues = new HashMap<>();
        itemValues.put(userFrom, AttributeValue.builder().s(userFromVal).build());
        itemValues.put(userTo, AttributeValue.builder().s(updatedUserToVal).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {
            PutItemResponse response = ddb.putItem(request);
            System.out.println(tableName +" was successfully updated. The request id is "+response.responseMetadata().requestId());
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public Set<Integer> getItems(DynamoDbClient ddb,
                                String tableName,
                                String userFromName,
                                String userFromVal,
                                String userToName) {
        Set<Integer> res = new HashSet<>();

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(userFromName, AttributeValue.builder().s(userFromVal).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(keyToGet)
                .build();

        try {
            GetItemResponse response = ddb.getItem(request);
            if (response.item().isEmpty()) {
                System.out.println("The item " + userFromVal + " does not exist in the table " + tableName);
                return null;
            } else {
                System.out.println("The item " + userFromVal + " exists in the table " + tableName);
                String values = response.item().get(userToName).s();
                System.out.println("before update: " + values);
                Set<String> tmp = new HashSet<>(Arrays.asList(values.split(",")));
                for(String s : tmp){
                    res.add(Integer.valueOf(s));
                }
                return res;
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
