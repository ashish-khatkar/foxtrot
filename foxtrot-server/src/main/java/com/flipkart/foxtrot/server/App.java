/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.server;

//import com.flipkart.foxtrot.common.IndexTemplate;
//import org.joda.time.format.DateTimeFormat;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchUtils;
import com.google.gson.Gson;
import org.joda.time.format.DateTimeFormat;

import java.util.Map;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 15/03/14
 * Time: 9:25 PM
 */

public class App {
    public static void main(String[] args) throws Exception {
        FoxtrotServer foxtrotServer = new FoxtrotServer();
        foxtrotServer.run(args);
//        String str = "{\"data\":{  \n" +
//                "      \"header\":{  \n" +
//                "         \"appName\":\"bro\",\n" +
//                "         \"configName\":\"STATE_TRANSITION\",\n" +
//                "         \"profile\":\"BRO_JOURNEY_ACTION\",\n" +
//                "         \"instanceId\":\"fk-bro-app-ga-fk-bro-app-console-002-249958\",\n" +
//                "         \"eventId\":\"b2848def-2c40-4166-9ef5-e55c1c963cf3\",\n" +
//                "         \"timestamp\":1511368204439\n" +
//                "      },\n" +
//                "      \"data\":{  \n" +
//                "         \"entityId\":\"8b05812a2cec052ed504d1ec122d26a0\",\n" +
//                "         \"fromState\":\"S02\",\n" +
//                "         \"eventType\":\"ActiveUserPnCallbackEvent\",\n" +
//                "         \"timestamp\":1511368204412,\n" +
//                "         \"version\":\"1\",\n" +
//                "         \"stateMachineId\":\"SMPYFDDH\",\n" +
//                "         \"toState\":\"S01\",\n" +
//                "         \"currentStates\":[  \n" +
//                "            \"S01\"\n" +
//                "         ]\n" +
//                "      }\n" +
//                "   }}";
//        Gson gson = new Gson();
//        Map<String, Object> mp = gson.fromJson(str, Map.class);
//        JsonNode metaNode = new ObjectMapper().readTree(str);
////        ObjectNode dataNode = translatedDocument.getData().deepCopy();
//        System.out.println(metaNode.get("data").get("header").get("configName"));
//        List<String> tableTemplates = new ArrayList<>();
//
//
//        tableTemplates.add("testConfig1");
//        tableTemplates.add("testConfig2");
//
//        System.out.println(tableTemplates.toString());
//
//

//        String date = DateTimeFormat.forPattern("dd-M-yyyy-HH").print(1512730283000L);
//        String date1 = DateTimeFormat.forPattern("HH").print(1512730283000L);
//        System.out.println(date);
//        System.out.println(date1);
    }
}
