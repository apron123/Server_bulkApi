package com.ziumks.bulkApiServer.util;

import javax.persistence.Table;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;


//공통 메소드 묶어놓은 클래스..
public class CommonMethod {


    /**
     * @Table 어노테이션으로 테이블 명 가져오는 메서드
     * @param entityClass Class<?>
     * @return string
     */
    public static String getTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.name();
        }
        return null; // 테이블명이 지정되지 않은 경우
    }

    /*** 정규표현식 사용하여 숫자 말고 전부 제거
     * @param input string
     * @return string
     */
    public static String regexMethod(String input) {
        // 방법 1: 정규표현식 사용
        String result = input.replaceAll("[^0-9.]+", "");
        return result;
    }

    /*** Xml data를 변환 class 객체로 parser
     * @param xmlData string
     * @param targetClass Class<T>
     * @return Class<T>
     */
    public static <T> T xmlToObject(String xmlData, Class<T> targetClass) {
        try {
            // JAXBContext 객체 생성
            JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);
            // Unmarshaller 생성
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            StringReader reader = new StringReader(xmlData);
            // Unmarshal 수행하여 XML을 객체로 변환
            return targetClass.cast(unmarshaller.unmarshal(reader));
        } catch (JAXBException e) {
            e.printStackTrace();
            // 여기서 언마샬 오류 처리를 수행할 수 있습니다.
            return null;
        }
    }
}
