package com.moke.assistant.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @ClassName: OSSUtil
 * @Description: OSS存储工具类
 * @Author: xujinpeng
 */
@Slf4j
@Component
public class OSSUtil {

//    /**
//     * Workbook to file 保存到未来云oss
//     * @param workbook
//     * @param fileName
//     * @return
//     */
//    public String uploadFile(Workbook workbook, String fileName){
//        File localFile = new File(talOssConfig.getUploadPath() + fileName);
//        try{
//            File desk = new File(talOssConfig.getUploadPath());
//            if(!desk.exists()){
//                desk.mkdirs();
//            }
//            OutputStream outputStream = new FileOutputStream(localFile);
//            workbook.write(outputStream);
//            //file 类型判断
//            String fileType = FileTypeUtil.getType(localFile);
//            if(!FileUtil.checkReportFile(fileType)){
//                log.info(UPLOAD_FILE_FAIL_+ "type_less:{}", fileType);
//                localFile.delete();
//                return UPLOAD_FILE_FAIL_ + fileType;
//            }
//            if(StringUtils.isBlank(fileName)){
//                fileName = IdUtils.getRandomIncrementStringId() + "." + fileType;
//            }
//            else if(fileName.indexOf(".") < 0){
//                fileName = fileName + "." + fileType;
//            }
//            StorageCredentials credentials = new StorageCredentials(talOssConfig.getAppid(), talOssConfig.getAppkey());
//            StorageClient storageClient = new StorageClient(credentials);
//            PutObjectReq req = PutObjectReq.builder()
//                    .bucket(talOssConfig.getBucket())
//                    .file(localFile)
//                    .key(fileName)
//                    .build();
//            BaseResponse<GetObject> res = storageClient.putObject(req, talOssConfig.getUrl());
//            log.info("FileController_uploadFile2RiskAudit:_param:{}, result:{}", fileName, FastJsonUtils.convertObjectToJSON(res.getData()));
//            localFile.delete();
//            if(res.getResponseStatusCode() == 0){
//                GetObject dataObj = res.getData();
//                return dataObj.getUrl();
//            }
//            else{
//                log.error("FileController_uploadFile2RiskAudit_res_error:{}", res.getResponseStatusCode());
//                return UPLOAD_FILE_FAIL_ + res.getResponseStatusCode();
//            }
//        }catch(Exception e){
//            localFile.delete();
//            log.error("FileController_uploadFile2RiskAudit_error:", e);
//            return UPLOAD_FILE_FAIL_ + "exception";
//        }
//    }




}
