package com.ziumks.bulkApiServer.controller;


import com.ziumks.bulkApiServer.model.dto.BulkRequestDTO;
import com.ziumks.bulkApiServer.model.dto.BulkResponseDTO;
import com.ziumks.bulkApiServer.service.ElasticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/bulk")
@Tag(name = "1.Bulk 컨트롤러",description="Elastic Bulk Api 컨트롤러")
public class BulkController {

    @Autowired
    ElasticService elasticService;

    private static final Logger _log = LoggerFactory.getLogger(BulkController.class);


    @Operation(summary = "elastic bulk status check Api",description="elastic bulk api - json")
    @GetMapping(value = "/check")
    public ResponseEntity<?> elasticCheck() {


        return new ResponseEntity<>(200,HttpStatus.OK);

    }







    @Operation(summary = "elastic bulk insert Api",description="elastic bulk api - json")
    @PostMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BulkResponseDTO> elasticJsontoBulkInsert(
            @Parameter(description  = "bulk Request Dto", required = true) @RequestBody BulkRequestDTO bulkRequestDTO) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        BulkResponseDTO bulkResponse = new BulkResponseDTO();
        List<Map<String, Object>> bulkList = bulkRequestDTO.getDataList();


        String indexName = bulkRequestDTO.getIndexName();
        String tableName = bulkRequestDTO.getTableName();
        String docId = bulkRequestDTO.getDocId();


        Map<String, String> elasticMethodResponse = elasticService.elasticApiStart(bulkList, indexName, docId, tableName);

        String msg = elasticMethodResponse.get("bulkResponseMsg");
        String result = elasticMethodResponse.get("result");

        bulkResponse.setIndexName(indexName);
        bulkResponse.setTableName(tableName);
        bulkResponse.setMsg(msg);

        if (result.equals("true")) {
            bulkResponse.setResponseCode(200);
            return new ResponseEntity<>(bulkResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(bulkResponse, HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/delete")
    public ResponseEntity<BulkResponseDTO> elasticIndexDocDelete(
            @RequestParam(value = "indexName", required = true) String indexName) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        BulkResponseDTO bulkResponse = new BulkResponseDTO();
        Map<String, String> dataMap = elasticService.deleteSetElastic(indexName);

        String flag = dataMap.get("flag");
        String msg = dataMap.get("msg");


        bulkResponse.setIndexName(indexName);
        bulkResponse.setMsg(msg);
        bulkResponse.setResponseCode(200);

        HttpStatus status = HttpStatus.OK;

        if ("false".equals(flag)) {
            bulkResponse.setResponseCode(400);
            status = HttpStatus.BAD_REQUEST;

        }
        return new ResponseEntity<>(bulkResponse,status);

    }





//    @GetMapping(value = "/bulkFormTest")
//    public ResponseEntity<?> elasticFormtoBulkInsert() throws HttpConnectionException {
//        HttpConnection<String> httpConnection = new HttpConnection<>();
//
//        String url ="http://localhost:11960/bulk";
//
//        Map<String,Object> formData = new HashMap<>();
//        List<Map<String,Object>> dataList = new ArrayList<>();
//
//        Map<String,Object> dataListFormMap = new HashMap<>();
//        dataListFormMap.put("seq",1);
//        dataListFormMap.put("name","kim");
//        dataListFormMap.put("created_at","2024-01-12 17:12:00.109");
//
//        dataList.add(dataListFormMap);
//
//
//        Map<String,Object> dataListFormMap2 = new HashMap<>();
//        dataListFormMap2.put("seq",2);
//        dataListFormMap2.put("name","aaa");
//        dataListFormMap2.put("created_at","2024-01-11 17:12:00.109");
//        dataList.add(dataListFormMap2);
//
//
//        Map<String,Object> dataListFormMap3 = new HashMap<>();
//        dataListFormMap3.put("seq",3);
//        dataListFormMap3.put("name","hahaha");
//        dataListFormMap3.put("created_at","2024-01-10 17:12:00.109");
//        dataList.add(dataListFormMap3);
//
//
//        formData.put("tableName","kim_test_form");
//        formData.put("indexName","kim_test_form");
//
//        formData.put("docId","created_at");
//        formData.put("dataList",dataList);
//
//        Gson gson = new Gson();
//        String reqBody = gson.toJson(formData);
//
//        Map<String,Object> reqHeader = new HashMap<>();
//        reqHeader.put("Content-Type","application/json");
//        String res =  httpConnection.doPost(url,reqHeader,reqBody);
//        BulkResponseDTO dto =  gson.fromJson(res,BulkResponseDTO.class);
//        _log.info(dto.toString());
//        return new ResponseEntity<>(dto, HttpStatus.OK);
//    }
}
