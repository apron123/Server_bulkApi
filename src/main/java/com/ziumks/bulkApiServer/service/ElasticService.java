package com.ziumks.bulkApiServer.service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("elasticService")
public class ElasticService {


    /**
     * 작업자 : 이상민
     * 일 자 : 2024.01.05 16:30
     * 종 류 : Service
     * 용 도 : 엘라스틱 벌크 관련 서비스 로직
     **/

    private static final Logger _logger = LoggerFactory.getLogger(ElasticService.class);


    @Value("${elastic.auth.path}")
    String elasticAuthPath;

    @Value("${elastic.auth.scheme}")
    String elasticAuthScheme;
    @Value("${elastic.auth.hostname}")
    String elasticAuthHostName;

    @Value("${elastic.auth.port}")
    int elasticAuthPort;

    @Value("${date.formatter.pattern}")
    String dateFormatterPattern;

    // api start
    public Map<String, String> elasticApiStart(List<Map<String, Object>> bulkLm,
                                               String indexName, String docId, String tableName)
            throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String hostName = elasticAuthHostName;
        int port = elasticAuthPort;
        String scheme = elasticAuthScheme;
        RestHighLevelClient client;

        _logger.info("elastic bulk insert api start .. ");
        if (scheme.equals("https")) {
            SSLContext sslContext = getSslContext();
            client = getRestHighLevelClientObjectSsl(sslContext, hostName, port, scheme);
        } else {
            client = getRestHighLevelClientObject(hostName, port, scheme);
        }

        //bulk api function 호출
        Map<String, String> rMap = elasticBulkAddListMap(client, bulkLm, indexName, docId, tableName);
        if (rMap.get("result").equals("true")) {
            //tablename의 모든 status = 1, 상태 'up'으로 update

        } else {
            //tablename의 crawler status = 0, 상태 'down'으로 update

        }
        return rMap;
    }

    /**
     * 인증서 파일로 SSLContext 객체 생성 메소드
     */
    public SSLContext getSslContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        // KeyStore 생성 및 로드
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // 인증서 파일 로드
        FileInputStream certificateFile = new FileInputStream(getRealPathAuthFile());

        // 인증서 등록
        Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(certificateFile);
        keyStore.setCertificateEntry("my_certificate", certificate);

        // SSLContext 구성
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(keyStore, new TrustSelfSignedStrategy())
                .build();

        //https 통신 필요한 SSLContext return
        return sslContext;
    }


    /**
     * ElasticHighLevelClient SSLmode 객체 생성 메소드
     */
    public RestHighLevelClient getRestHighLevelClientObjectSsl(SSLContext sslContext, String hostName, int port, String scheme) {

        // elasticSearch RestHighLevelClient 선언
        RestHighLevelClient resthighlevelclient = new RestHighLevelClient(
                RestClient.builder(
                                new HttpHost(hostName, port, scheme))
                        .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                            return httpAsyncClientBuilder
                                    .setSSLContext(sslContext)
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        }));
        return resthighlevelclient;
    }


    /**
     * ElasticHighLevelClient SSLmode 사용안하는 메소드
     */
    public RestHighLevelClient getRestHighLevelClientObject(String hostName, int port, String scheme) {

        // elasticSearch RestHighLevelClient 선언
        RestHighLevelClient resthighlevelclient = new RestHighLevelClient(
                RestClient.builder(
                                new HttpHost(hostName, port, scheme))
                        .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                            return httpAsyncClientBuilder
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        }));
        return resthighlevelclient;
    }

    /**
     * https 인증서 파일 가져오는 메소드
     */
    public File getRealPathAuthFile() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //프로퍼티에 작성된 파일 경로
        File file = new File(elasticAuthPath);
        //해당 파일 return
        return file;
    }


    /**
     * 실제로 elasticBulkApi 수행하는 method
     */
    public Map<String, String> elasticBulkAddListMap(RestHighLevelClient client, List<Map<String, Object>> bulkLm,
                                                     String indexName, String docId, String tableName) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        // bulk 상태 flag
        boolean returnFlag = true;
        // bulk response Msg
        String bulkResponseMsg = "SUCCESS";
        //BulkRequest 생성
        BulkRequest bulkRequest = new BulkRequest();

        //id가 여러개일 수도 있으므로
        String[] splitDocId = docId.split(",");

        try {
            for (Map<String, Object> bulkMap : bulkLm) {
                // 필드가 date 타입일 경우 파싱 소스 - 20231222_1600_이상민
                Set<String> bulkMapKey = bulkMap.keySet();
                for (String key : bulkMapKey) {
                    if (bulkMap.get(key) != null) {
                        String bulkMapValue = bulkMap.get(key).toString();
                        if (bulkMapValue.contains("{date=")) {
                            try {
                                Map<String, LinkedTreeMap<String, Double>> dataMap = new Gson().fromJson(bulkMapValue, Map.class);
                                // 맵에서 날짜 정보 가져오기
                                LinkedTreeMap<String, Double> dateMap = dataMap.get("date");
                                double year = dateMap.get("year");
                                double month = dateMap.get("month");
                                double day = dateMap.get("day");
                                // 맵에서 시간 정보 가져오기
                                LinkedTreeMap<String, Double> timeMap = dataMap.get("time");
                                double hour = timeMap.get("hour");
                                double minute = timeMap.get("minute");
                                double second = timeMap.get("second");
                                double nano = timeMap.get("nano");

                                // LocalDateTime으로 합치기
                                LocalDateTime dateTime = LocalDateTime.of((int) year, (int) month, (int) day, (int) hour, (int) minute, (int) second, (int) nano);
                                // OffsetDateTime으로 변환 (오프셋은 UTC로 설정)
                                OffsetDateTime offsetDateTime = dateTime.atOffset(ZoneOffset.UTC);
                                // Elastic strict_date_optional_time 포맷형식
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatterPattern);
                                String formattedDateTime = offsetDateTime.format(formatter);

                                bulkMap.put(key, formattedDateTime);
                                _logger.info(key + " : " + bulkMap.get(key));
                            } catch (Exception e) {
                                _logger.error("Date Fromatter Error", e);
                            }
                        }
                    }
                }

                String TempdocID = new String();

                if (docId.equals("")) {

                } else if (splitDocId.length == 1) {

                    String pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";
                    String tempIdValue = bulkMap.get(splitDocId[0]).toString();

                    if (tempIdValue.matches(pattern)) {
                        tempIdValue = tempIdValue.replaceAll("[-T:.Z]", "");
                    }

                    //0.0이면 데이터 에러이므로
                    if (tempIdValue.equals("0.0")) {
                        tempIdValue = "0";
                    }
                    TempdocID = splitDocId[0] + ":" + tempIdValue;

                } else {
                    for (String str : splitDocId) {

                        String tempIdValue = bulkMap.get(str).toString();
                        tempIdValue = timeStampAndZeroPatternCheckFnc(tempIdValue);
                        TempdocID += str + ":" + tempIdValue + ",";
                    }
                    TempdocID = TempdocID.substring(0, TempdocID.length() - 1);
                }

                // Create a new IndexRequest in each iteration
                IndexRequest indexRequest = new IndexRequest(indexName);

                //docId가 "" 일 경우 - 즉 pk가 없는 이력 데이터 일 경우 id가 없이 uuid로 생성하여 보냄
                if (docId.equals("")) {
                    // elasticSearch @timestamp pipeline 설정
                    indexRequest.source(bulkMap).setPipeline("timestamp_pipeline");
                } else {
                    // elasticSearch @timestamp pipeline 설정
                    TempdocID = TempdocID.toLowerCase();
                    indexRequest.id(TempdocID).source(bulkMap).setPipeline("timestamp_pipeline");
                }

                //bulkRequest에 add
                bulkRequest.add(indexRequest);
            }

            //BulkResponse 실제 bulkApi 전송
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            //BulkResponse가 실패 했을 경우
            if (bulkResponse.hasFailures()) {
                // Handle failures if needed
                bulkResponseMsg = bulkResponse.buildFailureMessage();
                //flag flase로 변경
                returnFlag = false;
                if (bulkResponseMsg != null && !bulkResponseMsg.isEmpty()) {
                    String[] lines = bulkResponseMsg.split("\\r?\\n");
                    if (lines.length > 0) {
                        bulkResponseMsg = "";
                        //error 에러 처리..
                        bulkResponseMsg = lines[0] + lines[1];

                    }
                }
            }

        } catch (Exception e) {
            // bulk 실패 시
            e.printStackTrace();
            //flag flase로
            returnFlag = false;
        }

        client.close();
        Map<String, String> returnMap = new HashMap<>();
        if (returnFlag) {
            returnMap.put("result", "true");
            returnMap.put("bulkResponseMsg", bulkResponseMsg);

            _logger.info("ElasticBulk API " + "인덱스명 : " + indexName + " : " + "SUCCESS..");
            _logger.info(bulkResponseMsg);
        } else {

            returnMap.put("result", "false");
            returnMap.put("bulkResponseMsg", bulkResponseMsg);

            _logger.warn("ElasticBulk API " + "인덱스명 : " + indexName + " : " + "Failed..");
            _logger.warn(bulkResponseMsg);
        }

        return returnMap;
    }

    public String timeStampAndZeroPatternCheckFnc(String value) {
        // docId에 시간값을 (timestamp)를 사용할 수 없으므로 pattern 체크함.
        String pattern = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z$";

        if (value.matches(pattern)) {
            value = value.replaceAll("[-T:.Z]", "");
        }
        //0.0이면 데이터 에러이므로 = 0으로 바꿈.
        if (value.equals("0.0")) {
            value = "0";
        }
        return value;
    }


    public Map<String, String> deleteSetElastic(String indexName) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String hostName = elasticAuthHostName;
        int port = elasticAuthPort;
        String scheme = elasticAuthScheme;
        RestHighLevelClient client;

        _logger.info("elastic bulk delete api start .. ");
        if (scheme.equals("https")) {
            SSLContext sslContext = getSslContext();
            client = getRestHighLevelClientObjectSsl(sslContext, hostName, port, scheme);
        } else {
            client = getRestHighLevelClientObject(hostName, port, scheme);
        }
        //delete All document 시작.
        Map<String, String> dataMap = deleteAllDocuments(client, indexName);

        return dataMap;
    }


    /**
     * index 명으로 모든 doc delete
     */
    public Map<String, String> deleteAllDocuments(RestHighLevelClient client, String indexName) throws IOException {

        DeleteByQueryRequest request = new DeleteByQueryRequest(indexName);

        request.setQuery(QueryBuilders.matchAllQuery());

        Map<String, String> rMap = new HashMap<>();
        String msg = "";
        String flag = "true";

        try{
            BulkByScrollResponse bulkResponse =  client.deleteByQuery(request,RequestOptions.DEFAULT);
            msg = indexName + " : All documents deleted successfully"; // 배열의 첫 번째 요소로 값 설정
        }catch (Exception e){
            e.printStackTrace();
            msg = e.getMessage();
            flag= "false";
        }finally {
            rMap.put("flag", flag);
            rMap.put("msg", msg);
            client.close();
        }
        return rMap;
    }

}
